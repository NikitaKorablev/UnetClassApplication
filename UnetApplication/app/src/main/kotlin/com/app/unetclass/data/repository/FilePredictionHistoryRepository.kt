package com.app.unetclass.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Environment
import com.app.unetclass.domain.repository.IPredictionHistoryRepository
import com.app.unetclass.models.PredictionHistoryItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FilePredictionHistoryRepository : IPredictionHistoryRepository {
    private companion object {
        const val NUM_CLASSES = 6
    }
    
    override suspend fun getAllSavedPredictions(context: Context): List<PredictionHistoryItem> {
        val directories = getAllPredictionDirectories(context)
        val historyItems = mutableListOf<PredictionHistoryItem>()
        
        for (directory in directories) {
            val historyItem = createHistoryItemFromDirectory(directory)
            if (historyItem != null) {
                historyItems.add(historyItem)
            }
        }
        
        // Сортируем по убыванию времени (новые сверху)
        return historyItems.sortedByDescending { it.timestamp }
    }
    
    private fun getAllPredictionDirectories(context: Context): List<File> {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val unetClassDir = File(picturesDir, "UnetClass")
        if (!unetClassDir.exists()) return emptyList()
        
        return unetClassDir.listFiles { file -> file.isDirectory }?.toList() ?: emptyList()
    }
    
    private fun isPredictionDirectoryValid(directory: File): Boolean {
        // Проверяем наличие всех необходимых файлов
        val requiredFiles = listOf("united_mask.png") + 
            (0 until NUM_CLASSES).map { "class_$it.png" }
        
        for (fileName in requiredFiles) {
            val file = File(directory, fileName)
            if (!file.exists()) {
                return false
            }
        }
        
        // Проверяем, что все изображения имеют одинаковые размеры
        val dimensionsList = mutableListOf<Pair<Int, Int>>()
        for (fileName in requiredFiles) {
            val filePath = File(directory, fileName).absolutePath
            val dimensions = getImageDimensions(filePath) ?: return false
            dimensionsList.add(dimensions)
        }
        
        // Проверяем, что все измерения одинаковы
        if (dimensionsList.distinct().size > 1) {
            return false
        }
        
        return true
    }
    
    private fun getImageDimensions(filePath: String): Pair<Int, Int>? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        return if (options.outWidth > 0 && options.outHeight > 0) {
            Pair(options.outWidth, options.outHeight)
        } else {
            null
        }
    }
    
    private fun createHistoryItemFromDirectory(directory: File): PredictionHistoryItem? {
        if (!isPredictionDirectoryValid(directory)) {
            return null
        }
        
        // Извлекаем временную метку из имени папки
        val timestamp = directory.name
        val outputPath = directory.absolutePath
        
        // Получаем размерности изображения
        val dimensions = getImageDimensions(File(directory, "united_mask.png").absolutePath)
        if (dimensions == null) {
            return null
        }

        val (width, height) = dimensions

        return PredictionHistoryItem(
            timestamp = timestamp,
            executionTime = 0, // Точное время выполнения недоступно из файловой системы
            outputPath = outputPath,
            imageWidth = width,
            imageHeight = height
        )
    }

}