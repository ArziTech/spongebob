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
import com.example.spongebob.model.TFLiteModelManager
import com.example.spongebob.model.ModelInfo
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

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

// ==================== MAIN MENU SCREEN ====================
@Composable
fun MainMenuScreen(
    currentModelName: String = "Small Classifier (3 classes)",
    onNavigateToDetect: () -> Unit,
    onNavigateToEvaluate: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToModelInfo: () -> Unit,
    onQuit: () -> Unit
) {
    UnderwaterBackground(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Settings button (top-right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    ) {
                        Text(
                            text = "⚙️",
                            fontSize = 28.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // App icon
                Card(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = SpongeYellow
                    ),
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🧽",
                            fontSize = 60.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // App title
                SpongeBobTitle(
                    text = "SpongeBob Classifier"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "AI Image Classification",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Current: $currentModelName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(44.dp))

                // Main Menu Options
                MainMenuOptionCard(
                    icon = "🔍",
                    title = "Deteksi",
                    description = "Identify objects using AI",
                    backgroundColor = OceanBlue,
                    onClick = onNavigateToDetect
                )

                Spacer(modifier = Modifier.height(16.dp))

                MainMenuOptionCard(
                    icon = "📊",
                    title = "Evaluate Model",
                    description = "Test accuracy with ground truth",
                    backgroundColor = PatrickPink,
                    onClick = onNavigateToEvaluate
                )

                Spacer(modifier = Modifier.height(16.dp))

                MainMenuOptionCard(
                    icon = "🧠",
                    title = "Model Info",
                    description = "View model architecture and details",
                    backgroundColor = SquidwardTeal,
                    onClick = onNavigateToModelInfo
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Quit button
                OutlinedButton(
                    onClick = onQuit,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(52.dp),
                    shape = RoundedCornerShape(CornerSize(16.dp)),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = KrabRed
                    )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quit",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MainMenuOptionCard(
    icon: String,
    title: String,
    description: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.15f)
        ),
        border = BorderStroke(
            2.dp,
            backgroundColor.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 40.sp
            )

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = backgroundColor
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
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
    onClearError: () -> Unit,
    onBackToMainMenu: () -> Unit = {}
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

                    // Title with back and settings buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back button
                        IconButton(
                            onClick = onBackToMainMenu,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = OceanBlue
                            )
                        }

                        SpongeBobTitle(
                            text = "Deteksi",
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

                // Performance metrics card
                if (showInferenceTime) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "📊 Performance Metrics",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Inference Time
                                MetricItem(
                                    icon = "⏱️",
                                    label = "Time",
                                    value = "${result.inferenceTimeMillis}ms",
                                    color = OceanBlue
                                )

                                // CPU Usage
                                MetricItem(
                                    icon = "💻",
                                    label = "CPU",
                                    value = "${result.cpuUsagePercent.toInt()}%",
                                    color = if (result.cpuUsagePercent > 80) KrabRed else PatrickPink
                                )

                                // Memory Usage
                                MetricItem(
                                    icon = "🧠",
                                    label = "Memory",
                                    value = "${result.memoryUsedMB}MB",
                                    color = SpongeYellowDark
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Hardware indicator
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (result.useNnapi) "⚡" else "💻",
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Running on ${if (result.useNnapi) "Hardware Accelerated" else "CPU"}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "•",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Total: ${result.memoryTotalMB}MB",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                } else {
                    // Simplified hardware indicator when metrics are hidden
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                                    text = if (result.useNnapi) "GPU" else "CPU",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (result.useNnapi) PatrickPink else OceanBlue
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

// ==================== METRIC ITEM ====================
@Composable
fun MetricItem(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

// ==================== GPU PROMPT SCREEN ====================
@Composable
fun GpuPromptScreen(
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
                    text = "Your device supports GPU for faster AI inference. This can significantly speed up image classification.",
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

// ==================== EVALUATION HOME SCREEN ====================
@Composable
fun EvaluationHomeScreen(
    viewModel: com.example.spongebob.viewmodel.EvaluationViewModel,
    onNavigateToInput: () -> Unit,
    onNavigateToHistory: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    UnderwaterBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = OceanBlue
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "📊 Model Evaluation",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanBlue
                    )

                    Spacer(modifier = Modifier.weight(1f))

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
            },
            floatingActionButton = {
                androidx.compose.material3.FloatingActionButton(
                    onClick = onNavigateToInput,
                    containerColor = SpongeYellow,
                    contentColor = DeepSea
                ) {
                    Icon(Icons.Default.Check, contentDescription = "New Evaluation")
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = OceanBlue)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Loading evaluation groups...")
                            }
                        }
                    }

                    uiState.availableGroups.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📁",
                                    fontSize = 64.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Evaluation Groups",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap + to create your first evaluation group",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    else -> {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    text = "Evaluation Groups",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }

                            uiState.availableGroups.forEach { group ->
                                val evaluationCount = uiState.groupCounts[group.groupId] ?: 0
                                val accuracy = uiState.groupAccuracy[group.groupId] ?: 0f

                                item {
                                    EvaluationGroupCard(
                                        group = group,
                                        evaluationCount = evaluationCount,
                                        accuracy = accuracy,
                                        onClick = { onNavigateToHistory(group.groupId) },
                                        onDelete = { viewModel.deleteGroup(group.groupId) }
                                    )
                                }
                            }

                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EvaluationGroupCard(
    group: com.example.spongebob.data.entity.EvaluationGroupEntity,
    evaluationCount: Int,
    accuracy: Float,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            2.dp,
            when {
                accuracy >= 0.8f -> PatrickPink.copy(alpha = 0.3f)
                accuracy >= 0.5f -> SpongeYellow.copy(alpha = 0.3f)
                else -> KrabRed.copy(alpha = 0.3f)
            }
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Group icon with accuracy indicator
                Card(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            accuracy >= 0.8f -> PatrickPink.copy(alpha = 0.2f)
                            accuracy >= 0.5f -> SpongeYellow.copy(alpha = 0.2f)
                            else -> KrabRed.copy(alpha = 0.2f)
                        }
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${(accuracy * 100).toInt()}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    accuracy >= 0.8f -> PatrickPink
                                    accuracy >= 0.5f -> SpongeYellow
                                    else -> KrabRed
                                }
                            )
                            Text(
                                text = "acc",
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = group.groupName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$evaluationCount evaluations",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    group.description?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Delete group",
                    tint = KrabRed
                )
            }
        }
    }
}

