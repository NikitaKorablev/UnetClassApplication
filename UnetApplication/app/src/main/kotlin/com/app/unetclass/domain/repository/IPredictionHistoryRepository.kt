package com.app.unetclass.domain.repository

import com.app.unetclass.models.PredictionHistoryItem
import android.content.Context

interface IPredictionHistoryRepository {
    suspend fun getAllSavedPredictions(context: Context): List<PredictionHistoryItem>
}