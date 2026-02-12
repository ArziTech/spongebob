# Spongebob App - Development Tasks

> **Important**: Create a plan before implementing. You can use context7 to get latest documentation.

---

## Priority Task 1: Multi-Model Support & Model Selector ✅ COMPLETED

### Overview
Enable users to switch between multiple ML models (TFLite and ONNX) with a model management UI.

**Status**: Implemented (2025-02-13)

### Tasks

#### 1.1 Model Configuration System ✅
- [x] Create `models.yaml` in `app/src/main/assets/` with model metadata
- [x] YAML format structure with `small_3class` model:
```yaml
models:
  - id: "small_3class"
    file: "small.tflite"
    name: "Small Classifier (3 classes)"
    description: "Lightweight model for quick inference"
    type: "tflite"
    inputSize: 640
    classes:
      - "Sehat"
      - "Sedang"
      - "Parah"
```

#### 1.2 Data Classes & Model Manager ✅
- [x] Create `ModelConfig.kt` with ModelType enum, ModelConfig, and ModelsConfig data classes
- [x] Create `ModelManager.kt` to handle model loading from YAML
- [x] Create `TFLiteModelLoader.kt` as configurable TFLite loader
- [x] Support both TFLite runtime (ONNX future)
- [x] Read and parse `models.yaml` at startup
- [x] Persist selected model in DataStore Preferences
- [x] Get current selected model info

#### 1.3 Preferences Manager Extension ✅
- [x] Add `SELECTED_MODEL_ID_KEY` to PreferencesManager
- [x] Add `selectedModelId` Flow with default "small_3class"
- [x] Add `setSelectedModelId()` suspend function

#### 1.4 Navigation Routes ✅
- [x] Add `ModelList` route to `NavRoutes.kt`
- [x] Add `ModelDetail` route with modelId parameter

#### 1.5 ViewModels ✅
- [x] Create `ModelSelectionViewModel.kt` for model selection UI
- [x] Update `ClassificationViewModel.kt` with PreferencesManager parameter

#### 1.6 UI Screens ✅
- [x] **ModelListScreen**: Display models in 1-column list
  - Show model name, brief description, type badge (TFLITE/ONNX)
  - Indicate current selected model with checkmark
  - Click item to navigate to detail
- [x] **ModelDetailScreen**: Show full model info
  - Display all metadata (name, description, type, input size, classes)
  - "Use Model" button at bottom to select
  - "Cancel" button to go back

#### 1.7 Main Menu Integration ✅
- [x] Add current model display at top of MainMenuScreen
- [x] Show model name in muted text below title
- [x] Format: "Current: Small Classifier (3 classes)"

#### 1.8 Settings Integration ✅
- [x] Add "Change Model" button in SettingsScreen
- [x] Navigate to ModelListScreen on button click
- [x] Added SettingsButtonCard composable for navigation buttons

#### 1.9 MainActivity Integration ✅
- [x] Add ModelManager for accessing model configurations
- [x] Add currentModelName state with LaunchedEffect loading
- [x] Pass currentModelName to MainMenuScreen
- [x] Add ModelList and ModelDetail composables in NavHost

### Files Created
- `app/src/main/java/com/example/spongebob/model/ModelConfig.kt`
- `app/src/main/java/com/example/spongebob/model/ModelManager.kt`
- `app/src/main/java/com/example/spongebob/model/TFLiteModelLoader.kt`
- `app/src/main/java/com/example/spongebob/viewmodel/ModelSelectionViewModel.kt`
- `app/src/main/java/com/example/spongebob/screens/model/ModelListScreen.kt`
- `app/src/main/java/com/example/spongebob/screens/model/ModelDetailScreen.kt`
- `app/src/main/assets/models.yaml`

### Files Modified
- `gradle/libs.versions.toml` - Added SnakeYAML version
- `app/build.gradle.kts` - Added SnakeYAML implementation
- `app/src/main/java/com/example/spongebob/data/PreferencesManager.kt` - Added model ID preferences
- `app/src/main/java/com/example/spongebob/navigation/NavRoutes.kt` - Added new routes
- `app/src/main/java/com/example/spongebob/screens/Screens.kt` - Updated MainMenuScreen
- `app/src/main/java/com/example/spongebob/screens/SettingsScreen.kt` - Added model selection button
- `app/src/main/java/com/example/spongebob/viewmodel/ClassificationViewModel.kt` - Added PreferencesManager
- `app/src/main/java/com/example/spongebob/MainActivity.kt` - Added navigation and model display

