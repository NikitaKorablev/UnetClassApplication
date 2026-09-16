package ru.unet_app.unet.domain.usecases

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import ru.unet_app.model.Tile

class SplitImageIntoTilesUseCaseTest {

    private val useCase = SplitImageIntoTilesUseCase()

    @Test
    fun testExactTileSize() {
        val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val tiles = useCase(bitmap)

        assertEquals(1, tiles.size)
        assertEquals(0, tiles[0].startX)
        assertEquals(0, tiles[0].startY)
        assertEquals(256, tiles[0].endX)
        assertEquals(256, tiles[0].endY)
    }

    @Test
    fun testHorizontalSplit() {
        val bitmap = Bitmap.createBitmap(512, 256, Bitmap.Config.ARGB_8888)
        val tiles = useCase(bitmap)

        assertEquals(2, tiles.size)
        assertEquals(0, tiles[0].startX)
        assertEquals(256, tiles[0].endX)
        assertEquals(256, tiles[1].startX)
        assertEquals(512, tiles[1].endX)
    }

    @Test
    fun testVerticalSplit() {
        val bitmap = Bitmap.createBitmap(256, 512, Bitmap.Config.ARGB_8888)
        val tiles = useCase(bitmap)

        assertEquals(2, tiles.size)
        assertEquals(0, tiles[0].startY)
        assertEquals(256, tiles[0].endY)
        assertEquals(256, tiles[1].startY)
        assertEquals(512, tiles[1].endY)
    }

    @Test
    fun testGridSplit() {
        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        val tiles = useCase(bitmap)

        assertEquals(4, tiles.size)
    }

    @Test
    fun testOverlapHandling() {
        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        val tiles = useCase(bitmap)

        for (i in 0 until tiles.size - 1) {
            for (j in i + 1 until tiles.size) {
                val t1 = tiles[i]
                val t2 = tiles[j]
                val horizontalOverlap = t1.startX < t2.endX && t2.startX < t1.endX
                val verticalOverlap = t1.startY < t2.endY && t2.startY < t1.endY
                if (horizontalOverlap && verticalOverlap) {
                    val overlapX = min(t1.endX, t2.endX) - max(t1.startX, t2.startX)
                    val overlapY = min(t1.endY, t2.endY) - max(t1.startY, t2.startY)
                    assertEquals(SplitImageIntoTilesUseCase.TILE_SIZE - SplitImageIntoTilesUseCase.STEP, overlapX)
                    assertEquals(SplitImageIntoTilesUseCase.TILE_SIZE - SplitImageIntoTilesUseCase.STEP, overlapY)
                }
            }
        }
    }

    @Test
    fun testNonMultipleOfTileSize() {
        val bitmap = Bitmap.createBitmap(1000, 800, Bitmap.Config.ARGB_8888)
        val tiles = useCase(bitmap)

        assertNotNull(tiles)
        assertEquals(1000, tiles.maxByOrNull { it.endX }?.endX)
        assertEquals(800, tiles.maxByOrNull { it.endY }?.endY)
    }

    @Test
    fun testSmallImage() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val tiles = useCase(bitmap)

        assertEquals(1, tiles.size)
        assertEquals(0, tiles[0].startX)
        assertEquals(0, tiles[0].startY)
        assertEquals(256, tiles[0].endX)
        assertEquals(256, tiles[0].endY)
    }

    @Test
    fun testAllTilesHaveCorrectSize() {
        val bitmap = Bitmap.createBitmap(1000, 800, Bitmap.Config.ARGB_8888)
        val tiles = useCase(bitmap)

        for (tile in tiles) {
            val width = tile.endX - tile.startX
            val height = tile.endY - tile.startY
            assertEquals(SplitImageIntoTilesUseCase.TILE_SIZE, width)
            assertEquals(SplitImageIntoTilesUseCase.TILE_SIZE, height)
        }
    }

    @Test
    fun testTileBitmapNotNull() {
        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        val tiles = useCase(bitmap)

        for (tile in tiles) {
            assertNotNull(tile.bitmap)
            assertEquals(256, tile.bitmap.width)
            assertEquals(256, tile.bitmap.height)
        }
    }
}