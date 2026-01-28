# ONNX Model Placement

Place your PyTorch-exported ONNX model file here as `model.onnx`

## Example path:
```
app/src/main/assets/model.onnx
```

## Model Requirements:
- Input: [1, 3, 224, 224] float32 tensor (RGB image, values 0-1)
- Output: [1, num_classes] float32 tensor (class logits or probabilities)

## Export from PyTorch:
```python
import torch

model = YourPyTorchModel()
model.eval()

dummy_input = torch.randn(1, 3, 224, 224)
torch.onnx.export(
    model,
    dummy_input,
    "model.onnx",
    export_params=True,
    opset_version=14,
    input_names=['input'],
    output_names=['output']
)
```

## Configuration:
Edit `OnnxModelManager.kt` to change:
- Model filename: `MODEL_FILE`
- Input size: `INPUT_WIDTH`, `INPUT_HEIGHT`
- Class labels: `CLASS_LABELS`
