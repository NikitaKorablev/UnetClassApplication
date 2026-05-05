package com.app.model

data class InferenceMetadata(
    val width: Int,
    val height: Int,
    val executionTimeMs: Long,
    val memoryUsageBytes: Long
)
