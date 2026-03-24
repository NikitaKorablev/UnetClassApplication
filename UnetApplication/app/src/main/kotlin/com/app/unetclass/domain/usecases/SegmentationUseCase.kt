package com.app.unetclass.domain.usecases

import android.graphics.Bitmap
import com.app.datastore.data.PredictionHistoryItem
import com.app.model.ImageData
import com.app.model.ResultState
import com.app.unet.data.unetmodels.PyTorchModel
import com.app.unet.di.PytorchModel
import com.app.unet.domain.UnetModel
import com.app.unetclass.domain.usecases.SaveImageStitcherUseCase
import com.app.unet.models.SegmentationResult
import com.app.unet.domain.usecases.SplitImageIntoTilesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SegmentationUseCase @Inject constructor(
    @param:PytorchModel private val model: UnetModel,
    private val imageToTiles: SplitImageIntoTilesUseCase,
) {
    operator fun invoke(
        bitmap: Bitmap,
        onHistoryUpdate: (List<PredictionHistoryItem>) -> Unit
    ): ResultState<SegmentationResult, String> {
        val tiles = imageToTiles(bitmap)

        val startTime = System.currentTimeMillis()
        val result = model.predict(
            ImageData(bitmap.width, bitmap.height, tiles)
        )
        val totalTime = System.currentTimeMillis() - startTime

        return when(result) {
            is ResultState.Error -> ResultState.Error(result.error)
            is ResultState.Success -> ResultState.Success(
                SegmentationResult(
                    labeledData = result.data,
                    totalTimeMs = totalTime,
                )
            )
        }
    }
}