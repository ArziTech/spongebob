# Spongebob App - Refactoring Summary

## Architecture Overview

### Modern Android Stack (2025)

| Component | Technology |
|-----------|------------|
| UI | Jetpack Compose + Material3 |
| Navigation | Navigation Compose (Type-safe) |
| State Management | StateFlow + ViewModel |
| Camera | CameraX (Preview, ImageCapture) |
| ML Inference | ONNX Runtime (PyTorch models) |
| Image Loading | Coil |
| Async | Kotlin Coroutines & Flow |

---

## Project Structure

```
app/src/main/java/com/example/spongebob/
├── MainActivity.kt              # Entry point, NavHost setup
├── model/
│   └── OnnxModelManager.kt      # ONNX Runtime wrapper
├── navigation/
│   └── NavRoutes.kt             # Type-safe navigation routes
├── screens/
│   └── Screens.kt               # All screen composables
└── viewmodel/
    └── ClassificationViewModel.kt  # StateFlow UI state

app/src/main/assets/
└── model.onnx                   # YOUR ONNX MODEL (place here)
```

---

## Dependencies Added

### build.gradle.kts
```kotlin
// Navigation
implementation(libs.androidx.navigation.compose)
implementation(libs.kotlinx.serialization.json)

// CameraX
implementation(libs.androidx.camera.core)
implementation(libs.androidx.camera.camera2)
implementation(libs.androidx.camera.lifecycle)
implementation(libs.androidx.camera.view)

// ONNX Runtime
implementation(libs.onnxruntime.android)

// ViewModel
implementation(libs.androidx.lifecycle.viewmodel.compose)
```

### Plugins
```kotlin
alias(libs.plugins.kotlin.serialization)
```

---

## Key Features

### 1. StateFlow + ViewModel
```kotlin
// ViewModel with StateFlow
class ClassificationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ClassificationUiState())
    val uiState: StateFlow<ClassificationUiState> = _uiState.asStateFlow()
}

// Collect in Compose
val uiState by viewModel.uiState.collectAsState()
```

### 2. Type-Safe Navigation
```kotlin
// Define routes with @Serializable
@Serializable object Input
@Serializable object Camera
@Serializable object Inference
@Serializable data class Result(val className: String, val confidence: Float)

// Navigate
navController.navigate(Inference)
```

### 3. CameraX Integration
```kotlin
// Camera preview with capture
@Composable
fun CameraPreview(onImageCaptured: (Uri) -> Unit) {
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    // Bind to lifecycle
    cameraProvider.bindToLifecycle(
        lifecycleOwner,
        CameraSelector.DEFAULT_BACK_CAMERA,
        preview,
        imageCapture
    )
}
```

### 4. ONNX Runtime Integration
```kotlin
class OnnxModelManager(private val context: Context) {
    suspend fun initialize() {
        environment = OrtEnvironment.getEnvironment()
        session = environment?.createSession(modelBytes, options)
    }

    suspend fun runInference(imageUri: Uri): ClassificationResult {
        // Preprocess, run inference, return results
    }
}
```

---

## Adding Your ONNX Model

### Step 1: Export from PyTorch
```python
import torch

model = YourPyTorchModel()
model.eval()

torch.onnx.export(
    model,
    torch.randn(1, 3, 224, 224),  # Dummy input
    "model.onnx",
    export_params=True,
    opset_version=14,
    input_names=['input'],
    output_names=['output']
)
```

### Step 2: Place Model File
```
Copy model.onnx to:
app/src/main/assets/model.onnx
```

### Step 3: Configure in OnnxModelManager.kt
```kotlin
companion object {
    const val MODEL_FILE = "model.onnx"        // Your filename
    const val INPUT_WIDTH = 224                 // Your input size
    const val INPUT_HEIGHT = 224

    val CLASS_LABELS = listOf(                  // Your classes
        "Class 1", "Class 2", "Class 3", "Class 4"
    )
}
```

---

## Workflow

1. **Input Screen** → Select image from Gallery or Camera
2. **Camera Screen** → CameraX preview with capture button
3. **Inference Screen** → ONNX Runtime runs classification
4. **Result Screen** → Display predictions with confidence scores

---

## Development Notes

- Camera permission handled at runtime
- NNAPI enabled for hardware acceleration
- Model input: RGB [0,1], shape [1, 3, 224, 224]
- Model output: [1, num_classes] float32 tensor
- Softmax applied automatically for probabilities

---

## Build & Run

1. Sync Gradle (new dependencies added)
2. Place your `model.onnx` in `app/src/main/assets/`
3. Configure class labels in `OnnxModelManager.kt`
4. Run the app
