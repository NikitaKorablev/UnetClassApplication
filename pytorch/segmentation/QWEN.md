# PyTorch U-Net Segmentation Module

## Overview

This is the **segmentation module** of the U-Net Class Application project, specializing in semantic segmentation of biomedical images using U-Net architectures. The primary use case is segmenting neural structures from electron microscope images of mouse brain tissue.

### Purpose

The module provides a complete pipeline for:
- Training U-Net models for multi-class semantic segmentation
- Running inference on new images
- Evaluating segmentation quality with comprehensive metrics
- Exporting models for deployment (TorchScript, TensorFlow Lite)

### Segmentation Classes (6-class)

The model segments neural structures into 6 classes:
1. **Mitochondria**
2. **Post-synaptic densities (PSD)**
3. **Vesicles**
4. **Axon**
5. **Boundaries**
6. **Mitochondria boundaries**

## Project Structure

```
segmentation/
├── src/                          # Core source code
│   ├── models.py                 # U-Net variants (UNet, TinyUNet, MobileUNet, Lars76UNet)
│   ├── model_block.py            # Building blocks (DoubleConv, Down, Up, UpMod, OutConv)
│   ├── pipeliner.py              # Training/inference pipeline manager
│   ├── losses.py                 # Loss functions (Dice, BCE, MSE, Huber, custom)
│   ├── activation_function.py    # Activation functions (sigmoid, softsign, arctan, etc.)
│   ├── prepare_data.py           # Data preparation utilities
│   ├── tilingImages.py           # Image tiling for large images
│   ├── comparison.py             # Metrics calculation and comparison
│   ├── test.py                   # Testing utilities
│   ├── test_metric.py            # Metric implementations
│   └── history.py                # Training history tracking
│
├── converters/                   # Model export tools
│   ├── convert_to_torchscript.py # Export to TorchScript (.pt)
│   └── torch_to_tflite.py        # Convert to TensorFlow Lite
│
├── test_data/                    # Test data and pretrained models
│   ├── img_in/                   # Test input images
│   ├── img_etal/                 # Ground truth masks
│   └── model_data/               # Pretrained weights & configs
│
├── converted_models/             # Output directory for converted models
│
├── gradio_interface_segmentation.py  # Gradio web UI for inference
├── InterectiveTest.ipynb             # Jupyter notebook for experimentation
├── model_definition.py               # Standalone model definitions
├── check_model.py                    # Model inspection utility
├── load_weights.py                   # Weight loading utility
├── expand_image.py                   # Image expansion utility (2x2 tiling)
├── press.py                          # Visualization/testing script
└── PROJECT_SUMMARY_RU.md             # Detailed Russian documentation
```

## Technologies

| Category | Technologies |
|----------|-------------|
| **Deep Learning** | PyTorch, segmentation_models_pytorch (SMP) |
| **Image Processing** | OpenCV, scikit-image, TorchVision |
| **UI/Visualization** | Gradio, Jupyter, matplotlib |
| **Utilities** | tqdm, numpy, joblib |

## Available Models

All models are defined in `src/models.py`:

| Model | Architecture | Channels | Parameters | Use Case |
|-------|-------------|----------|------------|----------|
| `UNet` | Standard U-Net | 64→128→256→512→1024 | ~31M | General purpose, high accuracy |
| `Tiny_unet` | Lightweight U-Net | 16→32→64→128 | ~0.5M | Fast inference, low memory |
| `Tiny_unet_v3` | Modified TinyUNet | 32→32→64→128→256 | ~2M | Balanced speed/accuracy |
| `MobileUNet` | MobileNetV2-based | Inverted residual blocks | ~2M | Mobile deployment |
| `Lars76_unet` | ResNet34 encoder (SMP) | Pretrained ImageNet | ~11M | High accuracy (research) |

### Model Building Blocks (`src/model_block.py`)

- **DoubleConv**: Convolution → BatchNorm → ReLU × 2
- **Down**: MaxPool → DoubleConv (downsampling)
- **Up**: Upsample → Concatenate → DoubleConv (upsampling with skip connections)
- **UpMod**: Modified Up block with flexible channel handling
- **OutConv**: 1×1 convolution for output

## Building and Running

### Install Dependencies

```bash
cd D:\Repozitories\UnetClassApplication\pytorch
pip install -r requirements.txt
```

