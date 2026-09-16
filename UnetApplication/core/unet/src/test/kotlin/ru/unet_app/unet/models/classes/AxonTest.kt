package ru.unet_app.unet.models.classes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import ru.unet_app.model.PredictedClasses

class AxonTest {

    @Test
    fun testDefaultConstructor() {
        val axon = Axon()

        assertNotNull(axon)
        assertEquals(PredictedClasses.AXON, axon.type)
        assertEquals(0, axon.height)
        assertEquals(0, axon.width)
    }

    @Test
    fun testWithLabel() {
        val label = Array(50) { FloatArray(60) }
        val axon = Axon(label)

        assertEquals(50, axon.height)
        assertEquals(60, axon.width)
        assertEquals(PredictedClasses.AXON, axon.type)
    }
}