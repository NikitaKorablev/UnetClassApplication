package com.app.unetclass.utils

import android.content.Context
import android.graphics.Bitmap
import com.app.unetclass.data.ImageProcessor
import com.app.unetclass.data.ImageStitcher
import com.app.unetclass.models.InferenceModel
import com.app.unetclass.models.Tile
import org.pytorch.Tensor

class UnetModel(context: Context) {
    private val imageProcessor: ImageProcessor = ImageProcessor(256, 128)
    private val imageStitcher: ImageStitcher = ImageStitcher(128, 6)
    private val model: InferenceModel = InferenceModel(context)

    suspend fun startSegmentation(bitmap: Bitmap): ResultState<Bitmap, String> {
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

            return ResultState.Success(result.unitedMask)
        } catch (err: Exception) {
            return ResultState.Error(err.message.toString())
        }
    }

    private fun cutTensor(bitmap: Bitmap): Pair<List<Tile>, List<Tensor>> {
        val tiles = imageProcessor.splitImageIntoTiles(bitmap)
        val tensors = imageProcessor.getTensorsForInference(tiles)

        return Pair(tiles, tensors)
    }
}