**Key dependencies:**
- opencv-python
- torch, torchvision, torch-optimizer
- segmentation_models_pytorch (SMP)
- gradio
- jupyter
- tqdm

### Quick Start

**1. Run Gradio Web Interface (Recommended for testing):**
```bash
python gradio_interface_segmentation.py
```
Opens a web UI for interactive segmentation with metric calculation.

**2. Interactive Testing (Jupyter Notebook):**
```bash
jupyter notebook InterectiveTest.ipynb
```

**3. Model Inspection:**
```bash
python check_model.py          # Inspect model weights structure
python load_weights.py         # Load and test pretrained weights
```

**4. Export to TorchScript:**
```bash
python converters/convert_to_torchscript.py
```

**5. Convert to TensorFlow Lite:**
```bash
python converters/torch_to_tflite.py
```

### Training Pipeline

Training is managed through the `Pipeliner` class (`src/pipeliner.py`):

```python
from src.pipeliner import Pipeliner

# Initialize pipeline
model = Pipeliner(
    model_type="tiny_unet_v3",
    num_classes=6,
    num_channel=1,
    device="cuda",
    last_activation_name="sigmoid_activation",
    task_mode="segmentation"
)

# Configure training parameters via config JSON
# Run training with custom training script
```

**Configuration files** are stored in `test_data/model_data/` as JSON files with structure:

```json
{
  "model": {
    "experiment_type": "segmentation",
    "type_model": "tiny_unet_v3",
    "last_activation": "sigmoid_activation"
  },
  "train": {
    "mask_name_label_list": ["mitochondria", "PSD", "vesicles", "axon", "boundaries", "mitochondrial boundaries"],
    "num_class": 6,
    "batch_size": 7,
    "num_epochs": 200,
    "optimizer": "Adam",
    "loss": "DiceLossMulticlass",
    "lr_scheduler": "lr_scheduler_200"
  },
  "augmentation": {
    "rotation_range": 7,
    "width_shift_range": 0.05,
    "height_shift_range": 0.05,
    "zoom_range": 0.1,
    "horizontal_flip": true,
    "vertical_flip": true,
    "noise_limit": 3
  },
  "img_transform_data": {
    "color_mode_img": "gray",
    "target_size": [256, 256]
  }
}
```

## Key Features

### Training Pipeline (`src/pipeliner.py`)

- **Multi-class segmentation** with class balancing
- **Data augmentation**: rotation, shift, zoom, flip, noise injection
- **Learning rate scheduling** with warm restarts
- **Early stopping** support
- **Validation split** with crossover subsampling
- **Mixed precision** ready
- **Task modes**: segmentation, img2img, diffusion

### Loss Functions (`src/losses.py`)

| Loss | Class | Description |
|------|-------|-------------|
| `DiceLoss` | Binary | Standard Dice coefficient loss |
| `DiceLossMulticlass` | Multi-class | Per-class Dice with weighting |
| `BCELoss` / `BCELossMulticlass` | Binary/Multi | Binary Cross-Entropy |
| `MSELoss` / `MSELossMulticlass` | Binary/Multi | Mean Squared Error |
| `HuberLoss` | Both | Robust regression loss |
| `LossDistance2Nearest` | Custom | Distance-based loss |

**Custom activations with logits:**
- `softsign_with_logits` - SoftSign activation
- `inv_square_with_logits` - Inverse square activation

### Metrics (`src/comparison.py`, `src/test_metric.py`)

- **Dice coefficient** (per-class and average)
- **Jaccard index** (IoU)
- **Precision, Recall, F-score**
- **Accuracy**
- **Cluster-based metrics**

### Data Handling

**Image Tiling (`src/tilingImages.py`):**
- Split large images into tiles for processing
- Configurable overlap to reduce edge artifacts
- Reconstruct predictions from tiles

**Data Preparation (`src/prepare_data.py`):**
- Grayscale/RGB input support
- Multi-format: PNG, JPG, JPEG
- Normalization to [0, 1] range
- Batch processing utilities

### Model Export

| Format | Script | Output |
|--------|--------|--------|
| **TorchScript** | `converters/convert_to_torchscript.py` | `.pt` file |
| **TensorFlow Lite** | `converters/torch_to_tflite.py` | `.tflite` file |
| **Pipeline (joblib)** | Manual with `joblib.dump()` | `.pkl` file |
| **Weights only** | `torch.save(model.state_dict(), ...)` | `.pth` file |

