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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import com.example.spongebob.ui.theme.BubbleWhite
import com.example.spongebob.ui.theme.KrabRed
import com.example.spongebob.ui.theme.OceanBlue
import com.example.spongebob.ui.theme.SpongeYellow
import com.example.spongebob.ui.theme.SpongeYellowDark
import com.example.spongebob.viewmodel.ClassificationUiState
import com.example.spongebob.viewmodel.ClassificationViewModel
import kotlinx.coroutines.launch
import com.example.spongebob.ui.theme.DeepSea
import com.example.spongebob.ui.theme.PatrickPink
import com.example.spongebob.ui.theme.SeaFoam
import com.example.spongebob.ui.theme.SquidwardTeal
import com.example.spongebob.viewmodel.SettingsViewModel

// ==================== UNDERWATER BACKGROUND WITH BUBBLES ====================
@Composable
fun UnderwaterBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        // Animated bubbles
        UnderwaterBubbles()

        // Content
        content()
    }
}

@Composable
fun UnderwaterBubbles() {
    // Create multiple animated bubbles
    val bubbleCount = 8

    Box(modifier = Modifier.fillMaxSize()) {
        repeat(bubbleCount) { index ->
            val xPos = ((index * 120) % 400).dp
            val yPos = ((index * 80) % 600).dp
            val sizeVal = (20 + (index % 3) * 10).dp

            Bubble(
                modifier = Modifier
                    .offset(x = xPos, y = yPos)
                    .size(sizeVal),
                delay = index * 300L
            )
        }
    }
}

@Composable
fun Bubble(
    modifier: Modifier = Modifier,
    delay: Long = 0
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bubble")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000 + delay.toInt(), delayMillis = delay.toInt()),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubbleScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500 + delay.toInt(), delayMillis = delay.toInt()),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubbleAlpha"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(CircleShape)
            .background(BubbleWhite.copy(alpha = alpha))
    )
}

// ==================== SPONGEBOB TITLE ====================
@Composable
fun SpongeBobTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        letterSpacing = 0.5.sp
    )
}

// ==================== SPONGEBOB BUTTON ====================
@Composable
fun SpongeBobButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    text: String
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = SpongeYellow,
            contentColor = DeepSea,
            disabledContainerColor = SpongeYellow.copy(alpha = 0.5f),
            disabledContentColor = DeepSea.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(CornerSize(16.dp)),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==================== INPUT SCREEN ====================
