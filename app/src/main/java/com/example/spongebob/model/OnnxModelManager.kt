package com.example.spongebob.model

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * ONNX Runtime model manager for running PyTorch models on Android.
 *
 * MODEL PLACEMENT:
 * Put your ONNX model file at: app/src/main/assets/model.onnx
 *
 * Expected model input/output for 3-class classification:
 * - Input: [1, 3, 480, 480] float32 tensor (RGB image normalized to [0,1])
 * - Output: [1, 3] float32 tensor (class probabilities for Sehat, Sedang, Parah)
 */
class OnnxModelManager(
    private val context: Context,
    private val useNnapi: Boolean = false
) {

    private var environment: OrtEnvironment? = null
    private var session: OrtSession? = null
    private var inputName: String? = null
    private var outputName: String? = null
    var isInitialized: Boolean = false
        private set

    // Model configuration - ADJUST THESE TO YOUR MODEL
    companion object {
        private const val TAG = "OnnxModelManager"
        const val MODEL_FILE = "model.onnx"
        const val INPUT_WIDTH = 480   // Model input size
        const val INPUT_HEIGHT = 480  // Model input size
        const val CLASS_COUNT = 3 // 3 classes: Sehat, Sedang, Parah
        private const val MIN_API_LEVEL_FOR_NNAPI = 27 // Android 8.1+

        // Class labels - REPLACE WITH YOUR MODEL'S CLASSES
        val CLASS_LABELS = listOf(
            "Sehat",
            "Sedang",
            "Parah"
        )
    }

    /**
     * Check if the device supports NNAPI (Neural Networks API).
     * Requires API level 27+ (Android 8.1) and the NNAPI feature flag.
     */
    fun isNnapiSupported(): Boolean {
        if (Build.VERSION.SDK_INT < MIN_API_LEVEL_FOR_NNAPI) {
            Log.d(TAG, "NNAPI not supported: API level ${Build.VERSION.SDK_INT} < $MIN_API_LEVEL_FOR_NNAPI")
            return false
        }
        // FEATURE_NNAPI constant string ("android.software.nnapi")
        val hasFeature = context.packageManager.hasSystemFeature("android.software.nnapi")
        Log.d(TAG, "NNAPI supported: $hasFeature")
        return hasFeature
    }

    /**
     * Initialize the ONNX Runtime session.
     * Call this once before running inference.
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            initializeInternal()
        } catch (e: Exception) {
            Log.e(TAG, "========== Initialization Failed ==========", e)
            Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Error message: ${e.message}")
            e.cause?.let {
                Log.e(TAG, "Caused by: ${it.javaClass.simpleName}: ${it.message}")
            }
            isInitialized = false
            throw e
        }
    }

    private fun initializeInternal() {
        Log.i(TAG, "========== ONNX Model Initialization Started ==========")
        Log.d(TAG, "Model file: $MODEL_FILE")
        Log.d(TAG, "Input size: ${INPUT_WIDTH}x${INPUT_HEIGHT}")
        Log.d(TAG, "Class count: $CLASS_COUNT")
        Log.d(TAG, "Class labels: $CLASS_LABELS")

        environment = OrtEnvironment.getEnvironment()
        Log.d(TAG, "OrtEnvironment created")

        // Step 1: Load model from assets
        Log.d(TAG, "[1/5] Loading model from assets...")
        val modelBytes = try {
            loadModelFromAssets()
        } catch (e: Exception) {
            Log.e(TAG, "[1/5] Failed to load model from assets", e)
            throw RuntimeException(
                "Model file not found or cannot be read. " +
                "Make sure '$MODEL_FILE' is in app/src/main/assets/", e
            )
        }
        Log.i(TAG, "[1/5] Model loaded from assets: ${modelBytes.size} bytes (${modelBytes.size / 1024 / 1024}MB)")

        // Step 2: Create session options
        Log.d(TAG, "[2/5] Creating session options...")
        val nnapiEnabled = useNnapi && isNnapiSupported()
        val options = OrtSession.SessionOptions().apply {
            if (nnapiEnabled) {
                Log.d(TAG, "Enabling NNAPI hardware acceleration")
                addConfigEntry("session.nnapi.enabled", "1")
                addConfigEntry("session.nnapi.use_nhwc", "0")
            } else {
                if (useNnapi) {
                    Log.d(TAG, "NNAPI requested but not supported on this device, using CPU")
                } else {
                    Log.d(TAG, "Using CPU execution")
                }
            }
            // Set optimization level
            setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
        }
        Log.i(TAG, "[2/5] Session options created (${if (nnapiEnabled) "NNAPI" else "CPU"} execution, ALL_OPT)")

        // Step 3: Create inference session
        Log.d(TAG, "[3/5] Creating ONNX session...")
        try {
            val startTime = System.currentTimeMillis()
            session = environment?.createSession(modelBytes, options)
            val elapsed = System.currentTimeMillis() - startTime
            Log.i(TAG, "[3/5] ONNX session created in ${elapsed}ms")
        } catch (e: Exception) {
            Log.e(TAG, "[3/5] Failed to create ONNX session", e)
            throw RuntimeException("Failed to create ONNX session. Model may be corrupted.\nError: ${e.javaClass.simpleName}: ${e.message}", e)
        }

        // Step 4: Get input/output info
        Log.d(TAG, "[4/5] Getting model input/output info...")
        val inputInfo = session?.inputNames
        val outputInfo = session?.outputNames

        Log.i(TAG, "[4/5] Model inputs: ${inputInfo?.toList()}")
        Log.i(TAG, "[4/5] Model outputs: ${outputInfo?.toList()}")

        // Log detailed input info
        session?.inputInfo?.forEach { (name, info) ->
            Log.d(TAG, "Input '$name': ${info}")
        }

        // Log detailed output info
        session?.outputInfo?.forEach { (name, info) ->
            Log.d(TAG, "Output '$name': ${info}")
        }

        inputName = inputInfo?.firstOrNull()
        outputName = outputInfo?.firstOrNull()

        // Validate input/output
        if (inputName == null || outputName == null) {
            Log.e(TAG, "[4/5] Model has invalid input/output structure")
            throw RuntimeException("Model has invalid input/output structure")
        }

        // Step 5: Complete initialization
        Log.d(TAG, "[5/5] Finalizing initialization...")
        isInitialized = true
        Log.i(TAG, "[5/5] Initialization complete!")
        Log.i(TAG, "========== ONNX Model Ready ==========")
        Log.i(TAG, "Input name: $inputName")
        Log.i(TAG, "Output name: $outputName")
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

            if (session == null || environment == null) {
                Log.e(TAG, "Model not initialized!")
                throw IllegalStateException("Model not initialized. Call initialize() first.")
            }

            try {
                // Step 1: Load and preprocess image
                Log.d(TAG, "[1/4] Loading image...")
                val startTime = System.currentTimeMillis()

                val inputStream: InputStream = context.contentResolver.openInputStream(imageUri)
                    ?: throw IllegalArgumentException("Cannot open image URI")

                val preprocessedInput = preprocessImage(inputStream)
                inputStream.close()

                Log.d(TAG, "[1/4] Image preprocessed in ${System.currentTimeMillis() - startTime}ms")

                // Step 2: Create ONNX tensor
                Log.d(TAG, "[2/4] Creating input tensor...")
                val inputTensor = OnnxTensor.createTensor(
                    environment!!,
                    preprocessedInput,
                    longArrayOf(1, 3, INPUT_HEIGHT.toLong(), INPUT_WIDTH.toLong()),
                    OnnxJavaType.FLOAT
                )
                Log.d(TAG, "[2/4] Input tensor created")

                // Step 3: Run inference
                Log.d(TAG, "[3/4] Running inference...")
                val inferenceStart = System.currentTimeMillis()

                val inputs = mapOf(inputName!! to inputTensor)
                val outputs = session?.run(inputs)

                val inferenceTime = System.currentTimeMillis() - inferenceStart
                Log.i(TAG, "[3/4] Inference completed in ${inferenceTime}ms")

                // Step 4: Extract output
                Log.d(TAG, "[4/4] Processing output...")
                val rawOutput = outputs?.get(0)?.value
                Log.d(TAG, "Raw output type: ${rawOutput?.javaClass?.canonicalName}")

                // Log detailed output structure
                logOutputStructure(rawOutput)

                val probabilities = extractOutput(rawOutput)

                inputTensor.close()
                outputs?.close()

                // Step 5: Process results
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
     * Log detailed output structure for debugging.
     */
    private fun logOutputStructure(rawOutput: Any?, depth: Int = 0) {
        val indent = "  ".repeat(depth)
        if (rawOutput == null) {
            Log.d(TAG, "${indent}Output is null")
            return
        }

        Log.d(TAG, "${indent}Type: ${rawOutput.javaClass.canonicalName}")

        when (rawOutput) {
            is FloatArray -> {
                Log.d(TAG, "${indent}FloatArray[${rawOutput.size}]: [${rawOutput.take(10).joinToString(", ") { "%.4f".format(it) }}...]")
                // Log ALL raw values
                Log.d(TAG, "========== RAW OUTPUT START ==========")
                rawOutput.forEachIndexed { index, value ->
                    Log.d(TAG, "output[$index] = $value")
                }
                Log.d(TAG, "========== RAW OUTPUT END ==========")
            }
            is Array<*> -> {
                Log.d(TAG, "${indent}Array[${rawOutput.size}]")
                rawOutput.forEachIndexed { index, element ->
                    Log.d(TAG, "${indent}[$index]:")
                    if (depth < 3) {
                        logOutputStructure(element, depth + 1)
                    } else {
                        Log.d(TAG, "${indent}  ${element?.javaClass?.canonicalName}")
                    }
                }
            }
            else -> {
                Log.d(TAG, "${indent}Value: $rawOutput")
            }
        }
    }

    /**
     * Extract probabilities from various ONNX output formats.
     * Handles: 1D [F], 2D [[F]], 3D [[[F]]] arrays
     */
    private fun extractOutput(rawOutput: Any?): FloatArray {
        Log.d(TAG, "Extracting output from type: ${rawOutput?.javaClass?.canonicalName}")

        // Handle 1D FloatArray directly
        if (rawOutput is FloatArray) {
            Log.d(TAG, "1D FloatArray shape: ${rawOutput.size}")
            return rawOutput
        }

        // Handle arrays using componentType to avoid type erasure issues
        if (rawOutput is Array<*>) {
            return handleNestedArray(rawOutput)
        }

        Log.e(TAG, "Unexpected output type: ${rawOutput?.javaClass?.canonicalName}")
        throw IllegalArgumentException("Unexpected output type: ${rawOutput?.javaClass?.canonicalName}")
    }

    /**
     * Recursively handle nested arrays (2D or 3D) to extract FloatArray.
     */
    private fun handleNestedArray(arr: Array<*>): FloatArray {
        Log.d(TAG, "Array size: ${arr.size}, component type: ${arr.javaClass.componentType}")

        if (arr.isEmpty()) {
            throw IllegalArgumentException("Empty array output")
        }

        val firstElement = arr[0]

        // If first element is FloatArray, this is 2D: [[F]]
        if (firstElement is FloatArray) {
            Log.d(TAG, "2D array detected: [${arr.size}, ${firstElement.size}]")

            // Standard classification: [1, num_classes] or just [num_classes]
            if (arr.size == 1) {
                Log.d(TAG, "Single batch - returning first row: ${firstElement.size} elements")
                return firstElement
            }

            // Multiple rows - take first row or average
            Log.d(TAG, "Multiple batches detected, using first batch")
            return firstElement
        }

        // If first element is also Array<*>, this is 3D or deeper: [[[F]]]
        if (firstElement is Array<*>) {
            Log.d(TAG, "3D array detected: [${arr.size}, ${firstElement.size}, ...]")

            if (firstElement.isEmpty()) {
                throw IllegalArgumentException("Empty middle dimension in 3D array")
            }

            val innerElement = firstElement[0]
            Log.d(TAG, "Inner element type: ${innerElement?.javaClass?.canonicalName}")

            // If inner is FloatArray, we have [batch, predictions, features]
            if (innerElement is FloatArray) {
                val numFeatures = innerElement.size
                val numPredictions = firstElement.size
                Log.d(TAG, "3D array shape: [${arr.size}, $numPredictions, $numFeatures]")
                Log.d(TAG, "Total features: $numFeatures, Expected classes: $CLASS_COUNT")

                if (numFeatures == CLASS_COUNT) {
                    // Format: [1, N, 3] - N predictions with 3 class scores each
                    // Take the prediction with highest confidence
                    val classScores = FloatArray(CLASS_COUNT)
                    var maxIdx = 0
                    var maxVal = Float.NEGATIVE_INFINITY

                    // Find the class with highest score across all predictions
                    for (element in arr) {
                        if (element is Array<*>) {
                            for (pred in element) {
                                if (pred is FloatArray) {
                                    for (i in pred.indices) {
                                        if (pred[i] > maxVal) {
                                            maxVal = pred[i]
                                            maxIdx = i
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Create one-hot encoding for the best class
                    classScores[maxIdx] = 1f
                    Log.d(TAG, "3D classification result: class $maxIdx (${CLASS_LABELS.getOrElse(maxIdx) { "Unknown" }}) with score $maxVal")
                    return classScores
                } else if (numFeatures > CLASS_COUNT) {
                    // Model outputs more features than classes - try global pooling or aggregation
                    Log.d(TAG, "Model output has more features ($numFeatures) than classes ($CLASS_COUNT)")
                    Log.d(TAG, "Attempting to aggregate using global average pooling...")

                    // Check if numFeatures is divisible by CLASS_COUNT
                    if (numFeatures % CLASS_COUNT == 0) {
                        val featuresPerClass = numFeatures / CLASS_COUNT
                        Log.d(TAG, "Features per class: $featuresPerClass")

                        // Aggregate features per class using global average pooling
                        val classScores = FloatArray(CLASS_COUNT)

                        // Get the first prediction batch
                        @Suppress("UNCHECKED_CAST")
                        val predictions = arr[0] as Array<*>

                        for (pred in predictions) {
                            if (pred is FloatArray) {
                                for (classIdx in 0 until CLASS_COUNT) {
                                    var sum = 0f
                                    var count = 0
                                    // Aggregate features for this class
                                    for (i in classIdx * featuresPerClass until (classIdx + 1) * featuresPerClass) {
                                        if (i < pred.size) {
                                            sum += pred[i]
                                            count++
                                        }
                                    }
                                    val avg = if (count > 0) sum / count else 0f
                                    if (avg > classScores[classIdx]) {
                                        classScores[classIdx] = avg
                                    }
                                }
                                break // Use first prediction only
                            }
                        }

                        Log.d(TAG, "Aggregated scores: ${classScores.joinToString()}")
                        return classScores
                    } else {
                        // Try taking first CLASS_COUNT elements
                        Log.d(TAG, "Output size not divisible by CLASS_COUNT, taking first $CLASS_COUNT elements")
                        return innerElement.take(CLASS_COUNT).toFloatArray()
                    }
                } else {
                    throw IllegalArgumentException(
                        "Model output has $numFeatures features, expected $CLASS_COUNT classes"
                    )
                }
            }

            // Try to flatten and extract
            Log.d(TAG, "Attempting to extract from nested structure...")
            return extractFromNested3D(arr)
        }

        throw IllegalArgumentException("Unsupported array structure with element type: ${firstElement?.javaClass}")
    }

    /**
     * Extract class scores from nested 3D array structure.
     * Handles cases where structure might be Array<Array<Array<*>>>
     */
    private fun extractFromNested3D(arr: Array<*>): FloatArray {
        // Try to find FloatArray at any depth
        val floatArrays = mutableListOf<FloatArray>()

        for (element in arr) {
            if (element is FloatArray) {
                floatArrays.add(element)
            } else if (element is Array<*>) {
                // Recurse into middle dimension
                for (inner in element) {
                    if (inner is FloatArray) {
                        floatArrays.add(inner)
                    } else if (inner is Array<*>) {
                        // Recurse into inner dimension
                        for (deepInner in inner) {
                            if (deepInner is FloatArray) {
                                floatArrays.add(deepInner)
                            }
                        }
                    }
                }
            }
        }

        if (floatArrays.isEmpty()) {
            throw IllegalArgumentException("No FloatArray found in nested structure")
        }

        Log.d(TAG, "Found ${floatArrays.size} float arrays in nested structure")

        // Use the first array that matches our expected class count
        val matchingArray = floatArrays.find { it.size == CLASS_COUNT }
            ?: floatArrays.first()

        Log.d(TAG, "Using array with ${matchingArray.size} elements")
        return matchingArray
    }

    /**
     * Preprocess image for model input.
     * Converts image to [1, 3, 480, 480] float32 tensor with values in [0,1]
     */
    private fun preprocessImage(inputStream: InputStream): ByteBuffer {
        // Load bitmap
        val bitmap = BitmapFactory.decodeStream(inputStream)
            ?: throw IllegalArgumentException("Failed to decode image")

        Log.d(TAG, "Original bitmap: ${bitmap.width}x${bitmap.height}")

        // Resize to model input size
        val resized = Bitmap.createScaledBitmap(bitmap, INPUT_WIDTH, INPUT_HEIGHT, true)
        Log.d(TAG, "Resized to: ${resized.width}x${resized.height}")

        // Convert to float array [1, 3, 480, 480]
        val bufferSize = 1 * 3 * INPUT_HEIGHT * INPUT_WIDTH * 4 // 4 bytes per float
        val buffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder())

        // Convert to RGB and normalize to [0, 1]
        val pixels = IntArray(INPUT_WIDTH * INPUT_HEIGHT)
        resized.getPixels(pixels, 0, INPUT_WIDTH, 0, 0, INPUT_WIDTH, INPUT_HEIGHT)

        for (y in 0 until INPUT_HEIGHT) {
            for (x in 0 until INPUT_WIDTH) {
                val pixel = pixels[y * INPUT_WIDTH + x]
                val r = ((pixel shr 16) and 0xFF) / 255f
                val g = ((pixel shr 8) and 0xFF) / 255f
                val b = (pixel and 0xFF) / 255f

                // Store as CHW format: [batch, channel, height, width]
                buffer.putFloat(r) // Red channel
                buffer.putFloat(g) // Green channel
                buffer.putFloat(b) // Blue channel
            }
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

        // Create prediction list
        val predictions = softmaxProbs.mapIndexed { index, prob ->
            com.example.spongebob.viewmodel.Prediction(
                className = CLASS_LABELS.getOrElse(index) { "Class $index" },
                confidence = prob
            )
        }.sortedByDescending { it.confidence }

        val actuallyUsingNnapi = useNnapi && isNnapiSupported()
        Log.i(TAG, "Execution provider: ${if (actuallyUsingNnapi) "NNAPI (Hardware)" else "CPU"}")

        return com.example.spongebob.viewmodel.ClassificationResult(
            className = predictions.first().className,
            confidence = predictions.first().confidence,
            allPredictions = predictions,
            useNnapi = actuallyUsingNnapi
        )
    }

    /**
     * Apply softmax to convert logits to probabilities.
     */
    private fun softmax(logits: FloatArray): FloatArray {
        val max = logits.maxOrNull() ?: 0f
        val exp = logits.map { Math.exp((it - max).toDouble()).toFloat() }
        val sum = exp.sum()
        return exp.map { it / sum }.toFloatArray()
    }

    /**
     * Load model from assets.
     */
    private fun loadModelFromAssets(): ByteArray {
        Log.d(TAG, "Attempting to open: $MODEL_FILE")
        val inputStream = context.assets.open(MODEL_FILE)
        val bytes = inputStream.readBytes()
        inputStream.close()
        Log.d(TAG, "Successfully read ${bytes.size} bytes")
        return bytes
    }

    /**
     * Release resources.
     */
    fun close() {
        Log.d(TAG, "Closing ONNX model resources")
        session?.close()
        environment?.close()
        session = null
        environment = null
        isInitialized = false
        Log.d(TAG, "ONNX model resources closed")
    }
}
