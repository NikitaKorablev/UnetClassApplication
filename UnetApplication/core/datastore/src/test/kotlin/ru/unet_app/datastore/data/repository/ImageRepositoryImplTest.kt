package ru.unet_app.datastore.data.repository

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.unet_app.model.InferenceMetadata
import java.io.File

@RunWith(RobolectricTestRunner::class)
class ImageRepositoryImplTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val repository = ImageRepositoryImpl(context)

    @Test
    fun testSaveImage() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val dir = context.cacheDir
        val fileName = "test_image.png"

        val result = repository.saveImage(bitmap, dir, fileName)

        assertTrue(result)
        val file = File(dir, fileName)
        assertTrue(file.exists())
        assertTrue(file.length() > 0)
    }

    @Test
    fun testSaveImageFailure() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val dir = File("/invalid/path/that/does/not/exist")
        val fileName = "test_image.png"

        val result = repository.saveImage(bitmap, dir, fileName)

        assertTrue(!result)
    }

    @Test
    fun testCreateResultsDirectory() {
        val dir = repository.createResultsDirectory()

        assertNotNull(dir)
        assertTrue(dir.exists())
        assertTrue(dir.isDirectory)
        assertTrue(dir.absolutePath.contains("UnetClass"))
    }

    @Test
    fun testSaveAndLoadMetadata() {
        val dir = context.cacheDir
        val metadata = InferenceMetadata(
            width = 512,
            height = 512,
            executionTimeMs = 1234L,
            memoryUsageBytes = 1024L * 1024L
        )

        val saveResult = repository.saveMetadata(metadata, dir)
        assertTrue(saveResult)

        val loadedMetadata = repository.loadMetadata(dir)
        assertNotNull(loadedMetadata)
        assertEquals(metadata.width, loadedMetadata!!.width)
        assertEquals(metadata.height, loadedMetadata.height)
        assertEquals(metadata.executionTimeMs, loadedMetadata.executionTimeMs)
        assertEquals(metadata.memoryUsageBytes, loadedMetadata.memoryUsageBytes)
    }

    @Test
    fun testLoadMetadataNonExistent() {
        val dir = context.cacheDir
        val loadedMetadata = repository.loadMetadata(dir)

        assertNull(loadedMetadata)
    }

    @Test
    fun testLoadMetadataCorruptedJson() {
        val dir = context.cacheDir
        val file = File(dir, "metadata.json")
        file.writeText("invalid json")

        val loadedMetadata = repository.loadMetadata(dir)

        assertNull(loadedMetadata)
    }

    @Test
    fun testSaveMetadataCreatesDirectory() {
        val tempDir = File(context.cacheDir, "new_subdir")
        val metadata = InferenceMetadata(256, 256, 100L, 200L)

        val saveResult = repository.saveMetadata(metadata, tempDir)

        assertTrue(saveResult)
        assertTrue(tempDir.exists())
        assertTrue(File(tempDir, "metadata.json").exists())
    }
}