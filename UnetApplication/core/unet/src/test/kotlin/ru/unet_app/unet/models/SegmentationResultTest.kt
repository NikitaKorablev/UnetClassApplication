package ru.unet_app.unet.models

import org.junit.Assert.assertEquals
import org.junit.Test

class SegmentationResultTest {

    @Test
    fun testCreation() {
        val result = SegmentationResult(
            labeledData = createMockLabeledData(),
            totalTimeMs = 1234L,
            memoryUsageBytes = 1024L * 1024L
        )

        assertEquals(1234L, result.totalTimeMs)
        assertEquals(1024L * 1024L, result.memoryUsageBytes)
        assertNotNull(result.labeledData)
    }

    @Test
    fun testDefaultMemoryUsage() {
        val result = SegmentationResult(
            labeledData = createMockLabeledData(),
            totalTimeMs = 100L
        )

        assertEquals(0L, result.memoryUsageBytes)
    }

    @Test
    fun testEquality() {
        val r1 = SegmentationResult(
            labeledData = createMockLabeledData(),
            totalTimeMs = 100L,
            memoryUsageBytes = 200L
        )
        val r2 = SegmentationResult(
            labeledData = createMockLabeledData(),
            totalTimeMs = 100L,
            memoryUsageBytes = 200L
        )
        val r3 = SegmentationResult(
            labeledData = createMockLabeledData(),
            totalTimeMs = 200L,
            memoryUsageBytes = 200L
        )

        assertEquals(r1, r2)
        assertNotEquals(r1, r3)
    }

    private fun createMockLabeledData(): LabeledData {
        return LabeledData(
            mitochondria = ru.unet_app.unet.models.classes.Mitochondria(Array(10) { FloatArray(10) }),
            PSD = ru.unet_app.unet.models.classes.PSD(Array(10) { FloatArray(10) }),
            vesicles = ru.unet_app.unet.models.classes.Vesicles(Array(10) { FloatArray(10) }),
            axon = ru.unet_app.unet.models.classes.Axon(Array(10) { FloatArray(10) }),
            boundaries = ru.unet_app.unet.models.classes.Boundaries(Array(10) { FloatArray(10) }),
            mitochondriaBoundaries = ru.unet_app.unet.models.classes.MitochondriaBoundaries(Array(10) { FloatArray(10) })
        )
    }
}