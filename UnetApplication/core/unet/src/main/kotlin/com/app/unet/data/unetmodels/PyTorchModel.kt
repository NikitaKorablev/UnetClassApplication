package com.app.unet.data.unetmodels

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.app.model.ResultState
import com.app.model.Tile
import com.app.unet.data.Utils
import com.app.unet.domain.UnetModel
import com.app.unet.domain.models.SegmentationResult
import com.app.unet.domain.usecases.SaveImageStitcherUseCase
import com.app.unet.domain.usecases.SplitImageIntoTilesUseCase
import com.app.unet.domain.usecases.StitchingImageUseCase
import com.app.unet.domain.usecases.TilesToTensorsUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import javax.inject.Inject

class PyTorchModel @Inject constructor(
    @ApplicationContext
    context: Context,
    private val stitchingImageUseCase: StitchingImageUseCase,
    private val splitImageIntoTilesUseCase: SplitImageIntoTilesUseCase,
    private val tilesToTensorsUseCase: TilesToTensorsUseCase,
    private val saveImageStitcherUseCase: SaveImageStitcherUseCase,
): UnetModel {
    private val module: Module
    init {
        val modelPath = Utils.assetFilePath(context, MODEL_ASSET_NAME)
        module = Module.load(modelPath)

        Log.d(TAG, "PyTorch Mobile Model loaded successfully from $MODEL_ASSET_NAME")
    }

    override fun startSegmentation(bitmap: Bitmap): ResultState<SegmentationResult, String> {
        val startTime = System.currentTimeMillis()

        try {
            // --- 1. Нарезка и подготовка тензоров (распил) ---
            val (tiles, tensors) = cutTensor(bitmap)

            // --- 2. Инференс (predict) ---
            val outputTensors = predictBatch(tensors)

            // --- 3. Сборка (сборка) и Постобработка ---
            val result = stitchingImageUseCase(
                outputTensors,
                tiles,
                bitmap.width,
                bitmap.height
            )

            // Сохранение результатов
            val outputPath = saveImageStitcherUseCase(result)
            outputPath ?: return ResultState.Error("Failed to save results")

            val totalTime = System.currentTimeMillis() - startTime
            return ResultState.Success(
                SegmentationResult(
                    bitmap = result.unitedMask,
                    outputPath = outputPath,
                    totalTimeMs = totalTime,
                    imageWidth = result.unitedMask.width,
                    imageHeight = result.unitedMask.height
                )
            )
        } catch (err: Exception) {
            return ResultState.Error(err.message.toString())
        }
    }

    /**
     * Выполняет предсказание для батча тензоров.
     * Аналог model_pipeliner.predict(img_generator)
     */
    private fun predictBatch(tensors: List<Tensor>): List<Tensor> {
        // PyTorch Mobile может принимать батч, если сконкатенировать тензоры.

        return tensors.map { tensor ->
            // IValue.from(tensor) создает аргумент для forward()
            // .output.toTensor() извлекает тензор из результата
            module.forward(IValue.from(tensor)).toTensor()
        }
    }

    private fun cutTensor(bitmap: Bitmap): Pair<List<Tile>, List<Tensor>> {
        val tiles = splitImageIntoTilesUseCase(bitmap)
        val tensors = tilesToTensorsUseCase(tiles)

        return Pair(tiles, tensors)
    }

    companion object {
        const val TAG = "PyTorchModel"
        const val MODEL_ASSET_NAME = "traced_model.pt"
    }
}