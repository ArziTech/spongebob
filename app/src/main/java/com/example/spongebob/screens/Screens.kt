package com.example.spongebob.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.spongebob.R
import com.example.spongebob.navigation.Camera
import com.example.spongebob.navigation.Inference
import com.example.spongebob.navigation.Input
import com.example.spongebob.navigation.Result
import com.example.spongebob.viewmodel.ClassificationUiState
import com.example.spongebob.viewmodel.ClassificationViewModel
import kotlinx.coroutines.launch

// ==================== INPUT SCREEN ====================
@Composable
fun InputScreen(
    uiState: ClassificationUiState,
    onImageSelected: (android.net.Uri) -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToInference: () -> Unit,
    onClearError: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { onImageSelected(it) }
    }

    // Snackbar host for error messages
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                onClearError()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Title
                Text(
                    text = "Image Classification",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Image preview or placeholder
                if (uiState.imageUri != null) {
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = uiState.imageUri,
                            contentDescription = "Selected image",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No image selected",
                            color = Color.DarkGray
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Source buttons
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        enabled = uiState.isModelReady
                    ) {
                        Text("Gallery")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = onNavigateToCamera,
                        enabled = uiState.isModelReady
                    ) {
                        Text("Camera")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Model loading status
                if (!uiState.isModelReady) {
                    Text(
                        text = "Loading AI model...",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = "Select image source",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Next button
                Button(
                    onClick = onNavigateToInference,
                    enabled = uiState.imageUri != null && !uiState.isProcessing && uiState.isModelReady
                ) {
                    if (uiState.isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Next: Classify")
                    }
                }
            }

            // Loading indicator in top right corner
            if (!uiState.isModelReady) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        )
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Loading...",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ==================== CAMERA SCREEN ====================

// Camera overlay with focus box (70% of screen) and darkened outside area
@Composable
fun CameraOverlay(
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Box(modifier = modifier) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val screenWidth = size.width
            val screenHeight = size.height

            // Focus box is 70% of the smaller dimension
            val boxSize = minOf(screenWidth, screenHeight) * 0.7f
            val boxLeft = (screenWidth - boxSize) / 2
            val boxTop = (screenHeight - boxSize) / 2

            // Draw semi-transparent overlay outside the focus box
            // We'll draw four rectangles around the center box
            val overlayColor = android.graphics.Color.BLACK

            // Top rectangle
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = androidx.compose.ui.geometry.Size(screenWidth, boxTop),
                topLeft = androidx.compose.ui.geometry.Offset(0f, 0f)
            )

            // Bottom rectangle
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = androidx.compose.ui.geometry.Size(screenWidth, screenHeight - boxTop - boxSize),
                topLeft = androidx.compose.ui.geometry.Offset(0f, boxTop + boxSize)
            )

            // Left rectangle
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = androidx.compose.ui.geometry.Size(boxLeft, boxSize),
                topLeft = androidx.compose.ui.geometry.Offset(0f, boxTop)
            )

            // Right rectangle
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = androidx.compose.ui.geometry.Size(screenWidth - boxLeft - boxSize, boxSize),
                topLeft = androidx.compose.ui.geometry.Offset(boxLeft + boxSize, boxTop)
            )

            // Draw white border around focus box
            drawRoundRect(
                color = Color.White,
                size = androidx.compose.ui.geometry.Size(boxSize, boxSize),
                topLeft = androidx.compose.ui.geometry.Offset(boxLeft, boxTop),
                style = Stroke(width = 4.dp.toPx())
            )

            // Draw corner brackets for better visibility
            val cornerLength = boxSize * 0.1f
            val cornerThickness = 8.dp.toPx()

            // Top-left corner
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(boxLeft, boxTop + cornerLength),
                end = androidx.compose.ui.geometry.Offset(boxLeft, boxTop),
                strokeWidth = cornerThickness
            )
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(boxLeft, boxTop),
                end = androidx.compose.ui.geometry.Offset(boxLeft + cornerLength, boxTop),
                strokeWidth = cornerThickness
            )

            // Top-right corner
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(boxLeft + boxSize - cornerLength, boxTop),
                end = androidx.compose.ui.geometry.Offset(boxLeft + boxSize, boxTop),
                strokeWidth = cornerThickness
            )
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(boxLeft + boxSize, boxTop),
                end = androidx.compose.ui.geometry.Offset(boxLeft + boxSize, boxTop + cornerLength),
                strokeWidth = cornerThickness
            )

            // Bottom-left corner
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(boxLeft, boxTop + boxSize - cornerLength),
                end = androidx.compose.ui.geometry.Offset(boxLeft, boxTop + boxSize),
                strokeWidth = cornerThickness
            )
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(boxLeft, boxTop + boxSize),
                end = androidx.compose.ui.geometry.Offset(boxLeft + cornerLength, boxTop + boxSize),
                strokeWidth = cornerThickness
            )

            // Bottom-right corner
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(boxLeft + boxSize - cornerLength, boxTop + boxSize),
                end = androidx.compose.ui.geometry.Offset(boxLeft + boxSize, boxTop + boxSize),
                strokeWidth = cornerThickness
            )
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(boxLeft + boxSize, boxTop + boxSize - cornerLength),
                end = androidx.compose.ui.geometry.Offset(boxLeft + boxSize, boxTop + boxSize),
                strokeWidth = cornerThickness
            )
        }
    }
}

