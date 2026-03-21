package com.app.unet.domain

import android.graphics.Bitmap
import com.app.model.ResultState
import com.app.unet.domain.models.SegmentationResult

interface UnetModel {
    fun startSegmentation(bitmap: Bitmap): ResultState<SegmentationResult, String>
}