@Composable
fun InputScreen(
    uiState: ClassificationUiState,
    onImageSelected: (android.net.Uri) -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToInference: () -> Unit,
    onNavigateToSettings: () -> Unit,
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
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = KrabRed
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = snackbarData.visuals.message,
                            modifier = Modifier.padding(16.dp),
                            color = BubbleWhite
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        UnderwaterBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // App icon/title
                    Card(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = SpongeYellow
                        ),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🧽",
                                fontSize = 40.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Title with settings button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpongeBobTitle(
                            text = "SpongeBob Classifier",
                            modifier = Modifier.weight(1f)
                        )

                        // Settings button
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        ) {
                            Text(
                                text = "⚙️",
                                fontSize = 24.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Identify underwater creatures!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Image preview card
                    Card(
                        modifier = Modifier.size(280.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            3.dp,
                            OceanBlue.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.imageUri != null) {
                                AsyncImage(
                                    model = uiState.imageUri,
                                    contentDescription = "Selected image",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "📸",
                                        fontSize = 48.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No image selected",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Source buttons row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpongeBobButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            enabled = uiState.isModelReady,
                            text = "📷 Gallery",
                            modifier = Modifier.weight(1f)
                        )

                        SpongeBobButton(
                            onClick = onNavigateToCamera,
                            enabled = uiState.isModelReady,
                            text = "📸 Camera",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Model loading status
                    if (!uiState.isModelReady) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = OceanBlue,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Loading AI model...",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Text(
                            text = "✓ AI Ready - Select an image source",
                            fontSize = 12.sp,
                            color = PatrickPink.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Classify button
                    SpongeBobButton(
                        onClick = onNavigateToInference,
                        enabled = uiState.imageUri != null && !uiState.isProcessing && uiState.isModelReady,
                        text = if (uiState.isProcessing) "⏳ Processing..." else "✨ Classify",
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
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

            // Draw SpongeBob yellow border around focus box
            drawRoundRect(
                color = SpongeYellow,
                size = androidx.compose.ui.geometry.Size(boxSize, boxSize),
                topLeft = androidx.compose.ui.geometry.Offset(boxLeft, boxTop),
                style = Stroke(width = 4.dp.toPx())
            )

            // Draw corner brackets for better visibility
            val cornerLength = boxSize * 0.1f
            val cornerThickness = 8.dp.toPx()

            // Top-left corner
            drawLine(
                color = SpongeYellow,
                start = androidx.compose.ui.geometry.Offset(boxLeft, boxTop + cornerLength),
                end = androidx.compose.ui.geometry.Offset(boxLeft, boxTop),
                strokeWidth = cornerThickness
            )
            drawLine(
                color = SpongeYellow,
                start = androidx.compose.ui.geometry.Offset(boxLeft, boxTop),
                end = androidx.compose.ui.geometry.Offset(boxLeft + cornerLength, boxTop),
                strokeWidth = cornerThickness
            )

            // Top-right corner
            drawLine(
                color = SpongeYellow,
                start = androidx.compose.ui.geometry.Offset(boxLeft + boxSize - cornerLength, boxTop),
                end = androidx.compose.ui.geometry.Offset(boxLeft + boxSize, boxTop),
                strokeWidth = cornerThickness
            )
            drawLine(
                color = SpongeYellow,
                start = androidx.compose.ui.geometry.Offset(boxLeft + boxSize, boxTop),
                end = androidx.compose.ui.geometry.Offset(boxLeft + boxSize, boxTop + cornerLength),
                strokeWidth = cornerThickness
            )

            // Bottom-left corner
            drawLine(
                color = SpongeYellow,
                start = androidx.compose.ui.geometry.Offset(boxLeft, boxTop + boxSize - cornerLength),
                end = androidx.compose.ui.geometry.Offset(boxLeft, boxTop + boxSize),
                strokeWidth = cornerThickness
            )
            drawLine(
                color = SpongeYellow,
                start = androidx.compose.ui.geometry.Offset(boxLeft, boxTop + boxSize),
                end = androidx.compose.ui.geometry.Offset(boxLeft + cornerLength, boxTop + boxSize),
                strokeWidth = cornerThickness
            )

            // Bottom-right corner
            drawLine(
                color = SpongeYellow,
                start = androidx.compose.ui.geometry.Offset(boxLeft + boxSize - cornerLength, boxTop + boxSize),
                end = androidx.compose.ui.geometry.Offset(boxLeft + boxSize, boxTop + boxSize),
                strokeWidth = cornerThickness
            )
            drawLine(
                color = SpongeYellow,
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
        UnderwaterBackground(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📷",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Camera Permission Required",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please grant camera permission to take photos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        SpongeBobButton(
                            onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                            text = "Grant Permission"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onBack,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = OceanBlue
                            )
                        ) {
                            Text("Back")
                        }
                    }
                }
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

        // Top bar
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            FilledIconButton(
                onClick = onBack,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = SpongeYellow.copy(alpha = 0.9f),
                    contentColor = DeepSea
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
            }
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
        // Capture button with yellow SpongeBob style
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
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = SpongeYellow,
                contentColor = DeepSea
            ),
            elevation = ButtonDefaults.buttonElevation(8.dp)
        ) {
            // Inner circle
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(DeepSea)
            )
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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = CircleShape
            ) {
                IconButton(
                    onClick = onCancel,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = KrabRed.copy(alpha = 0.9f),
                        contentColor = BubbleWhite
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cancel",
                        tint = BubbleWhite
                    )
                }
            }

            Card(
                shape = CircleShape
            ) {
                IconButton(
                    onClick = {
                        bitmap?.let {
                            val croppedUri = cropAndSaveImage(context, it)
                            croppedUri?.let { uri -> onConfirm(uri) }
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = OceanBlue,
                        contentColor = BubbleWhite
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Confirm",
                        tint = BubbleWhite
                    )
                }
            }
        }

        // Instructions
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Position image within the frame",
                modifier = Modifier.padding(16.dp),
                color = BubbleWhite,
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

            // Draw SpongeBob yellow border around crop rectangle
            drawRoundRect(
                color = SpongeYellow,
                topLeft = androidx.compose.ui.geometry.Offset(cropLeft, cropTop),
                size = androidx.compose.ui.geometry.Size(cropSize, cropSize),
                style = Stroke(width = 4.dp.toPx())
            )

            // Draw corner brackets
            val cornerLength = cropSize * 0.08f
            val cornerThickness = 6.dp.toPx()

            // Top-left corner
            drawLine(
                color = SpongeYellow,
                start = androidx.compose.ui.geometry.Offset(cropLeft, cropTop + cornerLength),
                end = androidx.compose.ui.geometry.Offset(cropLeft, cropTop),
                strokeWidth = cornerThickness
            )
            drawLine(
                color = SpongeYellow,
                start = androidx.compose.ui.geometry.Offset(cropLeft, cropTop),
                end = androidx.compose.ui.geometry.Offset(cropLeft + cornerLength, cropTop),
                strokeWidth = cornerThickness
            )

            // Top-right corner
            drawLine(
                color = SpongeYellow,
                start = androidx.compose.ui.geometry.Offset(cropLeft + cropSize - cornerLength, cropTop),
                end = androidx.compose.ui.geometry.Offset(cropLeft + cropSize, cropTop),
                strokeWidth = cornerThickness
            )
            drawLine(
                color = SpongeYellow,
                start = androidx.compose.ui.geometry.Offset(cropLeft + cropSize, cropTop),
                end = androidx.compose.ui.geometry.Offset(cropLeft + cropSize, cropTop + cornerLength),
                strokeWidth = cornerThickness
            )

            // Bottom-left corner
            drawLine(
                color = SpongeYellow,
                start = androidx.compose.ui.geometry.Offset(cropLeft, cropTop + cropSize - cornerLength),
                end = androidx.compose.ui.geometry.Offset(cropLeft, cropTop + cropSize),
                strokeWidth = cornerThickness
            )
            drawLine(
                color = SpongeYellow,
                start = androidx.compose.ui.geometry.Offset(cropLeft, cropTop + cropSize),
                end = androidx.compose.ui.geometry.Offset(cropLeft + cornerLength, cropTop + cropSize),
                strokeWidth = cornerThickness
            )

            // Bottom-right corner
            drawLine(
                color = SpongeYellow,
                start = androidx.compose.ui.geometry.Offset(cropLeft + cropSize - cornerLength, cropTop + cropSize),
                end = androidx.compose.ui.geometry.Offset(cropLeft + cropSize, cropTop + cropSize),
                strokeWidth = cornerThickness
            )
            drawLine(
                color = SpongeYellow,
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

    UnderwaterBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated bubble effect
            Card(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = SpongeYellow
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse"
                    )
                    Text(
                        text = "🔍",
                        fontSize = 48.sp,
                        modifier = Modifier.scale(scale)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Analyzing...",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Image preview
            if (uiState.imageUri != null) {
                Card(
                    modifier = Modifier.size(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
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
                CircularProgressIndicator(
                    color = OceanBlue,
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Running AI classification...",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = KrabRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Error occurred",
                        modifier = Modifier.padding(16.dp),
                        color = BubbleWhite,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ==================== RESULT SCREEN ====================
@Composable
fun ResultScreen(
    uiState: ClassificationUiState,
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNewImage: () -> Unit
) {
    val result = uiState.result
    val showInferenceTime by settingsViewModel.showInferenceTime.collectAsState()

    UnderwaterBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Result icon
            Card(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = PatrickPink
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎉",
                        fontSize = 40.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SpongeBobTitle(
                text = "Classification Result"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Image preview
            if (uiState.imageUri != null) {
                Card(
                    modifier = Modifier.size(200.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        3.dp,
                        OceanBlue.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        2.dp,
                        SpongeYellowDark.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Predicted Class",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = result.className,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = OceanBlue
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Confidence meter
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Confidence",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${(result.confidence * 100).toInt()}%",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    result.confidence >= 0.8f -> PatrickPink
                                    result.confidence >= 0.5f -> SpongeYellowDark
                                    else -> KrabRed
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Confidence bar
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            val barWidth = size.width * result.confidence
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.2f),
                                size = size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                            )
                            drawRoundRect(
                                color = when {
                                    result.confidence >= 0.8f -> PatrickPink
                                    result.confidence >= 0.5f -> SpongeYellowDark
                                    else -> KrabRed
                                },
                                size = androidx.compose.ui.geometry.Size(barWidth, size.height),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Inference time and hardware display
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Hardware indicator
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (result.useNnapi) PatrickPink.copy(alpha = 0.15f) else OceanBlue.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (result.useNnapi) "⚡" else "💻",
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (result.useNnapi) "NNAPI" else "CPU",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (result.useNnapi) PatrickPink else OceanBlue
                            )
                        }
                    }

                    // Inference time (conditional based on settings)
                    if (showInferenceTime && result.inferenceTimeMillis > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = OceanBlue.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⏱️",
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "${result.inferenceTimeMillis}ms",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = OceanBlue
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // All predictions list
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "All Predictions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        result.allPredictions.forEach { prediction ->
                            PredictionRow(prediction)
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = OceanBlue
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, OceanBlue.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Home")
                    }

                    SpongeBobButton(
                        onClick = onNewImage,
                        modifier = Modifier.weight(1f),
                        text = "📷 New Image"
                    )
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
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = prediction.className,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Confidence indicator
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    prediction.confidence >= 0.8f -> PatrickPink.copy(alpha = 0.2f)
                    prediction.confidence >= 0.5f -> SpongeYellowDark.copy(alpha = 0.2f)
                    else -> KrabRed.copy(alpha = 0.2f)
                }
            )
        ) {
            Text(
                text = "${(prediction.confidence * 100).toInt()}%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    prediction.confidence >= 0.8f -> PatrickPink
                    prediction.confidence >= 0.5f -> SpongeYellowDark
                    else -> KrabRed
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

// ==================== NNAPI PROMPT SCREEN ====================
@Composable
fun NnapiPromptScreen(
    onEnable: () -> Unit,
    onSkip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onSkip) // Dismiss on background click
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(SpongeYellow.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡",
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Enable Hardware Acceleration?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = "Your device supports NNAPI for faster AI inference. This can significantly speed up image classification.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Warning card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = KrabRed.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 16.sp
                        )
                        Text(
                            text = "May cause issues on some devices. You can disable this in Settings later.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    ) {
                        Text("Skip", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = onEnable,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpongeYellow,
                            contentColor = DeepSea
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Enable", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
