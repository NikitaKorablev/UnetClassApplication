# Qwen Code Context File

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

## Project Structure

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

## Key Components

### Android Application
- Built with Kotlin and Android Studio
- Uses PyTorch Android library (version 2.1.0) for on-device inference
- Supports ARM architectures (armeabi-v7a, arm64-v8a)

### PyTorch Implementation
Located in `pytorch/segmentation/`, this is the main implementation:
- **models.py**: Contains multiple U-Net variants (Standard, Small, Mobile, Lars76 U-Net based on ResNet34)
- **model_block.py**: Core building blocks (DoubleConv, Down, Up, OutConv)
- **pipeliner.py**: Main training/prediction pipeline
- **src/**: Additional utilities for activation functions, losses, data preparation, etc.
- **gradio_interface_segmentation.py**: Web interface for interactive segmentation
- **InterectiveTest.ipynb**: Jupyter notebook for interactive testing

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
python3.11 trainSeriesExpts.py -c "segmentation/configs/syntetic_diffusion/multi_config_test_only_synt_6_classes.json" -s > "five_script_output_5.txt"
```

4. Interactive segmentation with Gradio:
```bash
python gradio_interface_segmentation.py
```

### Android Application
The Android app can be built using Gradle:
```bash
./gradlew build
```

## Key Features

1. **Multiple U-Net Architectures**:
   - Standard U-Net with skip connections
   - Lightweight Small U-Net for faster inference
   - Mobile U-Net optimized for mobile deployment
   - Custom v3 architecture with modified Up blocks

2. **Advanced Training Capabilities**:
   - Multiple activation functions (sigmoid, softsign, arctangent, etc.)
   - Multiple loss functions (Dice, BCE, MSE, Huber)
   - Multi-class classification support
   - Class balancing
   - Tiling support for large images

3. **Tiling Support**:
   - Process large images by splitting into tiles
   - Handle overlaps to reduce edge artifacts
   - Reconstruct from tile predictions

4. **Comprehensive Evaluation**:
   - Multiple metrics: Dice, Jaccard, Accuracy, Precision, Recall, F-score
   - Custom clustering metrics
   - Detailed reporting in text and CSV formats

5. **Model Export**:
   - TorchScript export (both standard .pt and mobile .ptl)
   - Pipeline with configuration and weights

6. **User Interface**:
   - Gradio web interface for interactive segmentation
   - Jupyter notebook for exploration
   - Command-line scripts

## Special Notes for Server/Cluster Usage
1. When redirecting print output to a file, add the -s argument to disable tqdm progress bars
2. Prefer using sbatch over srun for non-interactive execution to avoid connection interruptions
3. Example sbatch scripts are located in pytorch/sbatch_scripts

## Development Conventions

- Python code primarily supports 6-class segmentation of neural structures
- Training configuration is handled via JSON config files
- Model weights are stored in test_data/model_data/
- The project follows PyTorch best practices for segmentation tasks
- Android app uses PyTorch Mobile for on-device inference

## Key Directories and Files

- `pytorch/segmentation/PROJECT_SUMMARY_RU.md` - Detailed Russian documentation
- `pytorch/segmentation/src/` - Core Python source code
- `pytorch/segmentation/trainer.py` - Main training script
- `app/src/main/kotlin/` - Android application source code
- `pytorch/requirements.txt` - Python dependencies

## Data Organization

- Datasets are stored in `segmentation/data` with multi-class annotations
- Test data is located in `pytorch/segmentation/test_data/`
- Images and reference masks are organized in subdirectories