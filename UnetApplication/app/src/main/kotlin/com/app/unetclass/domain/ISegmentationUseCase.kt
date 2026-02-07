package com.app.unetclass.domain

import com.app.unetclass.models.SegmentationResult
import com.app.unetclass.utils.ResultState
import android.graphics.Bitmap

interface ISegmentationUseCase {
    suspend fun startSegmentation(bitmap: Bitmap): ResultState<SegmentationResult, String>
}