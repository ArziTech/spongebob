package com.example.spongebob

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
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
import com.example.spongebob.screens.CameraScreen
import com.example.spongebob.screens.CropScreen
import com.example.spongebob.screens.InferenceScreen
import com.example.spongebob.screens.InputScreen
import com.example.spongebob.screens.ResultScreen
import com.example.spongebob.ui.theme.SpongebobTheme
import com.example.spongebob.viewmodel.ClassificationViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ClassificationViewModel by viewModels {
        ClassificationViewModelFactory(
            OnnxModelManager(applicationContext)
        )
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
                    ClassificationNavHost(viewModel)
                }
            }
        }
    }
}

// ViewModel Factory
class ClassificationViewModelFactory(
    private val onnxModelManager: OnnxModelManager
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClassificationViewModel::class.java)) {
            return ClassificationViewModel(onnxModelManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Navigation Host
@Composable
fun ClassificationNavHost(
    viewModel: ClassificationViewModel
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

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
    }
}
