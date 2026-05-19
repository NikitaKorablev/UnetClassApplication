package ru.unet_app.datastore.domain.repository

import android.content.Context
import ru.unet_app.datastore.data.PredictionHistoryItem

interface PredictionHistoryRepository {
    suspend fun getAllSavedPredictions(context: Context): List<PredictionHistoryItem>
}