package ru.unet_app.transparency_settings.data.repository

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.unet_app.model.PredictedClasses
import ru.unet_app.model.TransparencyState
import ru.unet_app.transparency_settings.domain.repository.TransparencyImageProcRepository
import java.util.HashMap

@RunWith(RobolectricTestRunner::class)
class TransparencyImageProcImplTest {

    private val impl = TransparencyImageProcImpl()
    private val context = ApplicationProvider.getApplicationContext()

    private fun createTestBitmap(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(color)
        return bitmap
    }

    private fun createClassMasks(): HashMap<PredictedClasses, Bitmap> {
        val masks = HashMap<PredictedClasses, Bitmap>()
        masks[PredictedClasses.MITOCHONDRIA] = createTestBitmap(50, 50, Color.WHITE)
        masks[PredictedClasses.PSD] = createTestBitmap(50, 50, Color.WHITE)
        masks[PredictedClasses.VESICLES] = createTestBitmap(50, 50, Color.WHITE)
        masks[PredictedClasses.AXON] = createTestBitmap(50, 50, Color.WHITE)
        masks[PredictedClasses.BOUNDARIES] = createTestBitmap(50, 50, Color.WHITE)
        masks[PredictedClasses.MITOCHONDRIAL_BOUNDARIES] = createTestBitmap(50, 50, Color.WHITE)
        return masks
    }

    @Test
    fun testUnitedMaskDefaultTransparency() {
        val classMasks = createClassMasks()
        val state = TransparencyState()

        val result = impl.unitedMask(classMasks, state)

        assertNotNull(result)
        assertEquals(50, result.width)
        assertEquals(50, result.height)
    }

    @Test
    fun testUnitedMaskCustomTransparency() {
        val classMasks = createClassMasks()
        val state = TransparencyState(
            mitochondria = 1.0f,
            psd = 0.0f,
            vesicles = 0.5f,
            axon = 0.5f,
            boundaries = 0.5f,
            mitochondrialBoundaries = 0.5f
        )

        val result = impl.unitedMask(classMasks, state)

        assertNotNull(result)
        assertEquals(50, result.width)
        assertEquals(50, result.height)
    }

    @Test
    fun testUnitedMaskZeroTransparency() {
        val classMasks = createClassMasks()
        val state = TransparencyState(
            mitochondria = 0.0f,
            psd = 0.0f,
            vesicles = 0.0f,
            axon = 0.0f,
            boundaries = 0.0f,
            mitochondrialBoundaries = 0.0f
        )

        val result = impl.unitedMask(classMasks, state)

        assertNotNull(result)
        val pixels = IntArray(50 * 50)
        result.getPixels(pixels, 0, 50, 0, 0, 50, 50)
        for (pixel in pixels) {
            assertEquals(Color.BLACK, pixel)
        }
    }

    @Test
    fun testUnitedMaskFullTransparency() {
        val classMasks = createClassMasks()
        val state = TransparencyState(
            mitochondria = 1.0f,
            psd = 1.0f,
            vesicles = 1.0f,
            axon = 1.0f,
            boundaries = 1.0f,
            mitochondrialBoundaries = 1.0f
        )

        val result = impl.unitedMask(classMasks, state)

        assertNotNull(result)
        val pixels = IntArray(50 * 50)
        result.getPixels(pixels, 0, 50, 0, 0, 50, 50)
        for (pixel in pixels) {
            assertEquals(Color.WHITE, pixel)
        }
    }

    @Test
    fun testOverlayMasksWithTransparency() {
        val classMasks = createClassMasks()
        val state = TransparencyState()

        val result = impl.overlayMasksWithTransparency(classMasks, state)

        assertNotNull(result)
        assertEquals(50, result.width)
        assertEquals(50, result.height)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testUnitedMaskEmptyMapThrows() {
        val emptyMasks = HashMap<PredictedClasses, Bitmap>()
        val state = TransparencyState()

        impl.unitedMask(emptyMasks, state)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testOverlayMasksEmptyMapThrows() {
        val emptyMasks = HashMap<PredictedClasses, Bitmap>()
        val state = TransparencyState()

        impl.overlayMasksWithTransparency(emptyMasks, state)
    }

    @Test
    fun testDifferentSizeMasks() {
        val classMasks = HashMap<PredictedClasses, Bitmap>()
        classMasks[PredictedClasses.MITOCHONDRIA] = createTestBitmap(100, 100, Color.WHITE)
        classMasks[PredictedClasses.PSD] = createTestBitmap(100, 100, Color.WHITE)
        classMasks[PredictedClasses.VESICLES] = createTestBitmap(100, 100, Color.WHITE)
        classMasks[PredictedClasses.AXON] = createTestBitmap(100, 100, Color.WHITE)
        classMasks[PredictedClasses.BOUNDARIES] = createTestBitmap(100, 100, Color.WHITE)
        classMasks[PredictedClasses.MITOCHONDRIAL_BOUNDARIES] = createTestBitmap(100, 100, Color.WHITE)
        val state = TransparencyState()

        val result = impl.unitedMask(classMasks, state)

        assertNotNull(result)
        assertEquals(100, result.width)
        assertEquals(100, result.height)
    }
}