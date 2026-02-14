# Spongebob Image Classification App

## Overview
Android app for AI-based image classification using Jetpack Compose, CameraX, and TensorFlow Lite. Features multi-model support, evaluation system, and dynamic model configuration via YAML.

## Architecture

### Modern Android Stack (2025)
| Component | Technology |
|-----------|------------|
| UI | Jetpack Compose + Material3 |
| Navigation | Navigation Compose (Type-safe) |
| State Management | StateFlow + ViewModel |
| Camera | CameraX (Preview, ImageCapture) |
| ML Inference | TensorFlow Lite |
| Image Loading | Coil |
| Async | Kotlin Coroutines & Flow |
| Persistence | Room Database + DataStore |
| Theme | OKLCH Color System (Light/Dark/System) |
| Charts | Vico Chart Library |
| Config | SnakeYAML |

### Project Structure
```
app/src/main/java/com/example/spongebob/
├── MainActivity.kt                  # Entry point, NavHost setup
├── model/
│   ├── TFLiteModelManager.kt       # TFLite inference wrapper
│   ├── ModelManager.kt              # YAML config loader
│   ├── ModelConfig.kt               # Model config data class
│   └── TFLiteModelLoader.kt         # Model loading utilities
├── navigation/
│   └── NavRoutes.kt                 # Type-safe navigation routes
├── screens/
│   ├── Screens.kt                    # Main screen composables
│   ├── SettingsScreen.kt             # Settings with theme/model selection
│   ├── model/
│   │   ├── ModelListScreen.kt        # Model selection UI
│   │   └── ModelDetailScreen.kt       # Model details UI
│   └── evaluation/
│       └── [Evaluation screens]
├── viewmodel/
│   ├── ClassificationViewModel.kt     # Main inference state
│   ├── SettingsViewModel.kt           # Settings state
│   ├── ModelSelectionViewModel.kt     # Model selection state
│   └── EvaluationViewModel.kt         # Evaluation state
├── data/
│   ├── SpongeBobDatabase.kt          # Room database
│   ├── PreferencesManager.kt          # DataStore preferences
│   ├── dao/                          # Data Access Objects
│   ├── entity/                        # Room entities
│   └── converters/                     # Type converters
└── ui/theme/
    ├── OklchTheme.kt                 # OKLCH theme wrapper
    ├── OklchColors.kt                # OKLCH color definitions
    ├── ThemeSwitcher.kt               # Theme selection UI
    └── Shape.kt                      # Custom shapes

app/src/main/assets/
├── small.tflite                      # Default TFLite model
└── models.yaml                       # Model configurations
```

### Navigation Routes

#### Main Routes
| Route | Description |
|-------|-------------|
| `MainMenu` | Main menu - choose Detection or Evaluation |
| `Input` | Image selection (gallery/camera) |
| `Camera` | CameraX capture |
| `Crop(imageUri: String)` | Image cropping |
| `Inference` | Run inference |
| `Result(className, confidence)` | Display results |
| `Settings` | App settings |
| `ModelList` | Model selection list |
| `ModelDetail(modelId: String)` | Model details |
| `ModelInfo` | Current model info |
| `GpuPrompt` | One-time GPU acceleration prompt |

#### Evaluation Routes
| Route | Description |
|-------|-------------|
| `EvaluationHome` | Evaluation dashboard |
| `EvaluationInput` | Select image + expected class |
| `EvaluationResult(evaluationId, groupId)` | View evaluation result |
| `EvaluationHistory(groupId: Long)` | View evaluation group history |

## Model Configuration (YAML)

Models are configured via `app/src/main/assets/models.yaml`:

```yaml
models:
  - id: "small_3class"
    file: "small.tflite"
    name: "Small Classifier (3 classes)"
    description: "Lightweight model for quick inference"
    type: "tflite"           # or "onnx"
    inputSize: 640
    classes:
      - "Sehat"
      - "Sedang"
      - "Parah"
```

### ModelConfig Properties
- `id`: Unique identifier (used in preferences)
- `file`: Model filename in assets folder
- `name`: Display name
- `description`: Human-readable description
- `type`: Model type (`tflite` or `onnx`)
- `inputSize`: Input image size (assumes square: inputSize x inputSize)
- `classes`: List of class labels

## Adding Your TFLite Model

1. **Export from PyTorch to TFLite:**
```python
import torch

model = YourModel()
model.eval()

# Export to ONNX first
torch.onnx.export(
    model,
    torch.randn(1, 3, 224, 224),
    "model.onnx",
    opset_version=14,
    input_names=['input'],
    output_names=['output']
)

# Convert ONNX to TFLite using onnx-tf
# or use tf-nightly with TFLite converter
```

2. **Place model at:** `app/src/main/assets/your_model.tflite`

3. **Add entry to** `models.yaml`:
```yaml
  - id: "my_model"
    file: "your_model.tflite"
    name: "My Custom Model"
    description: "Description of your model"
    type: "tflite"
    inputSize: 224
    classes:
      - "Class 1"
      - "Class 2"
      - "Class 3"
```

## DataStore Preferences

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `show_inference_time` | Boolean | `true` | Show inference time on results |
| `use_gpu` | Boolean | `false` | Use GPU acceleration (experimental) |
| `gpu_modal_shown` | Boolean | `false` | One-time GPU prompt shown flag |
| `selected_model_id` | String | `"small_3class"` | Current selected model ID |
| `theme` | String | `"system"` | Theme: `"light"`, `"dark"`, `"system"` |

## Room Database

### Entities
- `EvaluationGroupEntity`: Groups related evaluations
- `EvaluationEntity`: Single evaluation result with metrics

### DAOs
- `EvaluationGroupDao`: CRUD for evaluation groups
- `EvaluationDao`: CRUD for individual evaluations

## Dependencies (Version Catalog)

| Library | Version |
|---------|---------|
| AGP | 8.7.3 |
| Kotlin | 2.0.21 |
| Compose BOM | 2024.09.00 |
| Navigation Compose | 2.8.3 |
| CameraX | 1.4.0 |
| TensorFlow Lite | 2.14.0 |
| TFLite Support | 0.4.4 |
| DataStore | 1.1.1 |
| Room | 2.6.1 |
| Vico Charts | 2.0.0-beta.2 |
| SnakeYAML | 2.2 |
| Coil | 2.7.0 |

## UI Theme System

### OKLCH Color System
The app uses a custom OKLCH-based color system for perceptually uniform colors:
- `OklchLightColors`: Light theme colors
- `OklchDarkColors`: Dark theme colors
- `ThemeOption`: Enum for `LIGHT`, `DARK`, `SYSTEM`

### Theme Colors
- `SpongeYellow`: Primary yellow (SpongeBob themed)
- `OceanBlue`: Secondary blue
- `PatrickPink`: Accent pink (Patrick themed)
- `DeepSea`: Text/icon color

## Development Notes

### Model Input/Output
- Input format: RGB [0,1] normalized float32
- Input shape: `[batch, height, width, channels]` (NHWC)
- Output: Softmax probabilities (one per class)

### Camera Permissions
- `CAMERA` permission requested at runtime
- `READ_MEDIA_IMAGES` for gallery access

### Performance Monitoring
The app tracks and displays:
- Inference time (milliseconds)
- CPU usage during inference
- Memory usage (used/total MB)
- Hardware acceleration status (NNAPI/GPU)

### Build Configuration
- `minSdk`: 24 (Android 7.0)
- `targetSdk`: 35 (Android 15)
- `compileSdk`: 35
- JVM target: 11