@Composable
fun CameraScreen(
    onImageCaptured: (android.net.Uri) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        // Permission denied screen
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Camera permission is required",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Grant Permission")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack) {
                Text("Back")
            }
        }
        return
    }

    // Camera preview
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onImageCaptured = onImageCaptured
        )

        // Focus box overlay
        CameraOverlay(modifier = Modifier.fillMaxSize())

        // Back button
        Button(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Text("Back")
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onImageCaptured: (android.net.Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val imageCapture = remember { ImageCapture.Builder().build() }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    ) { view ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(view.surfaceProvider)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Capture button overlay
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        Button(
            onClick = {
                imageCapture.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(imageProxy: ImageProxy) {
                            try {
                                // Crop to center 480x480
                                val croppedBitmap = cropImageToCenter(imageProxy)

                                // Save to temp file
                                val outputFile = java.io.File(
                                    context.cacheDir,
                                    "photo_cropped_${System.currentTimeMillis()}.jpg"
                                )

                                java.io.FileOutputStream(outputFile).use { out ->
                                    croppedBitmap.compress(
                                        android.graphics.Bitmap.CompressFormat.JPEG,
                                        95,
                                        out
                                    )
                                }

                                onImageCaptured(android.net.Uri.fromFile(outputFile))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                imageProxy.close()
                            }
                        }

                        override fun onError(exc: ImageCaptureException) {
                            exc.printStackTrace()
                        }
                    }
                )
            },
            modifier = Modifier
                .padding(32.dp)
                .size(80.dp),
            shape = CircleShape
        ) {
            // Capture button (empty circle)
        }
    }
}

/**
 * Crop image to center 480x480 pixels
 */
private fun cropImageToCenter(imageProxy: ImageProxy): android.graphics.Bitmap {
    val targetSize = 480

    // Rotate if needed (based on image rotation)
    val rotation = imageProxy.imageInfo.rotationDegrees
    val bitmap = if (rotation != 0) {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(rotation.toFloat())
        android.graphics.Bitmap.createBitmap(
            imageProxy.toBitmap(),
            0, 0,
            imageProxy.width,
            imageProxy.height,
            matrix,
            true
        )
    } else {
        imageProxy.toBitmap()
    }

    // Calculate center crop
    val width = bitmap.width
    val height = bitmap.height

    val cropSize = minOf(width, height)
    val x = (width - cropSize) / 2
    val y = (height - cropSize) / 2

    // Crop center square
    val croppedBitmap = android.graphics.Bitmap.createBitmap(
        bitmap,
        x, y,
        cropSize, cropSize
    )

    // Scale to exactly 480x480
    return android.graphics.Bitmap.createScaledBitmap(
        croppedBitmap,
        targetSize,
        targetSize,
        true
    )
}

