package ru.unet_app.inference_details.presentation.detail

import org.junit.Assert.assertEquals
import org.junit.Test

class DetailImageItemTest {

    @Test
    fun testCreation() {
        val item = DetailImageItem(
            imagePath = "/path/to/image.png",
            imageName = "test_image",
            className = "mitochondria"
        )

        assertEquals("/path/to/image.png", item.imagePath)
        assertEquals("test_image", item.imageName)
        assertEquals("mitochondria", item.className)
    }

    @Test
    fun testCreationWithoutClassName() {
        val item = DetailImageItem(
            imagePath = "/path/to/image.png",
            imageName = "test_image"
        )

        assertEquals("/path/to/image.png", item.imagePath)
        assertEquals("test_image", item.imageName)
        assertEquals(null, item.className)
    }

    @Test
    fun testEquality() {
        val item1 = DetailImageItem("/path.png", "name", "class")
        val item2 = DetailImageItem("/path.png", "name", "class")
        val item3 = DetailImageItem("/other.png", "name", "class")

        assertEquals(item1, item2)
        assertNotEquals(item1, item3)
    }
}