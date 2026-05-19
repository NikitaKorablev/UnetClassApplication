package com.app.datastore.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PredictionHistoryItem(
    val timestamp: String, // Имя папки с результатами
    val executionTime: Long, // Время выполнения в миллисекундах
    val memoryUsageBytes: Long,
    val outputPath: String,
    val imageWidth: Int,
    val imageHeight: Int
) : Parcelable