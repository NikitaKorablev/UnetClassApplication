package ru.unet_app.model

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ImageDataTest {

    @Test
    fun testCreation() {
        val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val tile = Tile(bitmap, 0, 0, 256, 256)
        val tiles = listOf(tile)

        val imageData = ImageData(
            width = 512,
            height = 512,
            tiles = tiles
        )

        assertEquals(512, imageData.width)
        assertEquals(512, imageData.height)
        assertEquals(1, imageData.tiles.size)
        assertEquals(tile, imageData.tiles[0])
    }

    @Test
    fun testMultipleTiles() {
        val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val tiles = List(4) { i ->
            Tile(bitmap, i * 256, 0, (i + 1) * 256, 256)
        }

        val imageData = ImageData(
            width = 1024,
            height = 256,
            tiles = tiles
        )

        assertEquals(1024, imageData.width)
        assertEquals(256, imageData.height)
        assertEquals(4, imageData.tiles.size)
    }
}