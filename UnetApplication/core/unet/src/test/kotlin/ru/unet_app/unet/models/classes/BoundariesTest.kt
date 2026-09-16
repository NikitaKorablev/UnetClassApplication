package ru.unet_app.unet.models.classes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import ru.unet_app.model.PredictedClasses

class BoundariesTest {

    @Test
    fun testDefaultConstructor() {
        val boundaries = Boundaries()

        assertNotNull(boundaries)
        assertEquals(PredictedClasses.BOUNDARIES, boundaries.type)
        assertEquals(0, boundaries.height)
        assertEquals(0, boundaries.width)
    }

    @Test
    fun testWithLabel() {
        val label = Array(50) { FloatArray(60) }
        val boundaries = Boundaries(label)

        assertEquals(50, boundaries.height)
        assertEquals(60, boundaries.width)
        assertEquals(PredictedClasses.BOUNDARIES, boundaries.type)
    }
}