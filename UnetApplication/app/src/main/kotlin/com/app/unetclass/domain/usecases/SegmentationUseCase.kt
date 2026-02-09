package com.app.unetclass.domain.usecases

import android.graphics.Bitmap
import com.app.datastore.data.PredictionHistoryItem
import com.app.model.ResultState
import com.app.unetclass.models.SegmentationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SegmentationUseCase @Inject constructor(

) {
    suspend operator fun invoke(
        bitmap: Bitmap,
        onHistoryUpdate: (List<PredictionHistoryItem>) -> Unit
    ): ResultState<SegmentationResult, String> {
        val result = segmentationUseCase.startSegmentation(bitmap)

        // Если сегментация успешна, обновить историю
        if (result is ResultState.Success) {
            val historyItem = PredictionHistoryItem(
                timestamp = result.data.outputPath.substringAfterLast("/"),
                executionTime = result.data.totalTimeMs,
                outputPath = result.data.outputPath,
                imageWidth = result.data.imageWidth,
                imageHeight = result.data.imageHeight
            )

            // Вызвать коллбэк для обновления истории в UI
            withContext(Dispatchers.Main) {
                onHistoryUpdate(listOf(historyItem))
            }
        }

        return result
    }
}