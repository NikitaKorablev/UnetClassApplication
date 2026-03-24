package com.app.transparency_settings.data.repository

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import com.app.model.PredictedClasses
import com.app.transparency_settings.domain.repository.TransparencyImageProcRepository
import com.app.model.TransparencyState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TransparencyImageProcImpl : TransparencyImageProcRepository {
    override fun overlayMasksWithTransparency(
        classMasks: Map<PredictedClasses, Bitmap>,
        state: TransparencyState
    ): Bitmap = runBlocking(Dispatchers.Default) {
        require(classMasks.isNotEmpty()) {
            "Не найдены маски для расчета"
        }

        val firstMask = classMasks.values.first()
        val width = firstMask.width
        val height = firstMask.height
        val numClasses = classMasks.size

        // Плоский массив вместо трехмерного (в разы быстрее работа с памятью)
        val weightedValues = IntArray(width * height * numClasses)

        // 1. Параллельно обрабатываем каждую маску
        val jobs = classMasks.entries.mapIndexed { classIndex, (type, bitmap) ->
            launch {
                val transparency = state[type].coerceIn(0.0f, 1.0f)
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

                for (i in 0 until (width * height)) {
                    val brightness = (pixels[i] shr 16) and 0xFF
                    weightedValues[i * numClasses + classIndex] = (brightness * transparency).toInt()
                }
            }
        }
        jobs.joinAll()

        // 2. Параллельно вычисляем финальные цвета (разбиваем картинку на полосы по высоте)
        val finalColors = IntArray(width * height)

        val cpuCount = Runtime.getRuntime().availableProcessors()
        val chunkSize = (height + cpuCount - 1) / cpuCount
//        val rowsPerTask = 100 // Группируем строки для уменьшения накладных расходов

        (0 until height step chunkSize).map { startY ->
            launch {
                val endY = minOf(startY + chunkSize, height)
                for (y in startY until endY) {
                    val rowOffset = y * width
                    for (x in 0 until width) {
                        val pixelIdx = rowOffset + x
                        val startOffset = pixelIdx * numClasses

                        // Ищем максимум среди классов для этого пикселя
                        var maxVal = 0
                        for (c in 0 until numClasses) {
                            val v = weightedValues[startOffset + c]
                            if (v > maxVal) maxVal = v
                        }

                        finalColors[pixelIdx] = 0xFF shl 24 or (maxVal shl 16) or (maxVal shl 8) or maxVal
                    }
                }
            }
        }.joinAll()

        val outputBitmap = createBitmap(width, height)
        outputBitmap.setPixels(finalColors, 0, width, 0, 0, width, height)
        outputBitmap
    }
}