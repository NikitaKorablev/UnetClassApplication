import litert_torch
import torch
import numpy as np

import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))
from model_definition import Tiny_unet_v3

def loadModel() -> Tiny_unet_v3:
    model = Tiny_unet_v3(n_channels=1, n_classes=6)
    
    # Load the state dictionary from the .pth file
    state_dict = torch.load("test_data/model_data/model_by_config_diffusion_data_42_slices_6_classes_dataset_mix_6_classes_seed_1466947709_tiny_unet_v3.pth", 
                            map_location=torch.device('cpu'))
    
    # Load the weights into the model
    model.load_state_dict(state_dict)
    
    print("Model loaded successfully!")
    return model

if __name__ == "__main__":
    model = loadModel().eval()
    sample_input = torch.randn(1, 1, 256, 256)

    # 1. Определяем путь к папке
    output_dir = "converted_models"
    model_name = "tiny_unet_v3.tflite"

    # 2. Создаем папку, если её нет
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
        print(f"Папка {output_dir} создана.")

    full_path = os.path.join(output_dir, model_name)


    # 3. Конвертируем напрямую в формат LiteRT
    edge_model = litert_torch.convert(model, (sample_input,))
    edge_model.export(full_path)

    # 2. Прогон через PyTorch
    with torch.no_grad():
        torch_output = model(sample_input).numpy()

    edge_model = litert_torch.load(full_path)
    tflite_output = edge_model(sample_input)

    np.testing.assert_allclose(torch_output, tflite_output, rtol=1e-03, atol=1e-03)
    print("Проверка пройдена!")
