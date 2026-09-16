package ru.unet_app.unet.models.classes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import ru.unet_app.model.PredictedClasses

class VesiclesTest {

    @Test
    fun testDefaultConstructor() {
        val vesicles = Vesicles()

        assertNotNull(vesicles)
        assertEquals(PredictedClasses.VESICLES, vesicles.type)
        assertEquals(0, vesicles.height)
        assertEquals(0, vesicles.width)
    }

    @Test
    fun testWithLabel() {
        val label = Array(50) { FloatArray(60) }
        val vesicles = Vesicles(label)

        assertEquals(50, vesicles.height)
        assertEquals(60, vesicles.width)
        assertEquals(PredictedClasses.VESICLES, vesicles.type)
    }
}