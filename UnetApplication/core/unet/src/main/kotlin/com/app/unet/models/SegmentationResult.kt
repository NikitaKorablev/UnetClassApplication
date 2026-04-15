package com.app.unet.models

data class SegmentationResult(
    val labeledData: LabeledData,
    val totalTimeMs: Long,
    val memoryUsageBytes: Long = 0L
)