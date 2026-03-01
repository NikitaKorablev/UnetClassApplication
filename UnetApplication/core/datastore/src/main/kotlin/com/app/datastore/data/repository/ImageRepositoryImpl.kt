package com.app.datastore.data.repository

import android.graphics.Bitmap
import android.os.Environment
import com.app.datastore.domain.repository.ImageRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageRepositoryImpl: ImageRepository {
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
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val unetClassDir = File(picturesDir, "UnetClass")
        unetClassDir.mkdirs()

        val resultsDir = File(unetClassDir, timestamp)
        resultsDir.mkdirs()

        return resultsDir
    }

}