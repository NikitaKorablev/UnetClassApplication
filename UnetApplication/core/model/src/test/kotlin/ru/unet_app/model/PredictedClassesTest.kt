package ru.unet_app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PredictedClassesTest {

    @Test
    fun testEnumValues() {
        val values = PredictedClasses.entries

        assertEquals(6, values.size)
        assertEquals("mitochondria", PredictedClasses.MITOCHONDRIA.className)
        assertEquals("PSD", PredictedClasses.PSD.className)
        assertEquals("vesicles", PredictedClasses.VESICLES.className)
        assertEquals("axon", PredictedClasses.AXON.className)
        assertEquals("boundaries", PredictedClasses.BOUNDARIES.className)
        assertEquals("mitochondrial_boundaries", PredictedClasses.MITOCHONDRIAL_BOUNDARIES.className)
    }

    @Test
    fun testLabel() {
        assertEquals("mitochondria", PredictedClasses.MITOCHONDRIA.label())
        assertEquals("PSD", PredictedClasses.PSD.label())
        assertEquals("vesicles", PredictedClasses.VESICLES.label())
        assertEquals("axon", PredictedClasses.AXON.label())
        assertEquals("boundaries", PredictedClasses.BOUNDARIES.label())
        assertEquals("mitochondrial_boundaries", PredictedClasses.MITOCHONDRIAL_BOUNDARIES.label())
    }

    @Test
    fun testGetByIndex() {
        assertEquals(PredictedClasses.MITOCHONDRIA, PredictedClasses.get(0))
        assertEquals(PredictedClasses.PSD, PredictedClasses.get(1))
        assertEquals(PredictedClasses.VESICLES, PredictedClasses.get(2))
        assertEquals(PredictedClasses.AXON, PredictedClasses.get(3))
        assertEquals(PredictedClasses.BOUNDARIES, PredictedClasses.get(4))
        assertEquals(PredictedClasses.MITOCHONDRIAL_BOUNDARIES, PredictedClasses.get(5))
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun testGetByIndexOutOfBounds() {
        PredictedClasses.get(6)
    }

    @Test
    fun testNumClasses() {
        assertEquals(6, PredictedClasses.NUM_CLASSES)
    }
}