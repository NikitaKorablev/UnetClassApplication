package ru.unet_app.unet.models.classes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import ru.unet_app.model.PredictedClasses

class PSDTest {

    @Test
    fun testDefaultConstructor() {
        val psd = PSD()

        assertNotNull(psd)
        assertEquals(PredictedClasses.PSD, psd.type)
        assertEquals(0, psd.height)
        assertEquals(0, psd.width)
    }

    @Test
    fun testWithLabel() {
        val label = Array(50) { FloatArray(60) }
        val psd = PSD(label)

        assertEquals(50, psd.height)
        assertEquals(60, psd.width)
        assertEquals(PredictedClasses.PSD, psd.type)
    }
}