---

## Priority Task 2: UI/UX Fixes

### 2.1 Remove Quit Button
- [ ] Remove quit button from MainMenuScreen
- [ ] Rationale: Android has standard back/home navigation

### 2.2 Fix Missing Buttons in Result Screen
- [ ] Investigate why buttons don't appear on real devices
- [ ] Check layout constraints and Scaffold bottomBar placement
- [ ] Test on physical device after fix
- [ ] Files: `Screens.kt` - ResultScreen composable

### 2.3 Modularize Screens.kt
**Current**: All screens in one large file (~3500+ lines)
**Target**: Separate into logical modules

```
screens/
├── main/
│   └── MainMenuScreen.kt
├── classification/
│   ├── InputScreen.kt
│   ├── CameraScreen.kt
│   ├── CropScreen.kt
│   ├── InferenceScreen.kt
│   └── ResultScreen.kt
├── evaluation/
│   ├── EvaluationHomeScreen.kt
│   ├── EvaluationInputScreen.kt
│   ├── EvaluationResultScreen.kt
│   └── EvaluationHistoryScreen.kt
├── settings/
│   ├── SettingsScreen.kt
│   └── GpuPromptScreen.kt
└── model/
    ├── ModelListScreen.kt ✅
    └── ModelDetailScreen.kt ✅
```

---

## Priority Task 3: Theme System Migration

### 3.1 New Theme Implementation
- [ ] Remove SpongeBob hardcoded theme
- [ ] Implement new theme using OKLCH color space (provided below)
- [ ] Create modular theme system for easy theme switching

#### Color Theme Specification

**Light Theme** (`:root`):
```css
--background: oklch(0.9924 0.0028 308.4292);
--foreground: oklch(0.1288 0.0219 314.0129);
--primary: oklch(0.2236 0.1469 265.8205);
--primary-foreground: oklch(1.0000 0 0);
--secondary: oklch(0.9387 0.0262 264.4409);
--secondary-foreground: oklch(0.4691 0.2225 262.4817);
--muted: oklch(0.9518 0.0057 308.3939);
--muted-foreground: oklch(0.4882 0.0203 308.0008);
--accent: oklch(0.9356 0.0312 279.8620);
--accent-foreground: oklch(0.4691 0.2225 262.4817);
--destructive: oklch(0.5858 0.2220 17.5846);
--destructive-foreground: oklch(1.0000 0 0);
--border: oklch(0.9160 0.0120 313.2115);
--input: oklch(0.9160 0.0120 313.2115);
--ring: oklch(0.6219 0.2036 262.1505);
--card: oklch(1.0000 0 0);
--card-foreground: oklch(0.1288 0.0219 314.0129);
--popover: oklch(1.0000 0 0);
--popover-foreground: oklch(0.1288 0.0219 314.0129);
```

**Dark Theme** (`.dark`):
```css
--background: oklch(0.1063 0.0172 259.5380);
--foreground: oklch(0.9924 0.0028 308.4292);
--primary: oklch(0.3820 0.1967 265.2890);
--primary-foreground: oklch(1.0000 0 0);
--secondary: oklch(0.2108 0.0426 270.3694);
--secondary-foreground: oklch(0.9924 0.0028 308.4292);
--muted: oklch(0.1797 0.0376 272.6396);
--muted-foreground: oklch(0.6878 0.0218 285.8125);
--accent: oklch(0.2553 0.0657 274.6654);
--accent-foreground: oklch(0.9924 0.0028 308.4292);
--destructive: oklch(0.4038 0.1343 13.3026);
--destructive-foreground: oklch(1.0000 0 0);
--border: oklch(0.2445 0.0736 280.5871);
--input: oklch(0.2603 0.0625 272.3700);
--ring: oklch(0.3395 0.1557 273.0758);
--card: oklch(0.1450 0.0211 263.1799);
--card-foreground: oklch(0.9924 0.0028 308.4292);
--popover: oklch(0.1418 0.0229 268.4497);
--popover-foreground: oklch(0.9924 0.0028 308.4292);
```