// ==================== EVALUATION INPUT SCREEN ====================
@Composable
fun EvaluationInputScreen(
    viewModel: com.example.spongebob.viewmodel.EvaluationViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showGroupDialog by remember { mutableStateOf(false) }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    UnderwaterBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = OceanBlue
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "📊 New Evaluation",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanBlue
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Image preview
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
                            if (uiState.selectedImageUri != null) {
                                AsyncImage(
                                    model = uiState.selectedImageUri,
                                    contentDescription = "Selected image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "📷",
                                        fontSize = 48.sp
                                    )
                                    Text(
                                        text = "Select an image",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Image selection button
                    SpongeBobButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        text = "📷 Select Image",
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Expected class input
                    Card(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        androidx.compose.material3.OutlinedTextField(
                            value = uiState.expectedClass,
                            onValueChange = { viewModel.onExpectedClassChanged(it) },
                            label = { Text("Expected Class (Ground Truth)") },
                            placeholder = {
                                Text("e.g., ${viewModel.getClassLabels().first()}")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Group selection
                    Card(
                        onClick = { showGroupDialog = true },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Evaluation Group",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = uiState.selectedGroupName ?: "Select a group",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text("▼", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Run evaluation button
                    SpongeBobButton(
                        onClick = { viewModel.runEvaluation() },
                        enabled = uiState.canRunEvaluation,
                        text = if (uiState.isProcessing) "Processing..." else "✨ Run Evaluation",
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                }
            }
        }
    }

    // Group selection dialog
    if (showGroupDialog) {
        GroupSelectionDialog(
            groups = uiState.availableGroups,
            selectedGroupId = uiState.selectedGroupId,
            onGroupSelected = { groupId, groupName ->
                viewModel.onGroupSelected(groupId, groupName)
                showGroupDialog = false
            },
            onCreateNew = { groupName ->
                viewModel.createNewGroup(groupName)
                showGroupDialog = false
            },
            onDismiss = { showGroupDialog = false }
        )
    }
}

// ==================== GROUP SELECTION DIALOG ====================
@Composable
fun GroupSelectionDialog(
    groups: List<com.example.spongebob.data.entity.EvaluationGroupEntity>,
    selectedGroupId: Long?,
    onGroupSelected: (Long, String) -> Unit,
    onCreateNew: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isNewGroup by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Select Evaluation Group",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!isNewGroup) {
                    // Existing groups list
                    if (groups.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No groups yet",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        groups.forEach { group ->
                            Card(
                                onClick = { onGroupSelected(group.groupId, group.groupName) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedGroupId == group.groupId)
                                        SpongeYellow.copy(alpha = 0.3f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = group.groupName,
                                            fontWeight = FontWeight.Medium
                                        )
                                        group.description?.let {
                                            Text(
                                                text = it,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                    if (selectedGroupId == group.groupId) {
                                        Text("✓", color = OceanBlue)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Create new group option
                    Card(
                        onClick = { isNewGroup = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = OceanBlue.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "+ Create New Group",
                                color = OceanBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    // New group creation form
                    androidx.compose.material3.OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = { Text("Group Name") },
                        placeholder = { Text("e.g., Test Set A") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isNewGroup = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (newGroupName.isNotBlank()) {
                                    onCreateNew(newGroupName)
                                }
                            },
                            enabled = newGroupName.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SpongeYellow
                            )
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}

// ==================== EVALUATION RESULT SCREEN ====================
@Composable
fun EvaluationResultScreen(
    evaluationId: Long,
    groupId: Long,
    viewModel: com.example.spongebob.viewmodel.EvaluationViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNewEvaluation: () -> Unit
) {
    var evaluation by remember { mutableStateOf<com.example.spongebob.data.entity.EvaluationEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(evaluationId) {
        evaluation = viewModel.getEvaluationById(evaluationId)
        isLoading = false
    }

    UnderwaterBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = OceanBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading evaluation...")
                    }
                }
            } else {
                evaluation?.let { eval ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        // Result indicator
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (eval.isCorrect)
                                    PatrickPink.copy(alpha = 0.15f)
                                else
                                    KrabRed.copy(alpha = 0.15f)
                            ),
                            border = BorderStroke(
                                2.dp,
                                if (eval.isCorrect) PatrickPink else KrabRed
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (eval.isCorrect) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (eval.isCorrect) PatrickPink else KrabRed,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (eval.isCorrect) "CORRECT!" else "INCORRECT",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (eval.isCorrect) PatrickPink else KrabRed
                                )
                            }
                        }

                        // Details
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Image
                            Card(
                                modifier = Modifier.size(200.dp),
                                shape = RoundedCornerShape(20.dp),
                                elevation = CardDefaults.cardElevation(8.dp)
                            ) {
                                AsyncImage(
                                    model = eval.imageUri,
                                    contentDescription = "Evaluation image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Expected vs Predicted
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Expected: ${eval.expectedClass}",
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Predicted: ${eval.predictedClass}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OceanBlue
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Confidence: ${(eval.confidence * 100).toInt()}%",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = when {
                                            eval.confidence >= 0.8f -> PatrickPink
                                            eval.confidence >= 0.5f -> SpongeYellow
                                            else -> KrabRed
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onNavigateToHome,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Home")
                                }

                                SpongeBobButton(
                                    onClick = onNewEvaluation,
                                    modifier = Modifier.weight(1f),
                                    text = "New Eval"
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            SpongeBobButton(
                                onClick = onNavigateToHistory,
                                modifier = Modifier.fillMaxWidth(),
                                text = "View History"
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== EVALUATION HISTORY SCREEN ====================
@Composable
fun EvaluationHistoryScreen(
    groupId: Long,
    viewModel: com.example.spongebob.viewmodel.HistoryViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(groupId) {
        viewModel.loadGroup(groupId)
    }

    UnderwaterBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = OceanBlue
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = uiState.groupName ?: "Evaluation History",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanBlue
                    )
                }

                // Stats summary
                uiState.groupAccuracyStats?.let { stats ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                label = "Total",
                                value = stats.total.toString(),
                                color = OceanBlue
                            )
                            StatItem(
                                label = "Correct",
                                value = stats.correct.toString(),
                                color = PatrickPink
                            )
                            StatItem(
                                label = "Accuracy",
                                value = "${(stats.accuracy * 100).toInt()}%",
                                color = if (stats.accuracy >= 0.8f)
                                    PatrickPink else KrabRed
                            )
                        }
                    }
                }

                // Confusion matrix / prediction distribution
                uiState.confusionMatrixData?.let { confusionData ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Prediction Distribution",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            confusionData.forEach { entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${entry.expectedClass} → ${entry.predictedClass}",
                                        modifier = Modifier.width(160.dp),
                                        fontSize = 12.sp
                                    )

                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(24.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(end = (100 - entry.count * 10).dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (entry.expectedClass == entry.predictedClass)
                                                        PatrickPink.copy(alpha = 0.7f)
                                                    else
                                                        KrabRed.copy(alpha = 0.5f)
                                                )
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = entry.count.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Evaluations list
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = OceanBlue)
                        }
                    }
                    uiState.evaluations.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No evaluations yet")
                        }
                    }
                    else -> {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            uiState.evaluations.forEach { eval ->
                                item {
                                    EvaluationHistoryItem(
                                        evaluation = eval,
                                        onClick = { },
                                        onDelete = { viewModel.deleteEvaluation(eval.evaluationId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun EvaluationHistoryItem(
    evaluation: com.example.spongebob.data.entity.EvaluationEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            2.dp,
            if (evaluation.isCorrect) PatrickPink.copy(alpha = 0.5f) else KrabRed.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Card(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                AsyncImage(
                    model = evaluation.imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (evaluation.isCorrect) "✓" else "✗",
                        color = if (evaluation.isCorrect) PatrickPink else KrabRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = evaluation.predictedClass,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Expected: ${evaluation.expectedClass}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "${(evaluation.confidence * 100).toInt()}% confidence",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = KrabRed.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ==================== MODEL INFO SCREEN ====================
@Composable
fun ModelInfoScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var modelInfo by remember { mutableStateOf<ModelInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load model info
    LaunchedEffect(Unit) {
        try {
            val modelManager = TFLiteModelManager(context)
            modelManager.initialize()
            modelInfo = modelManager.getModelInfo()
            modelManager.close()
        } catch (e: Exception) {
            errorMessage = "Failed to load model info: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    UnderwaterBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = OceanBlue
                            )
                        }
                        Text(
                            text = "Model Information",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanBlue
                        )
                    }
                }
            }
        ) { paddingValues ->
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = OceanBlue)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading model information...",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.padding(24.dp),
                            colors = CardDefaults.cardColors(containerColor = KrabRed.copy(alpha = 0.1f)),
                            border = BorderStroke(2.dp, KrabRed)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "❌", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Error Loading Model",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KrabRed
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage ?: "Unknown error",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                modelInfo != null -> {
                    ModelInfoContent(
                        modelInfo = modelInfo!!,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModelInfoContent(
    modelInfo: ModelInfo,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Model Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = OceanBlue.copy(alpha = 0.1f)
            ),
            border = BorderStroke(2.dp, OceanBlue.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = OceanBlue)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🧠", fontSize = 32.sp)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "TensorFlow Lite Model",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanBlue
                    )
                    Text(
                        text = modelInfo.modelFileName,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // File Information Section
        ModelInfoSection(
            title = "File Information",
            icon = "📁"
        ) {
            ModelInfoRow(label = "File Name", value = modelInfo.modelFileName)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ModelInfoRow(label = "File Size", value = modelInfo.modelFileSizeFormatted)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ModelInfoRow(label = "Est. Parameters", value = modelInfo.totalParametersFormatted)
        }

        // Input Tensor Section
        ModelInfoSection(
            title = "Input Tensor",
            icon = "📥"
        ) {
            ModelInfoRow(label = "Shape", value = "[${modelInfo.inputShape.joinToString(", ")}]")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ModelInfoRow(label = "Data Type", value = modelInfo.inputDataType)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ModelInfoRow(label = "Format", value = modelInfo.inputFormat)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ModelInfoRow(label = "Dimensions", value = "${modelInfo.inputWidth} × ${modelInfo.inputHeight} × ${modelInfo.inputChannels}")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ModelInfoRow(label = "Batch Size", value = modelInfo.batchSize.toString())
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ModelInfoRow(label = "Size in Memory", value = modelInfo.inputSizeFormatted)
        }

        // Output Tensor Section
        ModelInfoSection(
            title = "Output Tensor",
            icon = "📤"
        ) {
            ModelInfoRow(label = "Shape", value = "[${modelInfo.outputShape.joinToString(", ")}]")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ModelInfoRow(label = "Data Type", value = modelInfo.outputDataType)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ModelInfoRow(label = "Classes", value = modelInfo.outputClasses.toString())
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ModelInfoRow(label = "Size in Memory", value = modelInfo.outputSizeFormatted)
        }

        // Class Labels Section
        ModelInfoSection(
            title = "Class Labels",
            icon = "🏷️"
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                modelInfo.classLabels.forEachIndexed { index, label ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (index) {
                                0 -> PatrickPink.copy(alpha = 0.15f)
                                1 -> SpongeYellow.copy(alpha = 0.15f)
                                else -> OceanBlue.copy(alpha = 0.15f)
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            when (index) {
                                0 -> PatrickPink.copy(alpha = 0.3f)
                                1 -> SpongeYellow.copy(alpha = 0.3f)
                                else -> OceanBlue.copy(alpha = 0.3f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (index) {
                                    0 -> PatrickPink
                                    1 -> SpongeYellowDark
                                    else -> OceanBlue
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Runtime Configuration Section
        ModelInfoSection(
            title = "Runtime Configuration",
            icon = "⚙️"
        ) {
            ModelInfoRow(label = "Number of Threads", value = modelInfo.numThreads.toString())
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ModelInfoRow(
                label = "Hardware Acceleration",
                value = if (modelInfo.useGpu && modelInfo.isGpuSupported) "GPU Enabled" else "CPU Only",
                valueColor = if (modelInfo.useGpu && modelInfo.isGpuSupported) PatrickPink else MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ModelInfoRow(label = "GPU Supported", value = if (modelInfo.isGpuSupported) "Yes" else "No")
        }

        // TFLite Version Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = SpongeYellow.copy(alpha = 0.1f)
            ),
            border = BorderStroke(2.dp, SpongeYellow.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📦", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "TensorFlow Lite Runtime",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpongeYellowDark
                    )
                    Text(
                        text = "Version ${TFLiteModelManager.TFLITE_VERSION}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Spacer for bottom padding
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ModelInfoSection(
    title: String,
    icon: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(text = icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OceanBlue
                )
            }
            content()
        }
    }
}

@Composable
private fun ModelInfoRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}
