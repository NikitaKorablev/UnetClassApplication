package ru.unet_app.unet.models.classes

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import ru.unet_app.model.PredictedClasses

class BaseLabelTest {

    private class TestLabel(label: Array<FloatArray>) : BaseLabel() {
        override val label: Array<FloatArray> = label
        override val type = PredictedClasses.MITOCHONDRIA
    }

    @Test
    fun testHeightAndWidth() {
        val label = Array(100) { FloatArray(200) }
        val testLabel = TestLabel(label)

        assertEquals(100, testLabel.height)
        assertEquals(200, testLabel.width)
    }

    @Test
    fun testGetMaskThreshold() {
        val label = Array(10) { FloatArray(10) { 0.6f } }
        val testLabel = TestLabel(label)
        val mask = testLabel.getMask()

        assertNotNull(mask.bitmap)
        assertEquals(10, mask.bitmap.width)
        assertEquals(10, mask.bitmap.height)

        val pixels = IntArray(10 * 10)
        mask.bitmap.getPixels(pixels, 0, 10, 0, 0, 10, 10)

        for (pixel in pixels) {
            assertEquals(0xFFFFFFFF, pixel)
        }
    }

    @Test
    fun testGetMaskBelowThreshold() {
        val label = Array(10) { FloatArray(10) { 0.4f } }
        val testLabel = TestLabel(label)
        val mask = testLabel.getMask()

        val pixels = IntArray(10 * 10)
        mask.bitmap.getPixels(pixels, 0, 10, 0, 0, 10, 10)

        for (pixel in pixels) {
            assertEquals(0xFF000000, pixel)
        }
    }

    @Test
    fun testGetMaskMixedValues() {
        val label = Array(2) { y ->
            FloatArray(2) { x ->
                if (x == 0 && y == 0) 0.6f else 0.4f
            }
        }
        val testLabel = TestLabel(label)
        val mask = testLabel.getMask()

        val pixels = IntArray(2 * 2)
        mask.bitmap.getPixels(pixels, 0, 2, 0, 0, 2, 2)

        assertEquals(0xFFFFFFFF, pixels[0])
        assertEquals(0xFF000000, pixels[1])
        assertEquals(0xFF000000, pixels[2])
        assertEquals(0xFF000000, pixels[3])
    }
}