package com.app.transparancy.data.repository

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import com.app.model.TransparencyType
import com.app.transparancy.domain.repository.TransparencyImageProcRepository
import com.app.transparancy.presentation.model.TransparencyState

class TransparencyImageProcImpl : TransparencyImageProcRepository {
    override fun overlayMasksWithTransparency(
        classMasks: Map<TransparencyType, Bitmap>,
        state: TransparencyState
    ): Bitmap {
        require(classMasks.isNotEmpty()) { "Список масок не может быть пустым" }

        val firstMask = classMasks.values.first()
        val width = firstMask.width
        val height = firstMask.height
        val numClasses = classMasks.size

        // Создаем массив для хранения "взвешенных" значений для каждого пикселя
        // где значение равно вероятность * прозрачность (вес)
        val weightedValues = Array(height) { Array(width) { IntArray(numClasses) } }

        // Вычисляем взвешенные значения для каждого класса

        classMasks.entries.forEachIndexed { classIndex, (type, bitmap) ->
            val transparency = state[type].coerceIn(0.0f, 1.0f)

            // Извлекаем значения из изображения маски класса
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pixelIndex = y * width + x
                    val pixel = pixels[pixelIndex]

                    // Извлекаем значение яркости (используем канал R, так как изображение в градациях серого)
                    val brightness = (pixel shr 16) and 0xFF

                    // Вычисляем взвешенное значение (вероятность * прозрачность)
                    weightedValues[y][x][classIndex] = (brightness * transparency).toInt()
                }
            }
        }

        // Создаем результирующее изображение, выбирая класс с максимальным взвешенным значением
        val colors = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val colorValue = weightedValues[y][x].maxOrNull() ?: 0
                colors[y * width + x] = 0xFF shl 24 or (colorValue shl 16) or (colorValue shl 8) or colorValue
            }
        }

        val outputBitmap = createBitmap(width, height)
        outputBitmap.setPixels(colors, 0, width, 0, 0, width, height)
        return outputBitmap
    }
}