package ru.unet_app.unet.models

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.Log
import androidx.core.graphics.createBitmap
import com.app.model.TransparencyState
import ru.unet_app.unet.models.classes.Axon
import ru.unet_app.unet.models.classes.BaseLabel
import ru.unet_app.unet.models.classes.Boundaries
import ru.unet_app.unet.models.classes.Mitochondria
import ru.unet_app.unet.models.classes.MitochondriaBoundaries
import ru.unet_app.unet.models.classes.PSD
import ru.unet_app.unet.models.classes.Vesicles

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
        val startTime = System.currentTimeMillis()
        val resultBitmap = createBitmap(width, height)
        val canvas = Canvas(resultBitmap)
        canvas.drawColor(Color.BLACK)

        val paint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.LIGHTEN)
            isAntiAlias = false
        }
        labels.forEach{ label ->
            val alphaFloat = alphas[label.type]
            paint.alpha = (alphaFloat * 255).toInt().coerceIn(0, 255)

            val bitmap = label.getMask().bitmap
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
        }

        val duration = System.currentTimeMillis() - startTime
        Log.d("Analytics", "unitedMask execution time: $duration ms")
        return resultBitmap
    }

    companion object {
        private const val CLASSES_COUNT = 6
    }
}