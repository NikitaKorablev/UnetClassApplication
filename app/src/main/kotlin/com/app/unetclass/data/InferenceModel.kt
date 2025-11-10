package com.app.unetclass.data

import android.content.Context
import android.util.Log
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor

class InferenceModel(context: Context) {
    private val module: Module

    init {
        // Загрузка модели из assets (аналог getPipliner)
        // Предполагается, что модель экспортирована в TorchScript (.ptl)
        val modelPath = Utils.assetFilePath(context, MODEL_ASSET_NAME)
        module = Module.load(modelPath)

        // В вашем Python-коде используется device='cpu', PyTorch Mobile делает это автоматически.
        Log.i(TAG, "PyTorch Mobile Model loaded successfully from $MODEL_ASSET_NAME")
    }

    /**
     * Выполняет предсказание для батча тензоров.
     * Аналог model_pipeliner.predict(img_generator)
     */
    fun predictBatch(tensors: List<Tensor>): List<Tensor> {
        // PyTorch Mobile может принимать батч, если сконкатенировать тензоры.
        // Однако для простоты и соответствия batch_size=1 (в вашем коде),
        // выполняем инференс по одному:

        return tensors.map { tensor ->
            // IValue.from(tensor) создает аргумент для forward()
            // .output.toTensor() извлекает тензор из результата
            module.forward(IValue.from(tensor)).toTensor()
        }
    }

    companion object {
        const val TAG = "InferenceModel"
        const val MODEL_ASSET_NAME = "traced_model.pt"
    }
}