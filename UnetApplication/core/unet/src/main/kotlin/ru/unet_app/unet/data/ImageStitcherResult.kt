package ru.unet_app.unet.data

import android.graphics.Bitmap

data class ImageStitcherResult(
    val unitedMask: Bitmap, // Финальная маска (как сейчас)
    val classMasks: List<Bitmap> // Маски для каждого класса
)