# UnetClassApplication - Project Context

## Overview

This is a comprehensive **semantic segmentation project** using U-Net architectures for analyzing biomedical images, specifically designed for segmenting neural structures from electron microscope images of mouse brain tissue. The project also includes diffusion model capabilities for image generation.

The repository contains three main components:
1. **PyTorch Implementation** (`pytorch/`) - Latest version with training/inference pipelines
2. **TensorFlow/Keras Implementation** (`tensorflow/`) - Legacy implementation
3. **Android Application** (`UnetApplication/`) - Mobile app for on-device inference

## Project Structure

```
UnetClassApplication/
├── pytorch/                      # PyTorch implementation (latest)
│   ├── classification/           # Image classification module
│   ├── segmentation/             # Main segmentation module
│   │   ├── src/                  # Source code
│   │   │   ├── models.py         # U-Net variants (UNet, TinyUNet, MobileUNet, Lars76UNet)
│   │   │   ├── pipeliner.py      # Training/inference pipeline
│   │   │   ├── model_block.py    # Building blocks (DoubleConv, Down, Up)
│   │   │   ├── losses.py         # Loss functions (Dice, BCE, etc.)
│   │   │   └── prepare_data.py   # Data preparation utilities
│   │   ├── configs/              # Training configuration files
│   │   └── test_data/            # Test images and pretrained weights
│   └── requirements.txt
│
├── tensorflow/                   # TensorFlow/Keras implementation (legacy)
│   ├── model.py                  # U-Net model definitions
│   ├── trainModel.py             # Training script
│   ├── dataGenerator.py          # Data loading and augmentation
│   ├── metrics.py                # Dice coefficient metrics
│   └── *.json                    # Configuration files
│
└── UnetApplication/              # Android application
    ├── app/                      # Main app module
    ├── core/                     # Core modules
    │   ├── unet/                 # U-Net inference engine
    │   ├── model/                # Data models
    │   ├── domain/               # Domain logic
    │   ├── datastore/            # Data persistence
    │   └── features/             # Feature modules
    └── build.gradle.kts
```

## Technologies

### PyTorch Stack
- **Deep Learning**: PyTorch, segmentation_models_pytorch (SMP)
- **Image Processing**: OpenCV, scikit-image, TorchVision
- **UI/Visualization**: Gradio, Jupyter, matplotlib
- **Utilities**: tqdm, joblib, numpy

### TensorFlow Stack (Legacy)
- **Deep Learning**: TensorFlow, Keras
- **Image Processing**: OpenCV, albumentations
- **Utilities**: keras-preprocessing

### Android Stack
- **Language**: Kotlin
- **Build System**: Gradle (Kotlin DSL)
- **Architecture**: MVVM with Hilt DI
- **Libraries**: 
  - AndroidX (Lifecycle, ViewModel, Activity KTX)
  - Material Design Components
  - Glide (image loading)
  - PhotoView (image zooming)
  - PyTorch Mobile (org.pytorch)

## Available Models

### PyTorch Models (`pytorch/segmentation/src/models.py`)
| Model | Description | Use Case |
|-------|-------------|----------|
| `UNet` | Standard U-Net (64→128→256→512→1024) | General purpose |
| `Tiny_unet` | Lightweight (16→32→64→128) | Fast inference |
| `Tiny_unet_v3` | Modified TinyUNet (32→32→64→128→256) | Balanced performance |
| `MobileUNet` | MobileNetV2-based encoder | Mobile deployment |
| `Lars76_unet` | ResNet34 encoder (via SMP) | High accuracy |

### TensorFlow Models (`tensorflow/model.py`)
- `unet` - Standard U-Net
- `tiny_unet` - Lightweight variant
- `tiny_unet_v3` - Improved tiny version
- `mobile_unet_v2` - MobileNetV2-based

## Segmentation Classes

The model performs **6-class semantic segmentation**:
1. Mitochondria
2. Post-synaptic densities (PSD)
3. Vesicles
4. Axon
5. Boundaries
6. Mitochondria boundaries

## Building and Running

### PyTorch

**Install Dependencies:**
```bash
cd pytorch
pip install -r requirements.txt
```

**Run Training (single config):**
```bash
python3.11 trainer.py -c segmentation/configs/proportion_data/config_proportion.json
```

**Run Training Series:**
```bash
python3.11 trainSeriesExpts.py -c "segmentation/configs/syntetic_diffusion/multi_config_test_only_synt_6_classes.json" -s > output.txt
```

**Interactive Testing (Jupyter):**
```bash
jupyter notebook segmentation/InterectiveTest.ipynb
```

**Gradio Web Interface:**
```bash
python segmentation/gradio_interface_segmentation.py
```

**Export to TorchScript:**
```bash
python segmentation/convert_to_torchscript.py
```

### TensorFlow (Legacy)

**Install Dependencies:**
```bash
cd tensorflow
pip install tensorflow keras albumentations
```

**Run Training:**
```bash
python trainModel.py -c config1.json
```

### Android Application

**Build:**
```bash
cd UnetApplication
./gradlew assembleDebug
```

