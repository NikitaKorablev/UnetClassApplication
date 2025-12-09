package com.app.unetclass.domain.repository

import android.graphics.Bitmap

interface ITransparencyImageSaver {
    /**
     * Сохраняет настроенное изображение с прозракостью
     * @param bitmap изображение для сохранения
     * @param outputPath путь к директории для сохранения
     * @return путь к сохраненному файлу или null в случае ошибки
     */
    fun saveTransparencyImage(bitmap: Bitmap, outputPath: String): String?
}