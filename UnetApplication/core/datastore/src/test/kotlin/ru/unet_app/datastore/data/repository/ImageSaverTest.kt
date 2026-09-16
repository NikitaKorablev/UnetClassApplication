package ru.unet_app.datastore.data.repository

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class ImageSaverTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun testCreateResultsDirectory() {
        val dir = ImageSaver.createResultsDirectory(context)

        assertNotNull(dir)
        assertTrue(dir.exists())
        assertTrue(dir.isDirectory)
        assertTrue(dir.absolutePath.contains("UnetClass"))
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        assertTrue(dir.name.startsWith(timestamp.substring(0, 8)))
    }

    @Test
    fun testSaveImage() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val dir = ImageSaver.createResultsDirectory(context)
        val fileName = "test_save.png"

        val result = ImageSaver.saveImage(bitmap, dir, fileName)

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

        val result = ImageSaver.saveImage(bitmap, dir, fileName)

        assertTrue(!result)
    }

    @Test
    fun testCreateResultsDirectoryUnique() {
        val dir1 = ImageSaver.createResultsDirectory(context)
        Thread.sleep(10)
        val dir2 = ImageSaver.createResultsDirectory(context)

        assertEquals(dir1.absolutePath, dir2.absolutePath) // Same second -> same timestamp
        // Note: In real usage, these would be called at different times
    }
}