### 3.2 Theme Architecture
Create `ui/theme/` directory with:
- `Color.kt` - Color definitions (OKLCH to Color conversion)
- `Theme.kt` - Compose Theme definitions
- `Type.kt` - Typography settings
- `Shape.kt` - Shape/elevation definitions

---

## Improvement Recommendations

### App Flow Enhancements

#### 1. Onboarding Experience
- [ ] Add first-run onboarding screen
- [ ] Explain camera permissions and their purpose
- [ ] Quick tutorial on how to use app
- [ ] Skip onboarding after first completion

#### 2. Result Screen Improvements
- [ ] Add ability to save/share result image
- [ ] Show confidence as progress bar/visual indicator
- [ ] Add "Retake" button for quick re-capture
- [ ] Show thumbnail of captured/cropped image
- [ ] Add "View Full Image" option

#### 3. History & Analytics
- [ ] Classification history (not just evaluation)
- [ ] Statistics dashboard: most predicted classes, daily usage
- [ ] Export evaluation results to CSV/JSON
- [ ] Filter/sort evaluation history

### UI/UX Improvements

#### 4. Accessibility
- [ ] Add content descriptions for screen readers
- [ ] Minimum touch target size (48dp)
- [ ] High contrast mode support
- [ ] Scalable font size support

#### 5. Visual Polish
- [ ] Add loading skeletons instead of plain spinners
- [ ] Smooth transitions between screens
- [ ] Micro-interactions (button press feedback)
- [ ] Success/error toast notifications

#### 6. Settings Screen
- [ ] Organize settings into sections (General, Model, Advanced)
- [ ] Add "About" section with app version
- [ ] Option to clear all evaluation data
- [ ] Theme selector (Light/Dark/System default)

### Performance & Technical

#### 7. Model Management
- [ ] Lazy load models (only load when needed)
- [ ] Show model file size in UI
- [ ] Option to download additional models (future)
- [ ] Model validation before use

#### 8. Camera & Image
- [ ] Flash toggle in camera screen
- [ ] Camera flip (front/back) for supported devices
- [ ] Image compression settings
- [ ] EXIF data preservation

#### 9. Error Handling
- [ ] User-friendly error messages
- [ ] Retry mechanism for failed classification
- [ ] Error reporting/analytics (opt-in)
- [ ] Network/offline detection

### Developer Experience

#### 10. Code Quality
- [ ] Add dependency injection (Hilt/Koin)
- [ ] Separate repository layer for data access
- [ ] Unit tests for ViewModels
- [ ] UI tests for critical flows
- [ ] Lint checks and static analysis

#### 11. Build & Deployment
- [ ] Configure product flavors (demo/production)
- [ ] ProGuard/R8 rules for ML libraries
- [ ] App bundle (.aab) support
- [ ] Version code/name management

---

## Architecture Reference

### Current Stack
| Layer | Technology |
|-------|------------|
| UI | Jetpack Compose + Material3 |
| Navigation | Navigation Compose (Type-safe) |
| State | StateFlow + ViewModel |
| Camera | CameraX (Preview, ImageCapture) |
| ML | TensorFlow Lite + NNAPI |
| Database | Room |
| Async | Kotlin Coroutines & Flow |

### Key Files to Modify
- `app/src/main/java/com/example/spongebob/navigation/NavRoutes.kt` - Add model routes ✅
- `app/src/main/java/com/example/spongebob/screens/Screens.kt` - Split into modules
- `app/src/main/java/com/example/spongebob/ui/theme/Color.kt` - New theme colors
- `app/src/main/java/com/example/spongebob/model/TFLiteModelManager.kt` - Model management
- `app/build.gradle.kts` - Add YAML parsing dependency ✅

### Dependencies Added
```kotlin
// YAML parsing ✅
implementation("org.yaml:snakeyaml:2.2")

// For OKLCH color space (if needed)
implementation("androidx.compose.ui:ui-util:1.5.0")
```