## Development Conventions

### Code Style
- **Python**: PEP 8 compliant
- **Naming**: Snake_case for functions/variables, CamelCase for classes
- **Type hints**: Used in newer code (e.g., `src/losses.py`)

### Module Structure
- Models defined in `models.py`
- Building blocks in `model_block.py`
- Pipeline logic in `pipeliner.py`
- Losses in `losses.py`
- Data utilities in `prepare_data.py`

### File Naming
- **Models**: `model_by_config_<config_name>_<model_type>.pth`
- **Configs**: `config_<purpose>_<model_type>.json`
- **Histories**: `history_<model_name>.json`
- **TorchScript**: `traced_model.pt`

## Testing

**Test utilities** are in `src/test.py`:

```python
from src.test import getPipliner, test_data

# Load model from config
model, name_model = getPipliner(
    config_dir="test_data/model_data/",
    config_name="config_diffusion_data_42_slices_6_classes_dataset_mix_6_classes_seed_1466947709_tiny_unet_v3",
    device='cpu'
)

# Run inference
predict_img_list, predict_name_list = test_data(
    model,
    dataset_for_predict,
    save_mask_dir=None,
    tiled_data={"size": 256, "overlap": 128},  # Optional tiling
    batch_size=1
)
```

## Pretrained Models

Available in `test_data/model_data/`:

| File | Description |
|------|-------------|
| `model_by_config_diffusion_data_42_slices_6_classes_dataset_mix_6_classes_seed_1466947709_tiny_unet_v3.pth` | TinyUNet v3 trained on mixed dataset |
| `model_by_config_diffusion_data_42_slices_6_classes_dataset_diff_6_classes_seed_2137103169_tiny_unet_v3.pth` | TinyUNet v3 trained on diffusion dataset |
| `traced_model.pt` | TorchScript version for deployment |

## Usage Examples

### Basic Inference

```python
import cv2
import torch
from src.pipeliner import Pipeliner
from src.prepare_data import to_0_1_format_img

# Load model
model = Pipeliner(
    model_type="tiny_unet_v3",
    num_classes=6,
    num_channel=1,
    device="cpu"
)
model.model.load_state_dict(torch.load("test_data/model_data/model.pth", map_location="cpu"))
model.model.eval()

# Load and preprocess image
img = cv2.imread("image.png", 0)
img_normalized = to_0_1_format_img(img)
input_tensor = torch.from_numpy(img_normalized).unsqueeze(0).unsqueeze(0).float()

# Run inference
with torch.no_grad():
    output = model.model(input_tensor)
    prediction = model.last_activation_fun(output)
```

### Tiled Inference for Large Images

```python
from src.tilingImages import split_image, glit_image

# Split image
tiles, (rows, cols) = split_image(img, size=256, overlap=128, save_dir=None)

# Process each tile
predictions = []
for tile in tiles:
    input_tensor = torch.from_numpy(tile).unsqueeze(0).unsqueeze(0).float()
    with torch.no_grad():
        pred = model.model(input_tensor)
        predictions.append(pred.squeeze().numpy())

# Reconstruct
result = glit_image(predictions, out_size=img.shape, tile_info=(rows, cols), overlap=128)
```

## Important Notes

1. **Primary Development**: This is the active PyTorch implementation (TensorFlow version exists in parent `../tensorflow/`)
2. **GPU Support**: Code checks for CUDA availability (`torch.cuda.is_available()`)
3. **Memory Management**: Large images are processed using tiling to fit in memory
4. **Overlap Handling**: Recommended overlap is 50% of tile size (e.g., 128px for 256px tiles)
5. **Activation Functions**: Output activation is configurable (sigmoid default for multi-label)

## Related Documentation

- `PROJECT_SUMMARY_RU.md` - Detailed Russian documentation
- `../QWEN.md` - PyTorch parent module overview
- `../../QWEN.md` - Full project overview (includes TensorFlow & Android)

## Troubleshooting

| Issue | Solution |
|-------|----------|
| **CUDA out of memory** | Reduce batch_size or use gradient accumulation |
| **Missing class masks** | Empty masks are created automatically (zeros) |
| **Tiled inference artifacts** | Increase overlap parameter (try 50% of tile size) |
| **Model loading errors** | Verify TorchScript export compatibility, check device mapping |
| **Shape mismatch** | Ensure input is (B, C, H, W) format with C=1 for grayscale |
