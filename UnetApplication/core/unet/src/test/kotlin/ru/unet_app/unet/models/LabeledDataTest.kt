package ru.unet_app.unet.models

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test
import ru.unet_app.model.PredictedClasses
import ru.unet_app.model.TransparencyState
import ru.unet_app.unet.models.classes.Axon
import ru.unet_app.unet.models.classes.Boundaries
import ru.unet_app.unet.models.classes.Mitochondria
import ru.unet_app.unet.models.classes.MitochondriaBoundaries
import ru.unet_app.unet.models.classes.PSD
import ru.unet_app.unet.models.classes.Vesicles

class LabeledDataTest {

    private fun createLabels(height: Int, width: Int) = listOf(
        Mitochondria(Array(height) { FloatArray(width) }),
        PSD(Array(height) { FloatArray(width) }),
        Vesicles(Array(height) { FloatArray(width) }),
        Axon(Array(height) { FloatArray(width) }),
        Boundaries(Array(height) { FloatArray(width) }),
        MitochondriaBoundaries(Array(height) { FloatArray(width) })
    )

    @Test
    fun testValidCreation() {
        val labels = createLabels(256, 256)
        val labeledData = LabeledData(
            mitochondria = labels[0] as Mitochondria,
            PSD = labels[1] as PSD,
            vesicles = labels[2] as Vesicles,
            axon = labels[3] as Axon,
            boundaries = labels[4] as Boundaries,
            mitochondriaBoundaries = labels[5] as MitochondriaBoundaries
        )

        assertEquals(256, labeledData.width)
        assertEquals(256, labeledData.height)
        assertEquals(6, labeledData.labels.size)
    }

    @Test
    fun testLabelsOrder() {
        val labels = createLabels(100, 100)
        val labeledData = LabeledData(
            mitochondria = labels[0] as Mitochondria,
            PSD = labels[1] as PSD,
            vesicles = labels[2] as Vesicles,
            axon = labels[3] as Axon,
            boundaries = labels[4] as Boundaries,
            mitochondriaBoundaries = labels[5] as MitochondriaBoundaries
        )

        assertEquals(PredictedClasses.MITOCHONDRIA, labeledData.labels[0].type)
        assertEquals(PredictedClasses.PSD, labeledData.labels[1].type)
        assertEquals(PredictedClasses.VESICLES, labeledData.labels[2].type)
        assertEquals(PredictedClasses.AXON, labeledData.labels[3].type)
        assertEquals(PredictedClasses.BOUNDARIES, labeledData.labels[4].type)
        assertEquals(PredictedClasses.MITOCHONDRIAL_BOUNDARIES, labeledData.labels[5].type)
    }

    @Test
    fun testMismatchedDimensionsThrows() {
        val labels = listOf(
            Mitochondria(Array(100) { FloatArray(100) }),
            PSD(Array(200) { FloatArray(200) }),
            Vesicles(Array(100) { FloatArray(100) }),
            Axon(Array(100) { FloatArray(100) }),
            Boundaries(Array(100) { FloatArray(100) }),
            MitochondriaBoundaries(Array(100) { FloatArray(100) })
        )

        val exception = assertThrows<IllegalArgumentException> {
            LabeledData(
                mitochondria = labels[0] as Mitochondria,
                PSD = labels[1] as PSD,
                vesicles = labels[2] as Vesicles,
                axon = labels[3] as Axon,
                boundaries = labels[4] as Boundaries,
                mitochondriaBoundaries = labels[5] as MitochondriaBoundaries
            )
        }
        assertEquals("All labels must have the same height and width.", exception.message)
    }

    @Test
    fun testUnitedMask() {
        val labels = createLabels(50, 50)
        val labeledData = LabeledData(
            mitochondria = labels[0] as Mitochondria,
            PSD = labels[1] as PSD,
            vesicles = labels[2] as Vesicles,
            axon = labels[3] as Axon,
            boundaries = labels[4] as Boundaries,
            mitochondriaBoundaries = labels[5] as MitochondriaBoundaries
        )

        val result = labeledData.unitedMask(TransparencyState())

        assertNotNull(result)
        assertEquals(50, result.width)
        assertEquals(50, result.height)
    }

    @Test
    fun testUnitedMaskWithCustomTransparency() {
        val labels = createLabels(50, 50)
        val labeledData = LabeledData(
            mitochondria = labels[0] as Mitochondria,
            PSD = labels[1] as PSD,
            vesicles = labels[2] as Vesicles,
            axon = labels[3] as Axon,
            boundaries = labels[4] as Boundaries,
            mitochondriaBoundaries = labels[5] as MitochondriaBoundaries
        )

        val transparency = TransparencyState(
            mitochondria = 1.0f,
            psd = 0.0f,
            vesicles = 0.5f,
            axon = 0.5f,
            boundaries = 0.5f,
            mitochondrialBoundaries = 0.5f
        )

        val result = labeledData.unitedMask(transparency)

        assertNotNull(result)
        assertEquals(50, result.width)
        assertEquals(50, result.height)
    }
}