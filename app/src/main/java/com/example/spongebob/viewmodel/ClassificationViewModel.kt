package com.example.spongebob.viewmodel

import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spongebob.model.TFLiteModelManager
import com.example.spongebob.data.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.RandomAccessFile
import kotlin.system.measureTimeMillis

// UI State for classification
data class ClassificationUiState(
    val imageUri: Uri? = null,
    val isModelLoading: Boolean = true,  // Loading model on app start
    val isProcessing: Boolean = false,
    val result: ClassificationResult? = null,
    val errorMessage: String? = null,
    val isModelReady: Boolean = false
)

data class ClassificationResult(
    val className: String,
    val confidence: Float,
    val allPredictions: List<Prediction>,
    val inferenceTimeMillis: Long = 0L,  // Time taken for inference in milliseconds
    val cpuUsagePercent: Float = 0f,     // CPU usage during inference
    val memoryUsedMB: Long = 0L,         // Memory used during inference in MB
    val memoryTotalMB: Long = 0L,        // Total available memory in MB
    val useNnapi: Boolean = false        // Whether hardware acceleration was used
)

@Serializable
data class Prediction(
    val className: String,
    val confidence: Float
)

class ClassificationViewModel(
    private val context: Context,
    private val tfLiteModelManager: TFLiteModelManager,
    private val preferencesManager: PreferencesManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassificationUiState())
    val uiState: StateFlow<ClassificationUiState> = _uiState.asStateFlow()

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    init {
        initializeModel()
    }

    private fun initializeModel() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isModelLoading = true) }
                tfLiteModelManager.initialize()
                _uiState.update { it.copy(isModelLoading = false, isModelReady = true) }
            } catch (e: Exception) {
                val errorMsg = buildString {
                    appendLine("Failed to load TFLite model")
                    appendLine("Error: ${e.javaClass.simpleName}")
                    appendLine("Message: ${e.message}")
                    e.cause?.let { appendLine("Cause: ${it.message}") }
                    appendLine()
                    appendLine("Please check:")
                    appendLine("- small.tflite is in app/src/main/assets/")
                    appendLine("- Model file size is correct (${e.javaClass.simpleName})")
                }
                _uiState.update {
                    it.copy(
                        isModelLoading = false,
                        isModelReady = false,
                        errorMessage = errorMsg.trim()
                    )
                }
            }
        }
    }

    fun onImageSelected(uri: Uri) {
        _uiState.update { it.copy(imageUri = uri, result = null, errorMessage = null) }
    }

    fun onImageCropped(uri: Uri) {
        _uiState.update { it.copy(imageUri = uri, result = null, errorMessage = null) }
    }

    fun onClearImage() {
        _uiState.update {
            it.copy(
                imageUri = null,
                result = null,
                errorMessage = null
            )
        }
    }

    fun onClassify() {
        val uri = _uiState.value.imageUri
        if (uri == null) {
            _uiState.update { it.copy(errorMessage = "Please select an image first") }
            return
        }

        if (!tfLiteModelManager.isInitialized) {
            _uiState.update {
                it.copy(
                    errorMessage = "Model not ready. Please wait for model to load."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

            try {
                // Get memory info before inference
                val memoryInfoBefore = getMemoryInfo()

                // Get CPU time before
                val cpuTimeBefore = getCpuTime()

                // Measure inference time
                var inferenceResult: ClassificationResult? = null
                val inferenceTime = measureTimeMillis {
                    inferenceResult = tfLiteModelManager.runInference(uri)
                }

                // Get CPU time after
                val cpuTimeAfter = getCpuTime()

                // Get memory info after inference
                val memoryInfoAfter = getMemoryInfo()

                // Calculate CPU usage during inference
                val cpuUsagePercent = calculateCpuUsage(cpuTimeBefore, cpuTimeAfter, inferenceTime)

                // Add metrics to the result
                val resultWithMetrics = inferenceResult?.copy(
                    inferenceTimeMillis = inferenceTime,
                    cpuUsagePercent = cpuUsagePercent,
                    memoryUsedMB = memoryInfoAfter.usedMB,
                    memoryTotalMB = memoryInfoAfter.totalMB
                )

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        result = resultWithMetrics
                    )
                }
            } catch (e: Exception) {
                val errorMsg = buildString {
                    appendLine("Inference failed")
                    appendLine("Error: ${e.javaClass.simpleName}: ${e.message}")
                    appendLine()
                    appendLine("Image URI: $uri")
                    e.stackTrace.take(3).forEach {
                        appendLine("  at $it")
                    }
                }
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = errorMsg.trim()
                    )
                }
            }
        }
    }

    private data class MemoryInfo(val usedMB: Long, val totalMB: Long)

    private fun getMemoryInfo(): MemoryInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalMem = memoryInfo.totalMem / (1024 * 1024) // Convert to MB
        val availableMem = memoryInfo.availMem / (1024 * 1024) // Convert to MB
        val usedMem = totalMem - availableMem

        return MemoryInfo(usedMem, totalMem)
    }

    private fun getCpuTime(): Long {
        try {
            val pid = Process.myPid()
            val stat = RandomAccessFile("/proc/$pid/stat", "r").use { it.readLine() }
            val parts = stat.split(" ")
            // utime + stime (clock ticks) - indices 13 and 14 in /proc/[pid]/stat
            val utime = parts[13].toLong()
            val stime = parts[14].toLong()
            return utime + stime
        } catch (e: Exception) {
            return 0L
        }
    }

    private fun calculateCpuUsage(cpuTimeBefore: Long, cpuTimeAfter: Long, elapsedTimeMs: Long): Float {
        if (cpuTimeBefore == 0L || cpuTimeAfter == 0L || elapsedTimeMs == 0L) {
            return 0f
        }

        try {
            // Get clock ticks per second (usually 100 on Android)
            val clkTck = 100L

            // CPU time used during inference (in clock ticks)
            val cpuTicksUsed = cpuTimeAfter - cpuTimeBefore

            // Convert elapsed time to clock ticks
            val elapsedTicks = (elapsedTimeMs * clkTck) / 1000

            // Get number of CPU cores
            val numCores = Runtime.getRuntime().availableProcessors()

            // Calculate CPU usage percentage
            val cpuUsage = (cpuTicksUsed.toFloat() / elapsedTicks.toFloat()) * 100f

            // Clamp to reasonable range (0-100% per core)
            return cpuUsage.coerceIn(0f, 100f * numCores)
        } catch (e: Exception) {
            return 0f
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        tfLiteModelManager.close()
    }
}
