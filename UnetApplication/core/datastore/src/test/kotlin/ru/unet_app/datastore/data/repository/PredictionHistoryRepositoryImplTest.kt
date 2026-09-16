package ru.unet_app.datastore.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.unet_app.model.ClassNames
import ru.unet_app.model.InferenceMetadata
import java.io.File

@RunWith(RobolectricTestRunner::class)
class PredictionHistoryRepositoryImplTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val repository = PredictionHistoryRepositoryImpl()

    @Test
    fun testGetAllSavedPredictionsEmpty() = runTest {
        val results = repository.getAllSavedPredictions(context)

        assertNotNull(results)
        assertEquals(0, results.size)
    }

    @Test
    fun testValidPredictionDirectory() = runTest {
        val baseDir = createValidPredictionDirectory()

        val results = repository.getAllSavedPredictions(context)

        assertEquals(1, results.size)
        val item = results[0]
        assertEquals(baseDir.name, item.timestamp)
        assertEquals(baseDir.absolutePath, item.outputPath)
        assertEquals(256, item.imageWidth)
        assertEquals(256, item.imageHeight)
        assertEquals(1000L, item.executionTime)
        assertEquals(2048L, item.memoryUsageBytes)
    }

    @Test
    fun testInvalidDirectoryMissingFiles() = runTest {
        val baseDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "UnetClass/invalid_dir")
        baseDir.mkdirs()
        File(baseDir, "united_mask.png").createNewFile()

        val results = repository.getAllSavedPredictions(context)

        assertEquals(0, results.size)
        
        baseDir.deleteRecursively()
    }

    @Test
    fun testInvalidDirectoryDimensionMismatch() = runTest {
        val baseDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "UnetClass/mismatch_dir")
        baseDir.mkdirs()
        
        createTestImage(File(baseDir, "united_mask.png"), 256, 256)
        createTestImage(File(baseDir, "mitochondria_prediction.png"), 256, 256)
        createTestImage(File(baseDir, "PSD_prediction.png"), 128, 128) // Different size
        createTestImage(File(baseDir, "vesicles_prediction.png"), 256, 256)
        createTestImage(File(baseDir, "axon_prediction.png"), 256, 256)
        createTestImage(File(baseDir, "boundaries_prediction.png"), 256, 256)
        createTestImage(File(baseDir, "mitochondrial_boundaries_prediction.png"), 256, 256)

        val results = repository.getAllSavedPredictions(context)

        assertEquals(0, results.size)
        
        baseDir.deleteRecursively()
    }

    @Test
    fun testMetadataMissing() = runTest {
        val baseDir = createValidPredictionDirectory()
        File(baseDir, "metadata.json").delete()

        val results = repository.getAllSavedPredictions(context)

        assertEquals(1, results.size)
        val item = results[0]
        assertEquals(0L, item.executionTime)
        assertEquals(0L, item.memoryUsageBytes)
    }

    private fun createValidPredictionDirectory(): File {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val unetClassDir = File(picturesDir, "UnetClass")
        unetClassDir.mkdirs()

        val timestamp = "20240101_120000"
        val baseDir = File(unetClassDir, timestamp)
        baseDir.mkdirs()

        val size = 256
        createTestImage(File(baseDir, "united_mask.png"), size, size)
        for (className in ClassNames.NAMES) {
            createTestImage(File(baseDir, "${className}_prediction.png"), size, size)
        }

        val metadata = InferenceMetadata(
            width = size,
            height = size,
            executionTimeMs = 1000L,
            memoryUsageBytes = 2048L
        )
        val json = JSONObject().apply {
            put("width", metadata.width)
            put("height", metadata.height)
            put("executionTimeMs", metadata.executionTimeMs)
            put("memoryUsageBytes", metadata.memoryUsageBytes)
        }
        File(baseDir, "metadata.json").writeText(json.toString())

        return baseDir
    }

    private fun createTestImage(file: File, width: Int, height: Int) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, file.outputStream())
    }
}