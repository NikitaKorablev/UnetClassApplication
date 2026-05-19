package com.app.datastore.domain.repository

import android.content.Context
import com.app.datastore.data.PredictionHistoryItem

interface PredictionHistoryRepository {
    suspend fun getAllSavedPredictions(context: Context): List<PredictionHistoryItem>
}