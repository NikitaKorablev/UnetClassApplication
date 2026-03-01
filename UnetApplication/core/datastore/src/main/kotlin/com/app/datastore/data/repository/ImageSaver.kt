package com.app.datastore.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageSaver {

    /**
     * Создает уникальную папку для сохранения результатов сегментации
     * Имя папки формируется по шаблону: yyyyMMdd_HHmmss
     */
    fun createResultsDirectory(context: Context): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val unetClassDir = File(picturesDir, "UnetClass")
        unetClassDir.mkdirs()

        val resultsDir = File(unetClassDir, timestamp)
        resultsDir.mkdirs()

        return resultsDir
    }

    /**
     * Сохраняет изображение в указанную папку
     */
    fun saveImage(bitmap: Bitmap, directory: File, fileName: String): Boolean {
        return try {
            val file = File(directory, fileName)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, file.outputStream())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Сохраняет изображение через MediaStore (для Android 10+)
     */
    fun saveImageUsingMediaStore(context: Context, bitmap: Bitmap, directoryName: String, fileName: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/UnetClass/$directoryName")
            }

            return try {
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    true
                } ?: false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else {
            // Для более старых версий Android используем традиционный подход
            val resultsDir = createResultsDirectory(context)
            return saveImage(bitmap, resultsDir, fileName)
        }
    }
}