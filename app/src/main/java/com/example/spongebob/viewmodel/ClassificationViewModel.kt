package com.example.spongebob.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spongebob.model.OnnxModelManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val allPredictions: List<Prediction>
)

data class Prediction(
    val className: String,
    val confidence: Float
)

class ClassificationViewModel(
    private val onnxModelManager: OnnxModelManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassificationUiState())
    val uiState: StateFlow<ClassificationUiState> = _uiState.asStateFlow()

    init {
        initializeModel()
    }

    private fun initializeModel() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isModelLoading = true) }
                onnxModelManager.initialize()
                _uiState.update { it.copy(isModelLoading = false, isModelReady = true) }
            } catch (e: Exception) {
                val errorMsg = buildString {
                    appendLine("Failed to load ONNX model")
                    appendLine("Error: ${e.javaClass.simpleName}")
                    appendLine("Message: ${e.message}")
                    e.cause?.let { appendLine("Cause: ${it.message}") }
                    appendLine()
                    appendLine("Please check:")
                    appendLine("- model.onnx is in app/src/main/assets/")
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

        if (!onnxModelManager.isInitialized) {
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
                val result = onnxModelManager.runInference(uri)
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        result = result
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

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        onnxModelManager.close()
    }
}
