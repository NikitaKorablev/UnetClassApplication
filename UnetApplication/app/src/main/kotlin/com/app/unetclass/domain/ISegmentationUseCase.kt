package com.app.unetclass.domain

import com.app.unetclass.models.SegmentationResult
import android.graphics.Bitmap
import com.app.model.ResultState

interface ISegmentationUseCase {
    suspend fun startSegmentation(bitmap: Bitmap): ResultState<SegmentationResult, String>
}