package com.app.unetclass.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PredictionHistoryItem(
    val timestamp: String, // Имя папки с результатами
    val executionTime: Long, // Время выполнения в миллисекундах
    val outputPath: String
) : Parcelable