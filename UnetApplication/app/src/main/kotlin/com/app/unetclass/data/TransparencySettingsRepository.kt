package com.app.unetclass.data

import android.graphics.Bitmap
import com.app.unetclass.domain.repository.ITransparencyImageSaver

class TransparencySettingsRepository : ITransparencyImageSaver {
    override fun saveTransparencyImage(bitmap: Bitmap, outputPath: String): String? {
        return try {
            val file = java.io.File(outputPath)
            if (!file.exists()) {
                file.mkdirs()
            }

            // Генерируем уникальное имя файла
            val fileName = generateUniqueFileName(outputPath)
            val outputFile = java.io.File(file, fileName)

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputFile.outputStream())
            
            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun generateUniqueFileName(outputPath: String): String {
        val outputDir = java.io.File(outputPath)
        var counter = 0
        var fileName: String
        
        do {
            fileName = "united_mask_${counter}.png"
            counter++
        } while (java.io.File(outputDir, fileName).exists() && counter < 10000) // Защита от бесконечного цикла
        
        return fileName
    }
}