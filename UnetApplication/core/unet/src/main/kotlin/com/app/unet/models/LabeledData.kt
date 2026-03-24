package com.app.unet.models

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.core.graphics.createBitmap
import com.app.model.TransparencyState
import com.app.unet.models.classes.Axon
import com.app.unet.models.classes.BaseLabel
import com.app.unet.models.classes.Boundaries
import com.app.unet.models.classes.Mitochondria
import com.app.unet.models.classes.MitochondriaBoundaries
import com.app.unet.models.classes.PSD
import com.app.unet.models.classes.Vesicles

data class LabeledData(
    val mitochondria: Mitochondria,
    val PSD: PSD,
    val vesicles: Vesicles,
    val axon: Axon,
    val boundaries: Boundaries,
    val mitochondriaBoundaries: MitochondriaBoundaries
) {
    init {
        require(mitochondria.height == PSD.height &&
                mitochondria.width == PSD.width &&
                vesicles.height == PSD.height &&
                vesicles.width == PSD.width &&
                axon.height == PSD.height &&
                axon.width == PSD.width &&
                boundaries.height == PSD.height &&
                boundaries.width == PSD.width &&
                mitochondriaBoundaries.height == PSD.height &&
                mitochondriaBoundaries.width == PSD.width) {
            "All labels must have the same height and width."
        }
    }

    val labels: List<BaseLabel>
        get() = listOf(
            mitochondria,
            PSD,
            vesicles,
            axon,
            boundaries,
            mitochondriaBoundaries
        )

    val width: Int
        get() = PSD.width
    val height: Int
        get() = PSD.height


    fun unitedMask(alphas: TransparencyState = TransparencyState()): Bitmap {
        val resultBitmap = createBitmap(width, height)
        val canvas = Canvas(resultBitmap)
        // Очищаем холст (черный цвет, так как мы ищем "самый светлый" пиксель)
        canvas.drawColor(Color.BLACK)

        labels.forEach{ label ->
            val alphaFloat = alphas[label.type]
            val paint = Paint().apply {
                // Устанавливаем прозрачность слоя
                alpha = (alphaFloat * 255).toInt().coerceIn(0, 255)

                // Устанавливаем режим наложения "Lighten"
                // Он сравнивает текущий пиксель (с учетом alpha) и пиксель на Canvas
                // и оставляет тот, что ярче
                xfermode = PorterDuffXfermode(PorterDuff.Mode.LIGHTEN)

                // Опционально: для четких границ бинарного изображения
                isAntiAlias = false
            }

            val bitmap = label.getMask().bitmap
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
        }

        return resultBitmap
    }

    fun createUnitedMask(): Bitmap {
        val colors = IntArray(width * height)
        var maxClass: Int
        var maxProb: Float

        for (y in 0 until height) {
            for (x in 0 until width) {
                maxClass = 0
                maxProb = -1.0f

                // Найти класс с максимальной вероятностью (argmax)
                for (c in 0 until CLASSES_COUNT) {
                    val prob = labels[y].label[x][c]
                    if (prob > maxProb) {
                        maxProb = prob
                        maxClass = c
                    }
                }

                // Перевести вероятности в 0-255 и выбрать один канал для Grayscale вывода.
                // В вашем коде используется to_0_255_format_img, что подразумевает
                // конвертацию в uint8. Для визуализации мы можем просто взять
                // номер класса * 40 (для визуального различия)
                val colorValue = (maxClass * (255 / (CLASSES_COUNT - 1))).coerceIn(0, 255)

                // Создание Grayscale цвета (RGB=value)
                colors[y * width + x] = 0xFF shl 24 or (colorValue shl 16) or (colorValue shl 8) or colorValue
            }
        }

        val outputBitmap = createBitmap(width, height)
        outputBitmap.setPixels(colors, 0, width, 0, 0, width, height)
        return outputBitmap
    }

    companion object {
        private const val CLASSES_COUNT = 6
    }
}