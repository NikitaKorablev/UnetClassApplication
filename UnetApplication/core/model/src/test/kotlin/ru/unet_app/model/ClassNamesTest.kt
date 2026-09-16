package ru.unet_app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ClassNamesTest {

    @Test
    fun testNamesList() {
        val names = ClassNames.NAMES

        assertEquals(6, names.size)
        assertEquals("mitochondria", names[0])
        assertEquals("PSD", names[1])
        assertEquals("vesicles", names[2])
        assertEquals("axon", names[3])
        assertEquals("boundaries", names[4])
        assertEquals("mitochondrial_boundaries", names[5])
    }

    @Test
    fun testNamesOrderMatchesEnum() {
        val names = ClassNames.NAMES
        val enumEntries = PredictedClasses.entries

        assertEquals(enumEntries.size, names.size)
        for (i in enumEntries.indices) {
            assertEquals(enumEntries[i].className, names[i])
        }
    }
}