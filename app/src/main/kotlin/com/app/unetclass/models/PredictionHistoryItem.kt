package com.app.unetclass.models

data class PredictionHistoryItem(
    val timestamp: String, // Имя папки с результатами
    val executionTime: Long, // Время выполнения в миллисекундах
    val outputPath: String
)