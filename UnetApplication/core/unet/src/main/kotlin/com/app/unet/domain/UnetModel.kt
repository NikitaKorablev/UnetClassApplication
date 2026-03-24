package com.app.unet.domain

import com.app.model.ImageData
import com.app.model.ResultState
import com.app.unet.models.LabeledData
import com.app.unet.models.SegmentationResult

interface UnetModel {
    fun predict(inputImageData: ImageData): ResultState<LabeledData, String>
}