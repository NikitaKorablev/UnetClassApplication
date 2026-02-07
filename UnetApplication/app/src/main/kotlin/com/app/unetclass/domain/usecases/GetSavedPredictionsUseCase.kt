package com.app.unetclass.domain.usecases

import com.app.unetclass.domain.repository.IPredictionHistoryRepository
import com.app.unetclass.models.PredictionHistoryItem
import android.content.Context

class GetSavedPredictionsUseCase(
    private val repository: IPredictionHistoryRepository
) {
    suspend operator fun invoke(context: Context): List<PredictionHistoryItem> {
        return repository.getAllSavedPredictions(context)
    }
}