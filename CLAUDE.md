# Spongebob Image Classification App

## Overview
Android app for AI-based image classification using Jetpack Compose, CameraX, and ONNX Runtime.

## Architecture

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

### Project Structure
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

### Workflow
1. **Input** → Gallery or CameraX to select image
2. **Inference** → ONNX Runtime classifies image
3. **Result** → Display predictions with confidence

## Adding Your ONNX Model

1. **Export from PyTorch:**
```python
import torch

model = YourModel()
model.eval()

torch.onnx.export(
    model,
    torch.randn(1, 3, 224, 224),
    "model.onnx",
    opset_version=14,
    input_names=['input'],
    output_names=['output']
)
```

2. **Place model at:** `app/src/main/assets/model.onnx`

3. **Configure** in `OnnxModelManager.kt`:
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

## Dependencies
- `androidx.navigation:navigation-compose` - Type-safe navigation
- `androidx.camera:*` - CameraX for modern camera
- `com.microsoft.onnxruntime:onnxruntime-android` - ML inference
- `androidx.lifecycle:lifecycle-viewmodel-compose` - ViewModel with Compose
- `org.jetbrains.kotlinx:kotlinx-serialization-json` - Navigation serialization

## Development Notes
- Camera permission handled at runtime
- NNAPI enabled for hardware acceleration
- Lifecycle-aware state collection (`collectAsState`)
- Model input: RGB [0,1], output: softmax probabilities
