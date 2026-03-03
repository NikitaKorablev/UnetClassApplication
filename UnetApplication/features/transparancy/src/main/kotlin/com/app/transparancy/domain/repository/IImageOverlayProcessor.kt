package com.app.transparancy.domain.repository

import android.graphics.Bitmap
import com.app.model.TransparencyType
import com.app.transparancy.presentation.model.TransparencyState

interface IImageOverlayProcessor {
    /**
     * Накладывает маски классов на изображение с заданным уровнем прозракности
     * @param classMasks маски классов
     * @param state значения прозракности для каждого класса (0.0 - полностью прозрачный, 1.0 - полностью непрозрачный)
     * @return объединенное изображение с наложенными масками
     */
    fun overlayMasksWithTransparency(
        classMasks: Map<TransparencyType, Bitmap>,
        state: TransparencyState
    ): Bitmap
}