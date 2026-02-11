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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.spongebob.model.OnnxModelManager
import com.example.spongebob.navigation.*
import com.example.spongebob.screens.*
import com.example.spongebob.ui.theme.SpongebobTheme
import com.example.spongebob.viewmodel.*
import com.example.spongebob.data.PreferencesManager
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
                        preferencesManager = preferencesManager,
                        activity = this
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
    preferencesManager: PreferencesManager,
    activity: ComponentActivity
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
        startDestination = MainMenu
    ) {
        // ==================== MAIN MENU & DETECTION ROUTES ====================

        // Main Menu Screen - New Start Destination
        composable<MainMenu> {
            MainMenuScreen(
                onNavigateToDetect = {
                    navController.navigate(Input)
                },
                onNavigateToEvaluate = {
                    navController.navigate(EvaluationHome)
                },
                onNavigateToSettings = {
                    navController.navigate(Settings)
                },
                onQuit = {
                    activity.finish()
                }
            )
        }

        // Input Screen
        composable<Input> {
            InputScreen(
                uiState = uiState,
                onImageSelected = { uri ->
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
                onClearError = { viewModel.clearError() },
                onBackToMainMenu = {
                    navController.popBackStack()
                }
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
                    settingsViewModel.setUseNnapi(true)
                    settingsViewModel.markNnapiModalShown()
                    navController.popBackStack()
                },
                onSkip = {
                    settingsViewModel.markNnapiModalShown()
                    navController.popBackStack()
                }
            )
        }

        // ==================== EVALUATION ROUTES ====================

        // Evaluation Home Screen
        composable<EvaluationHome> {
            val evaluationViewModel: EvaluationViewModel = viewModel()
            EvaluationHomeScreen(
                viewModel = evaluationViewModel,
                onNavigateToInput = {
                    navController.navigate(EvaluationInput)
                },
                onNavigateToHistory = { groupId ->
                    navController.navigate(EvaluationHistory(groupId))
                },
                onNavigateToSettings = {
                    navController.navigate(Settings)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Evaluation Input Screen
        composable<EvaluationInput> {
            val evaluationViewModel: EvaluationViewModel = viewModel()
            val evalUiState by evaluationViewModel.uiState.collectAsState()

            // Handle navigation when evaluation completes
            androidx.compose.runtime.LaunchedEffect(evalUiState.lastEvaluationId) {
                val lastId = evalUiState.lastEvaluationId
                val groupId = evalUiState.selectedGroupId
                if (lastId != null && groupId != null) {
                    navController.navigate(EvaluationResult(evaluationId = lastId, groupId = groupId))
                    evaluationViewModel.onNavigationComplete()
                }
            }

            EvaluationInputScreen(
                viewModel = evaluationViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Evaluation Result Screen
        composable<EvaluationResult> { backStackEntry ->
            val result: EvaluationResult = backStackEntry.toRoute()
            val evaluationViewModel: EvaluationViewModel = viewModel()

            EvaluationResultScreen(
                evaluationId = result.evaluationId,
                groupId = result.groupId,
                viewModel = evaluationViewModel,
                onNavigateToHistory = {
                    navController.navigate(EvaluationHistory(result.groupId)) {
                        popUpTo(EvaluationHome) { inclusive = false }
                    }
                },
                onNavigateToHome = {
                    navController.popBackStack(EvaluationHome, inclusive = false)
                },
                onNewEvaluation = {
                    navController.popBackStack(EvaluationInput, inclusive = false)
                }
            )
        }

        // Evaluation History Screen
        composable<EvaluationHistory> { backStackEntry ->
            val history: EvaluationHistory = backStackEntry.toRoute()
            val historyViewModel: HistoryViewModel = viewModel()

            EvaluationHistoryScreen(
                groupId = history.groupId,
                viewModel = historyViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
