package ru.unet_app.unetclass.domain.usecases

import android.content.Context
import ru.unet_app.datastore.data.PredictionHistoryItem
import ru.unet_app.datastore.domain.repository.PredictionHistoryRepository
import javax.inject.Inject

class GetSavedPredictionsUseCase @Inject constructor(
    private val repository: PredictionHistoryRepository
) {
    suspend operator fun invoke(context: Context): List<PredictionHistoryItem> {
        return repository.getAllSavedPredictions(context)
    }
}
