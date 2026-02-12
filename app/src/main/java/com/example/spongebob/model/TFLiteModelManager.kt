package com.example.spongebob.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.DataType
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Data class to hold detailed model information
 */
data class ModelInfo(
    // Basic Info
    val modelFileName: String,
    val modelFileSizeBytes: Long,
    val modelFileSizeFormatted: String,

    // Input Info
    val inputShape: IntArray,
    val inputDataType: String,
    val inputWidth: Int,
    val inputHeight: Int,
    val inputChannels: Int,
    val batchSize: Int,
    val inputFormat: String, // "NHWC" or "NCHW"
    val inputSizeBytes: Long,
    val inputSizeFormatted: String,

    // Output Info
    val outputShape: IntArray,
    val outputDataType: String,
    val outputClasses: Int,
    val outputSizeBytes: Long,
    val outputSizeFormatted: String,

    // Class Labels
    val classLabels: List<String>,

    // Runtime Config
    val numThreads: Int,
    val useGpu: Boolean,
    val isGpuSupported: Boolean,

    // Model Metadata
    val totalParameters: Long,
    val totalParametersFormatted: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModelInfo

        if (!inputShape.contentEquals(other.inputShape)) return false
        if (!outputShape.contentEquals(other.outputShape)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = inputShape.contentHashCode()
        result = 31 * result + outputShape.contentHashCode()
        return result
    }
}

/**
 * TensorFlow Lite model manager for running image classification models on Android.
 *
 * MODEL PLACEMENT:
 * Put your TFLite model file at: app/src/main/assets/small.tflite
 *
 * The model input shape is read dynamically from the model file.
 */
