# QWEN.md - UnetClassApplication Project Context

## Project Overview

**Name:** UnetClassApplication
**Type:** Multi-framework machine learning project for image segmentation
**Primary Purpose:** U-Net based image segmentation for biomedical images, specifically segmentation of mouse brain images obtained using electron microscopy and generation of electron microscope images using diffusion models. The project also supports image-to-image translation tasks.

The project combines both Android application development (using PyTorch Android) and Python-based model training and inference. It focuses on 6-class segmentation of neural structures:
1. Mitochondria
2. Postsynaptic densities (PSD)
3. Vesicles
4. Axon
5. Borders
6. Mitochondrial borders

### Key Features
- **Multi-framework implementation**: Both PyTorch and TensorFlow implementations
- **Multi-class segmentation**: Handles 6 different classes of neural structures
- **Multiple U-Net architectures**: Standard, lightweight, and mobile-optimized U-Net models
- **Tile-based processing**: For large images that don't fit in memory
- **Web interface**: Gradio-based web interface for interactive segmentation
- **Cross-platform**: Android application with PyTorch Mobile integration
- **Image-to-image translation capabilities**
- **Diffusion model for image generation**

## Project Architecture

### Directory Structure
```
UnetClassApplication/
├── app/                    # Android application module
├── pytorch/                # PyTorch implementation
│   ├── classification/     # Classification models
│   └── segmentation/       # Segmentation models and tools
│       ├── src/            # Source code files
│       ├── test_data/      # Test images and reference masks
│       └── [main files]    # Core Python scripts
├── tensorflow/             # TensorFlow implementation
├── build.gradle.kts        # Gradle build file
└── settings.gradle.kts     # Gradle settings
```

### Primary Implementation (PyTorch Segmentation)
The primary implementation is in the `pytorch/segmentation/` directory and includes:
- Multiple U-Net architectures (standard, lightweight, mobile, custom v3)
- Training pipeline with support for various loss functions and metrics
- Tile-based processing for large images
- Comprehensive evaluation framework with Dice, Jaccard, accuracy metrics
- Gradio web interface for interactive segmentation
- Jupyter notebook for interactive testing

### Android Application
- Built with Kotlin and Android Studio
- Uses PyTorch Android library (version 2.1.0) for on-device inference
- Supports ARM architectures (armeabi-v7a, arm64-v8a)

### TensorFlow Implementation
Located in `tensorflow/`, contains legacy implementation:
- Model definitions, training scripts, and configuration files
- Data generators and evaluation metrics

## Technologies Used

- **Primary Frameworks**: PyTorch, TensorFlow
- **Programming Languages**: Python, Kotlin
- **Mobile Platform**: Android
- **Machine Learning**: Semantic segmentation using U-Net architectures
- **Development Tools**: Android Studio, Jupyter Notebooks, Gradle
- **UI Framework**: Gradio (for web interface)

## Building and Running

### Python (Training/Inference)
1. Install dependencies:
```bash
pip install -r pytorch/requirements.txt
```

2. For single training run:
```bash
python3.11 trainer.py -c segmentation/configs/proportion_data/config_proportion.json
```

3. For series of training with different parameters:
```bash
python3.11 trainSeriesExpts.py -c "segmentation/configs/syntetic_diffusion/multi_config_test_only_synt_6_classes.json" -s > "output.txt"
```

4. Interactive segmentation with Gradio:
```bash
python gradio_interface_segmentation.py
```

### Android Application
The Android app can be built using Gradle:
```bash
./gradlew build

# Install on connected device
./gradlew installDebug
```

## Development Conventions

### Code Style
- Python code follows standard conventions with type hints where appropriate
- Kotlin code follows official Android/Kotlin style guidelines
- Configuration files use JSON format

### Project Organization
- The project maintains both TensorFlow (legacy) and PyTorch (current) implementations
- The README clearly indicates PyTorch as the "latest version"
- Android app uses PyTorch Mobile for on-device inference
- Data sets are stored in `segmentation/data` with multiclass annotations

### Model Architectures
- Standard U-Net with skip connections
- Lightweight U-Net for faster inference
- Mobile U-Net optimized for mobile deployment
- Custom U-Net v3 with modified Up blocks
- Lars76 U-Net based on ResNet34 backbone

## Key Files and Components

### Android App
- `app/build.gradle.kts`: Contains PyTorch Mobile dependencies
- Android app includes native PyTorch libraries for mobile inference

### PyTorch Segmentation
- **models.py**: Contains multiple U-Net variants (Standard, Small, Mobile, Lars76 U-Net based on ResNet34)
- **model_block.py**: Core building blocks (DoubleConv, Down, Up, OutConv)
- **pipeliner.py**: Main training/prediction pipeline
- **src/**: Additional utilities for activation functions, losses, data preparation, etc.
- **gradio_interface_segmentation.py**: Web interface for interactive segmentation
- **InterectiveTest.ipynb**: Jupyter notebook for interactive testing
- `press.py`: Testing script with visualization
- `PROJECT_SUMMARY_RU.md`: Detailed Russian documentation

### Configuration
- JSON-based configuration files for training parameters
- Support for overriding config parameters via command-line arguments
- Multi-config support for running series of experiments

## Special Features

### Multiple U-Net Architectures:
- Standard U-Net with skip connections
- Lightweight Small U-Net for faster inference
- Mobile U-Net optimized for mobile deployment
- Custom v3 architecture with modified Up blocks

### Advanced Training Capabilities:
- Multiple activation functions (sigmoid, softsign, arctangent, etc.)
- Multiple loss functions (Dice, BCE, MSE, Huber)
- Multi-class classification support
- Class balancing
- Tiling support for large images

### Tile-based Large Image Processing
- Divides large images into tiles to handle memory constraints
- Handles overlaps between tiles to reduce edge artifacts
- Reconstructs final image from tile predictions

### Comprehensive Evaluation
- Multiple metrics: Dice, Jaccard, Accuracy, Precision, Recall, F-score
- Custom clustering metrics
- Detailed reporting in text and CSV formats

### Model Export
- TorchScript export (both standard .pt and mobile .ptl)
- Pipeline with configuration and weights

### User Interface
- Gradio web interface for interactive segmentation
- Jupyter notebook for exploration
- Command-line scripts

## Usage Notes

### For Server/Cluster Execution
1. When redirecting print output to a file, add the -s argument to disable tqdm progress bars
2. Prefer using sbatch over srun for non-interactive execution to avoid connection interruptions
3. Example sbatch scripts are located in pytorch/sbatch_scripts

### Dataset Location
- Dataset with multiclass annotations is located at `segmentation/data` with the name "ITMM"

The project appears to be specifically designed for electron microscopy neural image segmentation with a focus on biological structures, with the model trained on a specific dataset that can be used for inference of new images via Gradio interface or Python scripts.