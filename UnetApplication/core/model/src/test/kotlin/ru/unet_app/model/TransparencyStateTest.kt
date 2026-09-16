package ru.unet_app.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TransparencyStateTest {

    @Test
    fun testDefaultValues() {
        val state = TransparencyState()

        assertEquals(0.3f, state.mitochondria, 0.001f)
        assertEquals(0.7f, state.psd, 0.001f)
        assertEquals(0.6f, state.vesicles, 0.001f)
        assertEquals(0.8f, state.axon, 0.001f)
        assertEquals(0.75f, state.boundaries, 0.001f)
        assertEquals(0.45f, state.mitochondrialBoundaries, 0.001f)
    }

    @Test
    fun testGetByIndex() {
        val state = TransparencyState()

        assertEquals(0.3f, state[0], 0.001f)
        assertEquals(0.7f, state[1], 0.001f)
        assertEquals(0.6f, state[2], 0.001f)
        assertEquals(0.8f, state[3], 0.001f)
        assertEquals(0.75f, state[4], 0.001f)
        assertEquals(0.45f, state[5], 0.001f)
    }

    @Test
    fun testGetByPredictedClasses() {
        val state = TransparencyState()

        assertEquals(0.3f, state[PredictedClasses.MITOCHONDRIA], 0.001f)
        assertEquals(0.7f, state[PredictedClasses.PSD], 0.001f)
        assertEquals(0.6f, state[PredictedClasses.VESICLES], 0.001f)
        assertEquals(0.8f, state[PredictedClasses.AXON], 0.001f)
        assertEquals(0.75f, state[PredictedClasses.BOUNDARIES], 0.001f)
        assertEquals(0.45f, state[PredictedClasses.MITOCHONDRIAL_BOUNDARIES], 0.001f)
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun testGetByIndexOutOfBounds() {
        val state = TransparencyState()
        state[6]
    }

    @Test
    fun testCopy() {
        val original = TransparencyState()
        val modified = original.copy(mitochondria = 0.5f)

        assertEquals(0.5f, modified.mitochondria, 0.001f)
        assertEquals(0.7f, modified.psd, 0.001f)
        assertEquals(original.psd, modified.psd)
    }
}