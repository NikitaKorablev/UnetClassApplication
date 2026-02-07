package com.app.unetclass.models

import android.graphics.Bitmap

data class SegmentationResult(
    val bitmap: Bitmap,
    val outputPath: String,
    val totalTimeMs: Long,
    val imageWidth: Int = bitmap.width,
    val imageHeight: Int = bitmap.height
)