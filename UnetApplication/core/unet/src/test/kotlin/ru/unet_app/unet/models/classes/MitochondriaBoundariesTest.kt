package ru.unet_app.unet.models.classes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import ru.unet_app.model.PredictedClasses

class MitochondriaBoundariesTest {

    @Test
    fun testDefaultConstructor() {
        val mitochondriaBoundaries = MitochondriaBoundaries()

        assertNotNull(mitochondriaBoundaries)
        assertEquals(PredictedClasses.MITOCHONDRIAL_BOUNDARIES, mitochondriaBoundaries.type)
        assertEquals(0, mitochondriaBoundaries.height)
        assertEquals(0, mitochondriaBoundaries.width)
    }

    @Test
    fun testWithLabel() {
        val label = Array(50) { FloatArray(60) }
        val mitochondriaBoundaries = MitochondriaBoundaries(label)

        assertEquals(50, mitochondriaBoundaries.height)
        assertEquals(60, mitochondriaBoundaries.width)
        assertEquals(PredictedClasses.MITOCHONDRIAL_BOUNDARIES, mitochondriaBoundaries.type)
    }
}