package com.example.spongebob

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.spongebob.model.OnnxModelManager
import com.example.spongebob.navigation.Camera
import com.example.spongebob.navigation.Crop
import com.example.spongebob.navigation.Inference
import com.example.spongebob.navigation.Input
import com.example.spongebob.navigation.Result
import com.example.spongebob.navigation.Settings
import com.example.spongebob.screens.CameraScreen
import com.example.spongebob.screens.CropScreen
import com.example.spongebob.screens.InferenceScreen
import com.example.spongebob.screens.InputScreen
import com.example.spongebob.screens.NnapiPromptScreen
import com.example.spongebob.screens.ResultScreen
import com.example.spongebob.screens.SettingsScreen
import com.example.spongebob.ui.theme.SpongebobTheme
import com.example.spongebob.viewmodel.ClassificationViewModel
import com.example.spongebob.viewmodel.SettingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spongebob.data.PreferencesManager
import com.example.spongebob.navigation.NnapiPrompt
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    // Use lazy initialization to avoid NPE during activity construction
    private val preferencesManager by lazy { PreferencesManager(applicationContext) }

    // We'll initialize viewModel with false initially, then update when NNAPI setting changes
    private val viewModel: ClassificationViewModel by lazy {
        ClassificationViewModelFactory(
            applicationContext,
            useNnapi = false
        ).create(ClassificationViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpongebobTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ClassificationNavHost(
                        viewModel = viewModel,
                        preferencesManager = preferencesManager
                    )
                }
            }
        }
    }
}

// ViewModel Factory
class ClassificationViewModelFactory(
    private val context: Context,
    private val useNnapi: Boolean = false
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClassificationViewModel::class.java)) {
            val onnxModelManager = OnnxModelManager(context, useNnapi)
            return ClassificationViewModel(onnxModelManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Navigation Host
@Composable
fun ClassificationNavHost(
    viewModel: ClassificationViewModel,
    preferencesManager: PreferencesManager
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val settingsViewModel: SettingsViewModel = viewModel()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Check for NNAPI modal on first navigation to Input screen
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 27) {
            val modalShown = preferencesManager.nnapiModalShown.first()
            if (!modalShown) {
                // Check if device supports NNAPI
                val onnxModel = OnnxModelManager(context)
                val nnapiSupported = onnxModel.isNnapiSupported()
                if (nnapiSupported) {
                    navController.navigate(NnapiPrompt)
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Input
    ) {
        // Input Screen
        composable<Input> {
            InputScreen(
                uiState = uiState,
                onImageSelected = { uri ->
                    // Navigate to Crop screen instead of directly setting the image
                    navController.navigate(Crop(imageUri = uri.toString()))
                },
                onNavigateToCamera = {
                    navController.navigate(Camera)
                },
                onNavigateToInference = {
                    navController.navigate(Inference)
                },
                onNavigateToSettings = {
                    navController.navigate(Settings)
                },
                onClearError = { viewModel.clearError() }
            )
        }

        // Crop Screen
        composable<Crop> { backStackEntry ->
            val crop: Crop = backStackEntry.toRoute()
            CropScreen(
                imageUri = crop.imageUri,
                onConfirm = { croppedUri ->
                    viewModel.onImageCropped(croppedUri)
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        // Camera Screen
        composable<Camera> {
            CameraScreen(
                onImageCaptured = { uri ->
                    viewModel.onImageSelected(uri)
                    // Navigate back to input, then to inference
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Inference Screen
        composable<Inference> {
            InferenceScreen(
                uiState = uiState,
                onClassify = { viewModel.onClassify() },
                onComplete = {
                    val result = uiState.result
                    if (result != null) {
                        navController.navigate(Result(className = result.className, confidence = result.confidence))
                    }
                }
            )
        }

        // Result Screen
        composable<Result> {
            ResultScreen(
                uiState = uiState,
                settingsViewModel = settingsViewModel,
                onBack = {
                    navController.popBackStack(route = Input, inclusive = false)
                    viewModel.onClearImage()
                },
                onNewImage = {
                    navController.popBackStack(route = Input, inclusive = false)
                    viewModel.onClearImage()
                }
            )
        }

        // Settings Screen
        composable<Settings> {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // NNAPI Prompt Modal (one-time)
        composable<NnapiPrompt> {
            NnapiPromptScreen(
                onEnable = {
                    // Enable NNAPI
                    settingsViewModel.setUseNnapi(true)
                    settingsViewModel.markNnapiModalShown()
                    navController.popBackStack()
                },
                onSkip = {
                    // Mark as shown but don't enable
                    settingsViewModel.markNnapiModalShown()
                    navController.popBackStack()
                }
            )
        }
    }
}
