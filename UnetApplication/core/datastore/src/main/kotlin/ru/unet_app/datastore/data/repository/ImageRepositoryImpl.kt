package ru.unet_app.datastore.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import ru.unet_app.datastore.domain.repository.ImageRepository
import ru.unet_app.model.InferenceMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
): ImageRepository {
    override fun saveImage(
        bitmap: Bitmap,
        directory: File,
        fileName: String
    ): Boolean {
        return try {
            val file = File(directory, fileName)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, file.outputStream())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun createResultsDirectory(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        // Use Scoped-safe storage: getExternalFilesDir
        val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val unetClassDir = File(picturesDir, "UnetClass")
        unetClassDir.mkdirs()

        val resultsDir = File(unetClassDir, timestamp)
        resultsDir.mkdirs()

        Log.d("ImageRepositoryImpl", "Created results directory: ${resultsDir.absolutePath}")
        return resultsDir
    }

    override fun saveMetadata(metadata: InferenceMetadata, directory: File): Boolean {
        Log.d("ImageRepositoryImpl", "Saving metadata to directory: ${directory.absolutePath}")
        return try {
            if (!directory.exists()) {
                val created = directory.mkdirs()
                Log.d("ImageRepositoryImpl", "Directory did not exist. Created: $created")
            }

            val json = JSONObject().apply {
                put("width", metadata.width)
                put("height", metadata.height)
                put("executionTimeMs", metadata.executionTimeMs)
                put("memoryUsageBytes", metadata.memoryUsageBytes)
            }
            val file = File(directory, "metadata.json")
            file.writeText(json.toString())
            Log.d("ImageRepositoryImpl", "Metadata saved successfully to: ${file.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e("ImageRepositoryImpl", "Error saving metadata to ${directory.absolutePath}: ${e.message}", e)
            false
        }
    }

    override fun loadMetadata(directory: File): InferenceMetadata? {
        val file = File(directory, "metadata.json")
        if (!file.exists()) {
            Log.w("ImageRepositoryImpl", "Metadata file not found: ${file.absolutePath}")
            return null
        }

        return try {
            val json = JSONObject(file.readText())
            Log.d("ImageRepositoryImpl", "Metadata loaded from: ${file.absolutePath}")
            InferenceMetadata(
                width = json.getInt("width"),
                height = json.getInt("height"),
                executionTimeMs = json.getLong("executionTimeMs"),
                memoryUsageBytes = json.getLong("memoryUsageBytes")
            )
        } catch (e: Exception) {
            Log.e("ImageRepositoryImpl", "Error loading metadata: ${e.message}", e)
            null
        }
    }

}
