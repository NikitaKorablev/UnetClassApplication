package com.app.model

import android.graphics.Bitmap

data class Tile(
    val bitmap: Bitmap,
    val startX: Int,
    val startY: Int,
    val endX: Int,
    val endY: Int
) {
    companion object {
        const val SIZE = 256
        const val SHIFT: Int = 32
    }
}