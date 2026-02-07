package com.app.unetclass.models

import android.graphics.Bitmap

data class Tile(
    val bitmap: Bitmap,
    val startX: Int,
    val startY: Int,
    val endX: Int,
    val endY: Int
)