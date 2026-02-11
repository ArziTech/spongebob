package com.example.spongebob.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

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
    }

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

        // Use CPU with multiple threads for stability
        Log.d(TAG, "Using CPU execution with 4 threads")
        options.numThreads = 4

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
        interpreter?.let { interp ->
            val inputShape = interp.getInputTensor(0).shape()
            val outputShape = interp.getOutputTensor(0).shape()
            Log.i(TAG, "[4/4] Model input shape: ${inputShape.toList()}")
            Log.i(TAG, "[4/4] Model output shape: ${outputShape.toList()}")

            // Parse input shape - expecting [batch, height, width, channels] or [batch, channels, height, width]
            when (inputShape.size) {
                4 -> {
                    // NHWC format: [batch, height, width, channels]
                    if (inputShape[3] == 3) {
                        inputHeight = inputShape[1]
                        inputWidth = inputShape[2]
                        Log.i(TAG, "Input format: NHWC [batch, height=$inputHeight, width=$inputWidth, channels=3]")
                    } else {
                        // NCHW format: [batch, channels, height, width]
                        inputHeight = inputShape[2]
                        inputWidth = inputShape[3]
                        Log.i(TAG, "Input format: NCHW [batch, channels=3, height=$inputHeight, width=$inputWidth]")
                    }
                }
                else -> {
                    Log.w(TAG, "Unexpected input shape dimensions: ${inputShape.size}, using default 640x640")
                }
            }

            // Parse output shape
            outputCount = outputShape.last()
            Log.i(TAG, "Output class count: $outputCount")
        }

        Log.d(TAG, "Final input size: ${inputWidth}x${inputHeight}")
        Log.d(TAG, "Class labels: $CLASS_LABELS")
        isInitialized = true
        Log.i(TAG, "========== TFLite Model Ready ==========")
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