// ==================== CROP SCREEN ====================
@Composable
fun CropScreen(
    imageUri: String,
    onConfirm: (android.net.Uri) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Load bitmap for cropping
    val bitmapState = remember(imageUri) {
        mutableStateOf<android.graphics.Bitmap?>(null)
    }

    LaunchedEffect(imageUri) {
        try {
            val inputStream = context.contentResolver.openInputStream(android.net.Uri.parse(imageUri))
            inputStream?.let { stream ->
                bitmapState.value = android.graphics.BitmapFactory.decodeStream(stream)
                stream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val bitmap = bitmapState.value

    Box(modifier = Modifier.fillMaxSize()) {
        // Image with pan/zoom gestures
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 5f)
                        offset += pan
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Image to crop",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
                contentScale = ContentScale.Fit
            )
        }

        // Fixed crop rectangle overlay (70% of screen)
        CropOverlay(modifier = Modifier.fillMaxSize())

        // Top buttons (Cancel/Confirm)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = onCancel) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    bitmap?.let {
                        val croppedUri = cropAndSaveImage(context, it)
                        croppedUri?.let { uri -> onConfirm(uri) }
                    }
                }
            ) {
                Text("Confirm")
            }
        }

        // Instructions
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
        ) {
            Text(
                text = "Position image within the frame",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Crop overlay - fixed square rectangle in center with dark overlay outside
@Composable
fun CropOverlay(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val screenWidth = size.width
            val screenHeight = size.height

            // Fixed crop rectangle is 70% of the smaller dimension
            val cropSize = minOf(screenWidth, screenHeight) * 0.7f
            val cropLeft = (screenWidth - cropSize) / 2
            val cropTop = (screenHeight - cropSize) / 2

            // Draw semi-transparent overlay outside the crop rectangle
            // Top rectangle
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.6f),
                topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(screenWidth, cropTop)
            )

            // Bottom rectangle
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.6f),
                topLeft = androidx.compose.ui.geometry.Offset(0f, cropTop + cropSize),
                size = androidx.compose.ui.geometry.Size(screenWidth, screenHeight - cropTop - cropSize)
            )

            // Left rectangle
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.6f),
                topLeft = androidx.compose.ui.geometry.Offset(0f, cropTop),
                size = androidx.compose.ui.geometry.Size(cropLeft, cropSize)
            )

            // Right rectangle
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.6f),
                topLeft = androidx.compose.ui.geometry.Offset(cropLeft + cropSize, cropTop),
                size = androidx.compose.ui.geometry.Size(screenWidth - cropLeft - cropSize, cropSize)
            )

            // Draw white border around crop rectangle
            drawRoundRect(
                color = Color.White,
                topLeft = androidx.compose.ui.geometry.Offset(cropLeft, cropTop),
                size = androidx.compose.ui.geometry.Size(cropSize, cropSize),
                style = Stroke(width = 4.dp.toPx())
            )

            // Draw corner brackets
            val cornerLength = cropSize * 0.08f
            val cornerThickness = 6.dp.toPx()

            // Top-left corner
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(cropLeft, cropTop + cornerLength),
                end = androidx.compose.ui.geometry.Offset(cropLeft, cropTop),
                strokeWidth = cornerThickness
            )
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(cropLeft, cropTop),
                end = androidx.compose.ui.geometry.Offset(cropLeft + cornerLength, cropTop),
                strokeWidth = cornerThickness
            )

            // Top-right corner
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(cropLeft + cropSize - cornerLength, cropTop),
                end = androidx.compose.ui.geometry.Offset(cropLeft + cropSize, cropTop),
                strokeWidth = cornerThickness
            )
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(cropLeft + cropSize, cropTop),
                end = androidx.compose.ui.geometry.Offset(cropLeft + cropSize, cropTop + cornerLength),
                strokeWidth = cornerThickness
            )

            // Bottom-left corner
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(cropLeft, cropTop + cropSize - cornerLength),
                end = androidx.compose.ui.geometry.Offset(cropLeft, cropTop + cropSize),
                strokeWidth = cornerThickness
            )
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(cropLeft, cropTop + cropSize),
                end = androidx.compose.ui.geometry.Offset(cropLeft + cornerLength, cropTop + cropSize),
                strokeWidth = cornerThickness
            )

            // Bottom-right corner
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(cropLeft + cropSize - cornerLength, cropTop + cropSize),
                end = androidx.compose.ui.geometry.Offset(cropLeft + cropSize, cropTop + cropSize),
                strokeWidth = cornerThickness
            )
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(cropLeft + cropSize, cropTop + cropSize - cornerLength),
                end = androidx.compose.ui.geometry.Offset(cropLeft + cropSize, cropTop + cropSize),
                strokeWidth = cornerThickness
            )
        }
    }
}

