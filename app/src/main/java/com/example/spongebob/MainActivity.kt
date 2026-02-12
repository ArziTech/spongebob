package com.example.spongebob

import android.content.Context
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.spongebob.model.TFLiteModelManager
import com.example.spongebob.model.ModelManager
import com.example.spongebob.navigation.*
import com.example.spongebob.screens.*
import com.example.spongebob.screens.model.ModelListScreen
import com.example.spongebob.screens.model.ModelDetailScreen
import com.example.spongebob.ui.theme.SpongebobTheme
import com.example.spongebob.viewmodel.*
import com.example.spongebob.data.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Use lazy initialization to avoid NPE during activity construction
    private val preferencesManager by lazy { PreferencesManager(applicationContext) }

    // We'll initialize viewModel with false initially, then update when GPU setting changes
    private val viewModel: ClassificationViewModel by lazy {
        ClassificationViewModelFactory(
            applicationContext,
            useGpu = false
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
    private val useGpu: Boolean = false
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClassificationViewModel::class.java)) {
            val tfLiteModelManager = TFLiteModelManager(context, useGpu)
            return ClassificationViewModel(context, tfLiteModelManager) as T
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

    // Model Manager for accessing model configurations
    val modelManager = ModelManager(context)

    // State for current model name
    var currentModelName by androidx.compose.runtime.mutableStateOf("Loading...")
    androidx.compose.runtime.LaunchedEffect(Unit) {
        androidx.compose.runtime.rememberCoroutineScope().launch {
            // Get current model name
            try {
                val selectedModelId = preferencesManager.selectedModelId.first()
                modelManager.loadModelConfigs()
                val config = modelManager.getModelConfig(selectedModelId ?: "small_3class")
                currentModelName = config?.name ?: "Unknown Model"
            } catch (e: Exception) {
                currentModelName = "Small Classifier (3 classes)"
            }
        }
    }

    // Check for GPU modal on first navigation to Input screen
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val modalShown = preferencesManager.gpuModalShown.first()
        if (!modalShown) {
            // Check if device supports GPU
            val tfLiteModel = TFLiteModelManager(context)
            val gpuSupported = tfLiteModel.isGpuSupported()
            if (gpuSupported) {
                navController.navigate(GpuPrompt)
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
                currentModelName = currentModelName,
                onNavigateToDetect = {
                    navController.navigate(Input)
                },
                onNavigateToEvaluate = {
                    navController.navigate(EvaluationHome)
                },
                onNavigateToSettings = {
                    navController.navigate(Settings)
                },
                onNavigateToModelInfo = {
                    navController.navigate(com.example.spongebob.navigation.ModelInfo)
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
                },
                onNavigateToModelList = {
                    navController.navigate(com.example.spongebob.navigation.ModelList)
                }
            )
        }

        // Model List Screen
        composable<com.example.spongebob.navigation.ModelList> {
            val modelSelectionViewModel: ModelSelectionViewModel = viewModel()
            ModelListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onModelClick = { modelId ->
                    navController.navigate(com.example.spongebob.navigation.ModelDetail(modelId))
                }
            )
        }

        // Model Detail Screen
        composable<com.example.spongebob.navigation.ModelDetail> { backStackEntry ->
            val modelDetail: com.example.spongebob.navigation.ModelDetail = backStackEntry.toRoute()
            val modelSelectionViewModel: ModelSelectionViewModel = viewModel()
            ModelDetailScreen(
                modelId = modelDetail.modelId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onUseModel = {
                    navController.popBackStack(com.example.spongebob.navigation.Settings, inclusive = false)
                }
            )
        }

        // GPU Prompt Modal (one-time)
        composable<GpuPrompt> {
            GpuPromptScreen(
                onEnable = {
                    settingsViewModel.setUseGpu(true)
                    settingsViewModel.markGpuModalShown()
                    navController.popBackStack()
                },
                onSkip = {
                    settingsViewModel.markGpuModalShown()
                    navController.popBackStack()
                }
            )
        }

        // Model Info Screen
        composable<com.example.spongebob.navigation.ModelInfo> {
            ModelInfoScreen(
                onBack = {
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
