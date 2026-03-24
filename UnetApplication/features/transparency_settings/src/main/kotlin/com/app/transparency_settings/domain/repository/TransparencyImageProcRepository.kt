package com.app.transparency_settings.domain.repository

import android.graphics.Bitmap
import com.app.model.PredictedClasses
import com.app.model.TransparencyState

interface TransparencyImageProcRepository {
    /**
     * Накладывает маски классов на изображение с заданным уровнем прозракности
     * @param classMasks маски классов
     * @param state значения прозракности для каждого класса (0.0 - полностью прозрачный, 1.0 - полностью непрозрачный)
     * @return объединенное изображение с наложенными масками
     */
    fun overlayMasksWithTransparency(
        classMasks: Map<PredictedClasses, Bitmap>,
        state: TransparencyState
    ): Bitmap
}