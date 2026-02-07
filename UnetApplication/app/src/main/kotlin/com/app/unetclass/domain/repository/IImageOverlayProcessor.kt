package com.app.unetclass.domain.repository

import android.graphics.Bitmap

interface IImageOverlayProcessor {
    /**
     * Накладывает маски классов на изображение с заданным уровнем прозракности
     * @param classMasks маски классов
     * @param transparencyValues значения прозракности для каждого класса (0.0 - полностью прозрачный, 1.0 - полностью непрозрачный)
     * @return объединенное изображение с наложенными масками
     */
    fun overlayMasksWithTransparency(
        classMasks: List<Bitmap>,
        transparencyValues: List<Float>
    ): Bitmap
}