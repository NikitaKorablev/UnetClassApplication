package ru.unet_app.unet.models.classes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import ru.unet_app.model.PredictedClasses

class MitochondriaTest {

    @Test
    fun testDefaultConstructor() {
        val mitochondria = Mitochondria()

        assertNotNull(mitochondria)
        assertEquals(PredictedClasses.MITOCHONDRIA, mitochondria.type)
        assertEquals(0, mitochondria.height)
        assertEquals(0, mitochondria.width)
    }

    @Test
    fun testWithLabel() {
        val label = Array(50) { FloatArray(60) }
        val mitochondria = Mitochondria(label)

        assertEquals(50, mitochondria.height)
        assertEquals(60, mitochondria.width)
        assertEquals(PredictedClasses.MITOCHONDRIA, mitochondria.type)
    }
}