/**
 * Crop and save image to 480x480
 * Takes the center crop of the original image and resizes to 480x480
 */
private fun cropAndSaveImage(
    context: Context,
    originalBitmap: android.graphics.Bitmap
): android.net.Uri? {
    return try {
        val targetSize = 480

        // Take the center crop of the original image and resize to 480x480
        val width = originalBitmap.width
        val height = originalBitmap.height
        val cropSize = minOf(width, height)
        val x = (width - cropSize) / 2
        val y = (height - cropSize) / 2

        // Crop center square
        val croppedBitmap = android.graphics.Bitmap.createBitmap(
            originalBitmap,
            x, y,
            cropSize, cropSize
        )

        // Scale to exactly 480x480
        val finalBitmap = android.graphics.Bitmap.createScaledBitmap(
            croppedBitmap,
            targetSize,
            targetSize,
            true
        )

        // Save to temp file
        val outputFile = java.io.File(
            context.cacheDir,
            "crop_${System.currentTimeMillis()}.jpg"
        )

        java.io.FileOutputStream(outputFile).use { out ->
            finalBitmap.compress(
                android.graphics.Bitmap.CompressFormat.JPEG,
                95,
                out
            )
        }

        android.net.Uri.fromFile(outputFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// ==================== INFERENCE SCREEN ====================
@Composable
fun InferenceScreen(
    uiState: ClassificationUiState,
    onClassify: () -> Unit,
    onComplete: () -> Unit
) {
    LaunchedEffect(Unit) {
        // Auto-start classification when screen loads
        onClassify()
    }

    LaunchedEffect(uiState.result) {
        // Navigate to result when ready
        if (uiState.result != null) {
            onComplete()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Classifying...",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Image preview
        if (uiState.imageUri != null) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = uiState.imageUri,
                    contentDescription = "Selected image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        if (uiState.isProcessing) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Running AI model inference...",
                color = Color.Gray
            )
        } else if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage ?: "Error occurred",
                color = Color.Red,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==================== RESULT SCREEN ====================
@Composable
fun ResultScreen(
    uiState: ClassificationUiState,
    onBack: () -> Unit,
    onNewImage: () -> Unit
) {
    val result = uiState.result

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Classification Result",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Image preview
        if (uiState.imageUri != null) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = uiState.imageUri,
                    contentDescription = "Selected image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Main result
        if (result != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Text(
                    text = "Predicted Class:",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Text(
                    text = result.className,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Confidence: ${(result.confidence * 100).toInt()}%",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))

                // All predictions list
                Text(
                    text = "All Predictions:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                result.allPredictions.forEach { prediction ->
                    PredictionRow(prediction)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Button(onClick = onBack) {
                    Text("Back to Input")
                }
                Button(onClick = onNewImage) {
                    Text("New Image")
                }
            }
        }
    }
}

@Composable
fun PredictionRow(
    prediction: com.example.spongebob.viewmodel.Prediction
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = prediction.className,
            fontSize = 14.sp
        )
        Text(
            text = "${(prediction.confidence * 100).toInt()}%",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                prediction.confidence >= 0.8f -> Color(0xFF4CAF50)
                prediction.confidence >= 0.5f -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }
        )
    }
}
