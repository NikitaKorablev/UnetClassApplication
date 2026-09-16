package ru.unet_app.unet.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test
import ru.unet_app.model.PredictedClasses
import ru.unet_app.unet.models.classes.Axon
import ru.unet_app.unet.models.classes.BaseLabel
import ru.unet_app.unet.models.classes.Boundaries
import ru.unet_app.unet.models.classes.Mitochondria
import ru.unet_app.unet.models.classes.MitochondriaBoundaries
import ru.unet_app.unet.models.classes.PSD
import ru.unet_app.unet.models.classes.Vesicles

class LabelFactoryTest {

    private val emptyLabel = Array(10) { FloatArray(10) }

    @Test
    fun testGetMitochondria() {
        val label = LabelFactory.getLabel(PredictedClasses.MITOCHONDRIA.className, emptyLabel)

        assertNotNull(label)
        assertEquals(PredictedClasses.MITOCHONDRIA, label.type)
        assertTrue(label is Mitochondria)
    }

    @Test
    fun testGetPSD() {
        val label = LabelFactory.getLabel(PredictedClasses.PSD.className, emptyLabel)

        assertNotNull(label)
        assertEquals(PredictedClasses.PSD, label.type)
        assertTrue(label is PSD)
    }

    @Test
    fun testGetVesicles() {
        val label = LabelFactory.getLabel(PredictedClasses.VESICLES.className, emptyLabel)

        assertNotNull(label)
        assertEquals(PredictedClasses.VESICLES, label.type)
        assertTrue(label is Vesicles)
    }

    @Test
    fun testGetAxon() {
        val label = LabelFactory.getLabel(PredictedClasses.AXON.className, emptyLabel)

        assertNotNull(label)
        assertEquals(PredictedClasses.AXON, label.type)
        assertTrue(label is Axon)
    }

    @Test
    fun testGetBoundaries() {
        val label = LabelFactory.getLabel(PredictedClasses.BOUNDARIES.className, emptyLabel)

        assertNotNull(label)
        assertEquals(PredictedClasses.BOUNDARIES, label.type)
        assertTrue(label is Boundaries)
    }

    @Test
    fun testGetMitochondriaBoundaries() {
        val label = LabelFactory.getLabel(PredictedClasses.MITOCHONDRIAL_BOUNDARIES.className, emptyLabel)

        assertNotNull(label)
        assertEquals(PredictedClasses.MITOCHONDRIAL_BOUNDARIES, label.type)
        assertTrue(label is MitochondriaBoundaries)
    }

    @Test
    fun testUnknownClassThrows() {
        val exception = assertThrows<IllegalArgumentException> {
            LabelFactory.getLabel("unknown_class", emptyLabel)
        }
        assertEquals("Unknown class getClassName: unknown_class", exception.message)
    }

    @Test
    fun testAllClassesMapped() {
        val classes = PredictedClasses.entries.map { it.className }
        for (className in classes) {
            val label = LabelFactory.getLabel(className, emptyLabel)
            assertNotNull(label)
        }
    }
}