**Requirements:**
- Android Studio Arctic Fox or later
- JDK 17
- Android SDK 24+ (min), 34 (target)

## Configuration

Training configurations are JSON files with the following structure:

```json
{
  "train": {
    "dir_img_path": "path/to/images",
    "dir_mask_path_without_name": "path/to/masks",
    "num_class": 6,
    "batch_size": 7,
    "num_epochs": 200
  },
  "model": {
    "type_model": "tiny_unet_v3",
    "optimizer": "Adam",
    "loss": "dice_distance"
  },
  "img_transform_data": {
    "color_mode_img": "gray",
    "target_size": [256, 256]
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
  "generator_config": {
    "type_gen": "default",
    "mode": "train",
    "augment": true,
    "shuffle": true,
    "subsampling": "crossover"
  }
}
```

## Key Features

### Training Pipeline
- **Multi-class segmentation** with class balancing
- **Data augmentation**: rotation, shift, zoom, flip, noise
- **Learning rate scheduling** with warm restarts
- **Early stopping** support
- **Validation split** with crossover subsampling
- **Mixed precision** ready

### Loss Functions
- Dice loss (`dice_distance`)
- Balanced Cross-Entropy (`BCE`)
- Combined losses supported
- Custom losses: `DiceLoss`, `LossMulticlassDice`, `HuberLoss`, `LossDistance2Nearest`

### Metrics
- Dice coefficient (per-class and average)
- Jaccard index
- Precision, Recall, F-score
- Accuracy

### Data Handling
- **Tile-based processing** for large images
- **Overlap handling** to reduce edge artifacts
- **Multi-format support**: PNG, JPG, JPEG
- **Grayscale/RGB** input modes

### Model Export
- TorchScript (`.pt`, `.ptl` for mobile)
- Pipeline packaging with joblib (`.pkl`)
- Weights-only export (`.pth`)

## Server/Cluster Usage

For SLURM-based clusters:
```bash
# Submit job (recommended)
sbatch scripts/train_script.sh

# Run interactively (not recommended for long jobs)
srun python trainer.py -c config.json -s
```

**Notes:**
- Use `-s` flag to disable tqdm progress bars when redirecting output
- Use `sbatch` instead of `srun` for long-running jobs to avoid disconnection

## Development Conventions

### Code Style
- **Python**: PEP 8 compliant
- **Kotlin**: Android/Kotlin style guide
- **Naming**: Snake_case for Python, CamelCase for Kotlin

### Testing
- PyTorch: Test scripts in `segmentation/src/test.py`
- Android: JUnit + Espresso tests in `app/src/test/` and `app/src/androidTest/`

### Version Control
- Main development in `pytorch/` branch
- TensorFlow code is legacy/maintenance mode
- Android app follows semantic versioning

## File Naming Conventions

- **Images**: `image_*.png`, `img_*.jpg`
- **Masks**: `mask_<classname>_<imagename>.png`
- **Models**: `<model_type>_<config_name>.pth`
- **Configs**: `config_<purpose>.json`
- **Histories**: `history_<model_name>.json`

## Dataset Structure

```
data/
├── train/
│   ├── origin/           # Input images
│   └── <class_name>/     # Class-specific masks
│       └── mask_*.png
└── validation/
    ├── origin/
    └── <class_name>/
        └── mask_*.png
```

## Android Architecture

The Android app follows a clean architecture with modular structure:

```
UnetApplication/
├── app/                      # Main application module
│   ├── presentation/         # UI layer (Activities, ViewModels)
│   ├── domain/               # Use cases
│   └── models/               # App-level data models
├── core/
│   ├── unet/                 # U-Net inference engine (PyTorch Mobile)
│   ├── model/                # Shared data models
│   ├── domain/               # Domain logic
│   └── datastore/            # Data persistence (Room, DataStore)
└── features/
    └── transparency_settings/ # Feature module for overlay transparency
```

**Key Android Components:**
- `UnetModel.kt` - PyTorch Mobile inference wrapper with tile-based processing
- `MainViewModel.kt` - MVVM ViewModel for main screen
- `SegmentationUseCase.kt` - Domain use case for segmentation
- Dependency Injection via Hilt

## Important Notes

1. **Latest Version**: The PyTorch implementation is the current primary version
2. **Legacy Code**: TensorFlow directory contains older implementation
3. **Raw Scripts**: Directories `segmentation/`, `diffusion/`, `img2img/` contain auxiliary scripts that may need refactoring
4. **GPU Support**: PyTorch code checks for CUDA availability (`torch.cuda.is_available()`)
5. **Memory Management**: Large images are processed using tiling to fit in memory

## Troubleshooting

### Common Issues
- **CUDA out of memory**: Reduce batch_size or use gradient accumulation
- **Missing class masks**: Empty masks are created automatically (zeros)
- **Tiled inference artifacts**: Ensure overlap parameter is set correctly
- **Android model loading**: Verify TorchScript export compatibility

## Related Documentation
- `pytorch/segmentation/PROJECT_SUMMARY_RU.md` - Detailed Russian documentation
- `UnetApplication/technical_report.md` - Android app technical report
- Configuration examples in `tensorflow/*.json`
