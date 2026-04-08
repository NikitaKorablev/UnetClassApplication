# PyTorch U-Net Segmentation Project

## Overview

This is a **PyTorch-based semantic segmentation project** using U-Net architectures for biomedical image analysis, specifically designed for segmenting neural structures from electron microscope images. The project also includes image classification capabilities.

### Main Components

| Directory | Purpose |
|-----------|---------|
| `segmentation/` | Main U-Net segmentation module with training/inference pipelines |
| `classification/` | Image classification module using VGG, ResNet, EfficientNet, DenseNet |

## Technologies

- **Deep Learning**: PyTorch, segmentation_models_pytorch (SMP)
- **Image Processing**: OpenCV, scikit-image, TorchVision
- **UI/Visualization**: Gradio, Jupyter, matplotlib
- **Utilities**: tqdm, numpy, joblib

## Project Structure

```
pytorch/
├── segmentation/
│   ├── src/
│   │   ├── models.py           # U-Net variants (UNet, TinyUNet, MobileUNet, Lars76UNet)
│   │   ├── model_block.py      # Building blocks (DoubleConv, Down, Up, OutConv)
│   │   ├── pipeliner.py        # Training/inference pipeline manager
│   │   ├── losses.py           # Loss functions (Dice, BCE, MSE, Huber)
│   │   ├── activation_function.py
│   │   ├── prepare_data.py     # Data preparation utilities
│   │   ├── test.py             # Testing utilities
│   │   └── tilingImages.py     # Image tiling for large images
│   ├── converters/
│   │   ├── convert_to_torchscript.py
│   │   ├── torch_to_tflite.py
│   │   └── litert-torch-0.8.0/  # LiteRT converter
│   ├── test_data/
│   │   ├── img_etal/           # Ground truth masks
│   │   ├── img_in/             # Test input images
│   │   └── model_data/         # Pretrained weights & configs
│   ├── gradio_interface_segmentation.py
│   ├── InterectiveTest.ipynb
│   └── PROJECT_SUMMARY_RU.md
│
├── classification/
│   ├── src/
│   │   ├── models.py           # VGG, ResNet, EfficientNet, DenseNet wrappers
│   │   ├── metric.py           # Classification metrics
│   │   └── train_classificator.py
│   ├── gradio_interface.py
│   ├── test_classification.ipynb
│   └── names_for_test.json
│
└── requirements.txt
```

## Available Models

### Segmentation Models (`segmentation/src/models.py`)

| Model | Architecture | Channels | Use Case |
|-------|-------------|----------|----------|
| `UNet` | Standard U-Net | 64→128→256→512→1024 | General purpose |
| `Tiny_unet` | Lightweight | 16→32→64→128 | Fast inference |
| `Tiny_unet_v3` | Modified TinyUNet | 32→32→64→128→256 | Balanced performance |
| `MobileUNet` | MobileNetV2-based | Inverted residual blocks | Mobile deployment |
| `Lars76_unet` | ResNet34 encoder (SMP) | Pretrained ImageNet | High accuracy |

### Classification Models (`classification/src/models.py`)

- `Custom_VGG` - VGG-based (default: vgg11_bn)
- `Custom_ResNet` - ResNet50-based
- `Custom_EfficientNet` - EfficientNetB0-based
- `Custom_DenseNet` - DenseNet121-based

## Segmentation Classes

The model performs **6-class semantic segmentation**:
1. Mitochondria
2. Post-synaptic densities (PSD)
3. Vesicles
4. Axon
5. Boundaries
6. Mitochondria boundaries

## Building and Running

### Install Dependencies

```bash
cd pytorch
pip install -r requirements.txt
```

### Segmentation

**Run Gradio Web Interface:**
```bash
python segmentation/gradio_interface_segmentation.py
```

**Interactive Testing (Jupyter):**
```bash
jupyter notebook segmentation/InterectiveTest.ipynb
```

**Export to TorchScript:**
```bash
python segmentation/converters/convert_to_torchscript.py
```

**Convert to TensorFlow Lite:**
```bash
python segmentation/converters/torch_to_tflite.py
```

### Classification

**Run Gradio Web Interface:**
```bash
python classification/gradio_interface.py
```

## Configuration

Training configurations are JSON files stored in `test_data/model_data/`. Example structure:

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

### Training Pipeline (`segmentation/src/pipeliner.py`)
- Multi-class segmentation with class balancing
- Data augmentation: rotation, shift, zoom, flip, noise
- Learning rate scheduling with warm restarts
- Early stopping support
- Validation split with crossover subsampling
- Mixed precision ready

### Loss Functions (`segmentation/src/losses.py`)
- `DiceLoss` / `DiceLossMulticlass`
- `BCELoss` / `BCELossMulticlass`
- `MSELoss` / `MSELossMulticlass`
- `HuberLoss`
- `LossDistance2Nearest`

### Metrics
- Dice coefficient (per-class and average)
- Jaccard index
- Precision, Recall, F-score
- Accuracy

### Data Handling
- **Tile-based processing** for large images
- **Overlap handling** to reduce edge artifacts
- Multi-format support: PNG, JPG
- Grayscale/RGB input modes

### Model Export
- **TorchScript** (`.pt`, `.ptl` for mobile)
- **TensorFlow Lite** conversion support
- Weights-only export (`.pth`)

## Development Conventions

### Code Style
- **Python**: PEP 8 compliant
- **Naming**: Snake_case for functions/variables, CamelCase for classes

### Module Structure
- Models defined in `models.py`
- Building blocks in `model_block.py`
- Pipeline logic in `pipeliner.py`
- Losses in `losses.py`
- Data preparation in `prepare_data.py`

### File Naming
- **Models**: `<model_type>_<config_name>.pth`
- **Configs**: `config_<purpose>.json`
- **Histories**: `history_<model_name>.json`

## Testing

**Segmentation Test Script:**
```bash
python segmentation/src/test.py
```

**Classification Test:**
```bash
python classification/test_classification_script.py
```

## Important Notes

1. **Primary Development**: This `pytorch/` directory is the current primary version (TensorFlow implementation exists in parent `../tensorflow/`)
2. **GPU Support**: Code checks for CUDA availability (`torch.cuda.is_available()`)
3. **Memory Management**: Large images processed using tiling to fit in memory
4. **Pretrained Models**: Available in `segmentation/test_data/model_data/`

## Related Documentation

- `segmentation/PROJECT_SUMMARY_RU.md` - Detailed Russian documentation
- `../QWEN.md` - Root project overview (includes Android app)
