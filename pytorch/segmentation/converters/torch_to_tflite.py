import litert_torch
import torch
import numpy as np
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

    # 2. Создаем пример входных данных (для трассировки графа)
    sample_input = torch.randn(1, 1, 256, 256)

    # 3. Конвертируем напрямую в формат LiteRT
    edge_model = litert_torch.convert(model, (sample_input,))

    # Сохраняем результат
    edge_model.export("resnet.tflite")


    # 2. Прогон через PyTorch
    with torch.no_grad():
        torch_output = model(sample_input).numpy()

    edge_model = litert_torch.load("resnet.tflite")
    tflite_output = edge_model(sample_input)

    np.testing.assert_allclose(torch_output, tflite_output, rtol=1e-05, atol=1e-05)
    print("Проверка пройдена!")




