package ru.unet_app.unet.models.classes

import androidx.core.graphics.createBitmap
import ru.unet_app.model.PredictedClasses
import ru.unet_app.model.Tile

abstract class BaseLabel {
    abstract val label: Array<FloatArray>
    abstract val type: PredictedClasses

    val height: Int
        get() = label.size

    val width: Int
        get() = label[0].size

    fun getMask(): Tile {
        val colors = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                // Если вероятность принадлежности к классу выше 0.5, делаем пиксель белым
                val isClass = label[y][x] > 0.5f
                val colorValue = if (isClass) 255 else 0

                // Создание Grayscale цвета (RGB=value)
                colors[y * width + x] = 0xFF shl 24 or (colorValue shl 16) or (colorValue shl 8) or colorValue
            }
        }

        val classBitmap = createBitmap(width, height)
        classBitmap.setPixels(colors, 0, width, 0, 0, width, height)
        return Tile(
            bitmap = classBitmap,
            startX = 0,
            startY = 0,
            endX = width,
            endY = height
        )
    }
}