class TFLiteModelManager(
    private val context: Context,
    private val useGpu: Boolean = false
) {
    private var interpreter: Interpreter? = null
    private var inputWidth: Int = 640  // Will be read from model
    private var inputHeight: Int = 640 // Will be read from model
    private var outputCount: Int = 3   // Will be read from model
    private var modelInfo: ModelInfo? = null

    var isInitialized: Boolean = false
        private set

    companion object {
        private const val TAG = "TFLiteModelManager"
        const val MODEL_FILE = "small.tflite"

        val CLASS_LABELS = listOf(
            "Sehat",
            "Sedang",
            "Parah"
        )

        // TFLite Runtime Version
        val TFLITE_VERSION = "2.14.0"
    }

    /**
     * Get detailed model information.
     */
    fun getModelInfo(): ModelInfo? = modelInfo

    /**
     * Check if the device supports GPU acceleration.
     * Note: GPU delegate requires additional setup and may not work on all devices.
     */
    fun isGpuSupported(): Boolean {
        // For now, return false to use CPU which is more stable
        Log.d(TAG, "GPU delegate: Currently using CPU for stability")
        return false
    }

    /**
     * Initialize the TensorFlow Lite interpreter.
     * Call this once before running inference.
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            initializeInternal()
        } catch (e: Exception) {
            Log.e(TAG, "========== Initialization Failed ==========", e)
            Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Error message: ${e.message}")
            isInitialized = false
            throw e
        }
    }

    private fun initializeInternal() {
        Log.i(TAG, "========== TFLite Model Initialization Started ==========")
        Log.d(TAG, "Model file: $MODEL_FILE")

        // Get model file size
        val modelFileSize = getModelFileSize()

        // Load model from assets
        Log.d(TAG, "[1/4] Loading model from assets...")
        val modelBuffer: MappedByteBuffer = try {
            loadModelFile()
        } catch (e: Exception) {
            Log.e(TAG, "[1/4] Failed to load model from assets", e)
            throw RuntimeException(
                "Model file not found or cannot be read. " +
                "Make sure '$MODEL_FILE' is in app/src/main/assets/", e
            )
        }
        Log.i(TAG, "[1/4] Model loaded from assets: ${modelBuffer.capacity()} bytes")

        // Create interpreter options
        Log.d(TAG, "[2/4] Creating interpreter options...")
        val options = Interpreter.Options()
        val numThreads = 4

        // Use CPU with multiple threads for stability
        Log.d(TAG, "Using CPU execution with $numThreads threads")
        options.numThreads = numThreads

        Log.i(TAG, "[2/4] Interpreter options created (CPU execution)")

        // Create interpreter
        Log.d(TAG, "[3/4] Creating TFLite interpreter...")
        try {
            val startTime = System.currentTimeMillis()
            interpreter = Interpreter(modelBuffer, options)
            val elapsed = System.currentTimeMillis() - startTime
            Log.i(TAG, "[3/4] TFLite interpreter created in ${elapsed}ms")
        } catch (e: Exception) {
            Log.e(TAG, "[3/4] Failed to create TFLite interpreter", e)
            throw RuntimeException("Failed to create TFLite interpreter. Model may be corrupted.\nError: ${e.javaClass.simpleName}: ${e.message}", e)
        }

        // Read model input/output shapes dynamically
        Log.d(TAG, "[4/4] Getting model input/output info...")

        var inputFormat = "Unknown"
        var batch = 1
        var channels = 3

        interpreter?.let { interp ->
            val inputTensor = interp.getInputTensor(0)
            val outputTensor = interp.getOutputTensor(0)

            val inputShape = inputTensor.shape()
            val outputShape = outputTensor.shape()

            Log.i(TAG, "[4/4] Model input shape: ${inputShape.toList()}")
            Log.i(TAG, "[4/4] Model output shape: ${outputShape.toList()}")

            // Parse input shape - expecting [batch, height, width, channels] or [batch, channels, height, width]
            when (inputShape.size) {
                4 -> {
                    batch = inputShape[0]
                    // NHWC format: [batch, height, width, channels]
                    if (inputShape[3] == 3 || inputShape[3] == 1 || inputShape[3] == 4) {
                        inputHeight = inputShape[1]
                        inputWidth = inputShape[2]
                        channels = inputShape[3]
                        inputFormat = "NHWC"
                        Log.i(TAG, "Input format: NHWC [batch=$batch, height=$inputHeight, width=$inputWidth, channels=$channels]")
                    } else {
                        // NCHW format: [batch, channels, height, width]
                        channels = inputShape[1]
                        inputHeight = inputShape[2]
                        inputWidth = inputShape[3]
                        inputFormat = "NCHW"
                        Log.i(TAG, "Input format: NCHW [batch=$batch, channels=$channels, height=$inputHeight, width=$inputWidth]")
                    }
                }
                else -> {
                    Log.w(TAG, "Unexpected input shape dimensions: ${inputShape.size}, using default 640x640")
                }
            }

            // Parse output shape
            outputCount = outputShape.last()
            Log.i(TAG, "Output class count: $outputCount")

            // Calculate sizes
            val inputSizeBytes = (batch * inputHeight * inputWidth * channels * 4).toLong() // 4 bytes per float32
            val outputSizeBytes = (batch * outputCount * 4).toLong()

            // Estimate parameters (rough estimate based on model size)
            val totalParams = modelBuffer.capacity().toLong() / 4 // Rough estimate

            // Build ModelInfo
            modelInfo = ModelInfo(
                modelFileName = MODEL_FILE,
                modelFileSizeBytes = modelFileSize,
                modelFileSizeFormatted = formatFileSize(modelFileSize),

                inputShape = inputShape,
                inputDataType = getDataTypeName(inputTensor.dataType()),
                inputWidth = inputWidth,
                inputHeight = inputHeight,
                inputChannels = channels,
                batchSize = batch,
                inputFormat = inputFormat,
                inputSizeBytes = inputSizeBytes,
                inputSizeFormatted = formatFileSize(inputSizeBytes),

                outputShape = outputShape,
                outputDataType = getDataTypeName(outputTensor.dataType()),
                outputClasses = outputCount,
                outputSizeBytes = outputSizeBytes,
                outputSizeFormatted = formatFileSize(outputSizeBytes),

                classLabels = CLASS_LABELS,

                numThreads = numThreads,
                useGpu = useGpu,
                isGpuSupported = isGpuSupported(),

                totalParameters = totalParams,
                totalParametersFormatted = formatNumber(totalParams)
            )
        }

        Log.d(TAG, "Final input size: ${inputWidth}x${inputHeight}")
        Log.d(TAG, "Class labels: $CLASS_LABELS")
        isInitialized = true
        Log.i(TAG, "========== TFLite Model Ready ==========")
    }

    private fun getModelFileSize(): Long {
        return try {
            context.assets.openFd(MODEL_FILE).length
        } catch (e: Exception) {
            try {
                val assetInputStream = context.assets.open(MODEL_FILE)
                val size = assetInputStream.available().toLong()
                assetInputStream.close()
                size
            } catch (e2: Exception) {
                0L
            }
        }
    }

    private fun getDataTypeName(dataType: DataType): String {
        return when (dataType) {
            DataType.FLOAT32 -> "Float32"
            DataType.INT32 -> "Int32"
            DataType.UINT8 -> "UInt8"
            DataType.INT64 -> "Int64"
            DataType.INT16 -> "Int16"
            DataType.INT8 -> "Int8"
            else -> dataType.name
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format("%.2f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }

    private fun formatNumber(num: Long): String {
        return when {
            num >= 1_000_000 -> String.format("%.2fM", num / 1_000_000.0)
            num >= 1_000 -> String.format("%.1fK", num / 1_000.0)
            else -> num.toString()
        }
    }

    /**
     * Run inference on an image URI.
     *
     * @param imageUri URI of the image to classify
     * @return ClassificationResult with top prediction and all probabilities
     */
    suspend fun runInference(imageUri: Uri): com.example.spongebob.viewmodel.ClassificationResult =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "========== Inference Started ==========")
            Log.d(TAG, "Image URI: $imageUri")

            if (interpreter == null) {
                Log.e(TAG, "Model not initialized!")
                throw IllegalStateException("Model not initialized. Call initialize() first.")
            }

            try {
                // Step 1: Load and preprocess image
                Log.d(TAG, "[1/4] Loading image...")
                val startTime = System.currentTimeMillis()

                val inputStream: InputStream = context.contentResolver.openInputStream(imageUri)
                    ?: throw IllegalArgumentException("Cannot open image URI")

                val inputBuffer = preprocessImage(inputStream)
                inputStream.close()

                Log.d(TAG, "[1/4] Image preprocessed in ${System.currentTimeMillis() - startTime}ms")

                // Step 2: Prepare output buffer
                Log.d(TAG, "[2/4] Preparing output buffer...")
                val outputBuffer = Array(1) { FloatArray(outputCount) }
                Log.d(TAG, "[2/4] Output buffer created: [1, $outputCount]")

                // Step 3: Run inference
                Log.d(TAG, "[3/4] Running inference...")
                val inferenceStart = System.currentTimeMillis()

                interpreter?.run(inputBuffer, outputBuffer)

                val inferenceTime = System.currentTimeMillis() - inferenceStart
                Log.i(TAG, "[3/4] Inference completed in ${inferenceTime}ms")

                // Step 4: Process output
                Log.d(TAG, "[4/4] Processing output...")
                val probabilities = outputBuffer[0]

                // Log raw output
                Log.d(TAG, "========== RAW OUTPUT START ==========")
                probabilities.forEachIndexed { index, value ->
                    Log.d(TAG, "output[$index] = $value")
                }
                Log.d(TAG, "========== RAW OUTPUT END ===========")

                val result = parseResults(probabilities)

                Log.i(TAG, "[4/4] Results processed")
                Log.i(TAG, "========== Inference Complete ==========")
                Log.i(TAG, "Predicted: ${result.className} (${(result.confidence * 100).toInt()}%)")

                result

            } catch (e: Exception) {
                Log.e(TAG, "========== Inference Failed ==========", e)
                throw RuntimeException("Inference failed: ${e.message}", e)
            }
        }

    /**
     * Preprocess image for model input.
     * Converts image to [1, height, width, 3] float32 tensor with values in [0,1]
     */
    private fun preprocessImage(inputStream: InputStream): ByteBuffer {
        // Load bitmap
        val bitmap = BitmapFactory.decodeStream(inputStream)
            ?: throw IllegalArgumentException("Failed to decode image")

        Log.d(TAG, "Original bitmap: ${bitmap.width}x${bitmap.height}")

        // Resize to model input size (read from model)
        val resized = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)
        Log.d(TAG, "Resized to: ${resized.width}x${resized.height}")

        // Convert to ByteBuffer in NHWC format [1, height, width, 3]
        val bufferSize = 1 * inputHeight * inputWidth * 3 * 4 // 4 bytes per float
        val buffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputWidth * inputHeight)
        resized.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)

        for (pixel in pixels) {
            // Extract RGB values and normalize to [0, 1]
            val r = ((pixel shr 16) and 0xFF) / 255f
            val g = ((pixel shr 8) and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f

            buffer.putFloat(r)
            buffer.putFloat(g)
            buffer.putFloat(b)
        }

        buffer.rewind()
        return buffer
    }

    /**
     * Parse raw output probabilities into ClassificationResult.
     */
    private fun parseResults(probabilities: FloatArray): com.example.spongebob.viewmodel.ClassificationResult {
        // Apply softmax if needed (if model outputs logits)
        val softmaxProbs = softmax(probabilities)

        // Log all predictions
        softmaxProbs.forEachIndexed { index, prob ->
            Log.d(TAG, "  ${CLASS_LABELS.getOrElse(index) { "Class $index" }}: ${(prob * 100).toInt()}%")
        }

        // Create prediction list (use min of output count and class labels)
        val numClasses = minOf(softmaxProbs.size, CLASS_LABELS.size)
        val predictions = softmaxProbs.take(numClasses).mapIndexed { index, prob ->
            com.example.spongebob.viewmodel.Prediction(
                className = CLASS_LABELS.getOrElse(index) { "Class $index" },
                confidence = prob
            )
        }.sortedByDescending { it.confidence }

        val actuallyUsingGpu = useGpu && isGpuSupported()
        Log.i(TAG, "Execution provider: ${if (actuallyUsingGpu) "GPU (Hardware)" else "CPU"}")

        return com.example.spongebob.viewmodel.ClassificationResult(
            className = predictions.first().className,
            confidence = predictions.first().confidence,
            allPredictions = predictions,
            useNnapi = actuallyUsingGpu // Reusing the field name for hardware acceleration status
        )
    }

    /**
     * Apply softmax to convert logits to probabilities.
     */
    private fun softmax(logits: FloatArray): FloatArray {
        val max = logits.maxOrNull() ?: 0f
        val exp = logits.map { kotlin.math.exp((it - max).toDouble()).toFloat() }
        val sum = exp.sum()
        return exp.map { it / sum }.toFloatArray()
    }

    /**
     * Load model file from assets as MappedByteBuffer.
     */
    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Release resources.
     */
    fun close() {
        Log.d(TAG, "Closing TFLite model resources")
        interpreter?.close()
        interpreter = null
        isInitialized = false
        Log.d(TAG, "TFLite model resources closed")
    }
}
