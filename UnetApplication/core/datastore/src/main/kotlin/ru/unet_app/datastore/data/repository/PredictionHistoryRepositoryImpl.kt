package ru.unet_app.datastore.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Environment
import ru.unet_app.datastore.data.PredictionHistoryItem
import ru.unet_app.datastore.domain.repository.PredictionHistoryRepository
import com.app.model.ClassNames
import com.app.model.InferenceMetadata
import org.json.JSONObject
import java.io.File

class PredictionHistoryRepositoryImpl
: PredictionHistoryRepository {
    private companion object {
        const val NUM_CLASSES = 6
    }

    override suspend fun getAllSavedPredictions(context: Context): List<PredictionHistoryItem> {
        val directories = getAllPredictionDirectories()
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

    private fun getAllPredictionDirectories(): List<File> {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val unetClassDir = File(picturesDir, "UnetClass")
        if (!unetClassDir.exists()) return emptyList()

        return unetClassDir.listFiles { file -> file.isDirectory }?.toList() ?: emptyList()
    }

    private fun isPredictionDirectoryValid(directory: File): Boolean {
        // Проверяем наличие всех необходимых файлов
        val requiredFiles = listOf("united_mask.png") +
                ClassNames.NAMES.map { "${it}_prediction.png" }

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
        if (!isPredictionDirectoryValid(directory)) return null

        val metadataFile = File(directory, "metadata.json")
        val metadata = if (metadataFile.exists()) {
            try {
                val json = JSONObject(metadataFile.readText())
                InferenceMetadata(
                    width = json.getInt("width"),
                    height = json.getInt("height"),
                    executionTimeMs = json.getLong("executionTimeMs"),
                    memoryUsageBytes = json.getLong("memoryUsageBytes")
                )
            } catch (e: Exception) {
                null
            }
        } else null

        // Извлекаем временную метку из имени папки
        val timestamp = directory.name
        val outputPath = directory.absolutePath

        // Получаем размерности изображения
        val width: Int
        val height: Int
        val executionTime: Long
        val memoryUsage: Long

        if (metadata != null) {
            width = metadata.width
            height = metadata.height
            executionTime = metadata.executionTimeMs
            memoryUsage = metadata.memoryUsageBytes
        } else {
            val dimensions = getImageDimensions(
                File(directory, "united_mask.png").absolutePath
            ) ?: return null
            width = dimensions.first
            height = dimensions.second
            executionTime = 0
            memoryUsage = 0
        }

        return PredictionHistoryItem(
            timestamp = timestamp,
            executionTime = executionTime,
            memoryUsageBytes = memoryUsage,
            outputPath = outputPath,
            imageWidth = width,
            imageHeight = height
        )
    }
}