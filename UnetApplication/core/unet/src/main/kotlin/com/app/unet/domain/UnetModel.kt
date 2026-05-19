package com.app.unet.domain

import com.app.model.ImageData
import com.app.model.PredictedClasses
import com.app.model.ResultState
import com.app.model.Tile
import com.app.unet.models.LabeledData
import com.app.unet.models.SegmentationResult
import kotlin.math.min

interface UnetModel {
    fun predict(inputImageData: ImageData): ResultState<LabeledData, String>

    /**
     * Освобождает ресурсы модели (Native Heap).
     */
    fun close()

    fun stitchTile(
        outputData: FloatArray,
        tileInfo: Tile, // или как называется объект в inputImageData.tiles
        finalMaskArray: Array<Array<FloatArray>>
    ) {
        val tileSize = Tile.SIZE // H или W (256)

        val outStartX = tileInfo.startX
        val outStartY = tileInfo.startY
        val outEndX = tileInfo.endX
        val outEndY = tileInfo.endY

        // Внутренние границы для уникальной области (получаем [64:192])
        val uniqueStart = Tile.SHIFT

        val tileYRange = uniqueStart until min(tileSize, outEndY - outStartY) - Tile.SHIFT
        val tileXRange = uniqueStart until min(tileSize, outEndX - outStartX) - Tile.SHIFT

        for (imgType in 0 until PredictedClasses.NUM_CLASSES) {
            for (ty in tileYRange) {
                val outY = outStartY + ty
                for (tx in tileXRange) {
                    val outX = outStartX + tx

                    val index = imgType * tileSize * tileSize + ty * tileSize + tx
                    finalMaskArray[imgType][outY][outX] = outputData[index]
                }
            }
        }
    }
}