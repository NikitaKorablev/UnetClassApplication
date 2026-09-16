package ru.unet_app.model

import org.junit.Assert.assertEquals
import org.junit.Test

class InferenceMetadataTest {

    @Test
    fun testCreation() {
        val metadata = InferenceMetadata(
            width = 512,
            height = 512,
            executionTimeMs = 1234L,
            memoryUsageBytes = 1024L * 1024L
        )

        assertEquals(512, metadata.width)
        assertEquals(512, metadata.height)
        assertEquals(1234L, metadata.executionTimeMs)
        assertEquals(1024L * 1024L, metadata.memoryUsageBytes)
    }

    @Test
    fun testEquality() {
        val m1 = InferenceMetadata(256, 256, 100L, 200L)
        val m2 = InferenceMetadata(256, 256, 100L, 200L)
        val m3 = InferenceMetadata(512, 512, 100L, 200L)

        assertEquals(m1, m2)
        assertEquals(m1.hashCode(), m2.hashCode())
        assertNotEquals(m1, m3)
    }
}