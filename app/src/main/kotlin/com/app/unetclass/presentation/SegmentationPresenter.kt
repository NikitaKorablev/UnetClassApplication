package com.app.unetclass.presentation

import com.app.unetclass.domain.ISegmentationUseCase
import com.app.unetclass.domain.ITimeMeasurementUseCase
import com.app.unetclass.models.PredictionHistoryItem
import com.app.unetclass.models.SegmentationResult
import com.app.unetclass.utils.ResultState
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SegmentationPresenter(
    private val segmentationUseCase: ISegmentationUseCase,
    private val timeMeasurementUseCase: ITimeMeasurementUseCase
) {
    suspend fun startSegmentation(
        bitmap: Bitmap,
        onHistoryUpdate: (List<PredictionHistoryItem>) -> Unit
    ): ResultState<SegmentationResult, String> {
        val result = segmentationUseCase.startSegmentation(bitmap)

        // Если сегментация успешна, обновить историю
        if (result is ResultState.Success) {
            val historyItem = PredictionHistoryItem(
                timestamp = result.data.outputPath.substringAfterLast("/"),
                executionTime = result.data.totalTimeMs,
                outputPath = result.data.outputPath
            )

            // Вызвать коллбэк для обновления истории в UI
            withContext(Dispatchers.Main) {
                onHistoryUpdate(listOf(historyItem))
            }
        }

        return result
    }
}