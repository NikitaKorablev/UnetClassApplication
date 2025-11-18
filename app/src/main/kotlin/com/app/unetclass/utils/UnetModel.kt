package com.app.unetclass.utils

import android.content.Context
import android.graphics.Bitmap
import com.app.unetclass.data.ImageProcessor
import com.app.unetclass.data.ImageStitcher
import com.app.unetclass.domain.ISegmentationUseCase
import com.app.unetclass.models.SegmentationResult
import com.app.unetclass.models.Tile
import com.app.unetclass.utils.ResultState
import com.app.unetclass.models.InferenceModel
import org.pytorch.Tensor

class UnetModel(
    private val context: Context
) : ISegmentationUseCase {
    private val imageProcessor: ImageProcessor = ImageProcessor(256, 128)
    private val imageStitcher: ImageStitcher = ImageStitcher(128, 6)
    private val model: InferenceModel = InferenceModel(context)

    override suspend fun startSegmentation(bitmap: Bitmap): ResultState<SegmentationResult, String> {
        val startTime = System.currentTimeMillis()

        try {
            // --- 1. Нарезка и подготовка тензоров (распил) ---
            val (tiles, tensors) = cutTensor(bitmap)
            // --- 2. Инференс (predict) ---
            val outputTensors = model.predictBatch(tensors)
            // --- 3. Сборка (сборка) и Постобработка ---
            val result = imageStitcher.stitchMasks(
                outputTensors,
                tiles,
                bitmap.width,
                bitmap.height
            )

            // Сохранение результатов
            val success = imageStitcher.saveResults(result, context)
            val outputPath = imageStitcher.getLastSavedPath() // Получаем путь к сохраненным результатам

            if (!success) {
                return ResultState.Error("Failed to save results")
            }

            val totalTime = System.currentTimeMillis() - startTime

            return ResultState.Success(
                SegmentationResult(
                    bitmap = result.unitedMask,
                    outputPath = outputPath,
                    totalTimeMs = totalTime
                )
            )
        } catch (err: Exception) {
            return ResultState.Error(err.message.toString())
        }
    }

    private fun cutTensor(bitmap: Bitmap): Pair<List<Tile>, List<Tensor>> {
        val tiles = imageProcessor.splitImageIntoTiles(bitmap)
        val tensors = imageProcessor.getTensorsForInference(tiles)

        return Pair(tiles, tensors)
    }

    fun getLastSavedPath(): String {
        return imageStitcher.getLastSavedPath()
    }
}