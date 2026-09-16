package ru.unet_app.model

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TileTest {

    @Test
    fun testTileCreation() {
        val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val tile = Tile(
            bitmap = bitmap,
            startX = 0,
            startY = 0,
            endX = 256,
            endY = 256
        )

        assertNotNull(tile)
        assertEquals(0, tile.startX)
        assertEquals(0, tile.startY)
        assertEquals(256, tile.endX)
        assertEquals(256, tile.endY)
        assertEquals(bitmap, tile.bitmap)
    }

    @Test
    fun testTileConstants() {
        assertEquals(256, Tile.SIZE)
        assertEquals(32, Tile.SHIFT)
    }

    @Test
    fun testTileWithCustomCoordinates() {
        val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val tile = Tile(
            bitmap = bitmap,
            startX = 100,
            startY = 200,
            endX = 356,
            endY = 456
        )

        assertEquals(100, tile.startX)
        assertEquals(200, tile.startY)
        assertEquals(356, tile.endX)
        assertEquals(456, tile.endY)
    }
}