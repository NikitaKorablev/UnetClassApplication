package com.app.unetclass.domain.usecases

import android.graphics.Bitmap
import com.app.datastore.domain.repository.ImageRepository
import com.app.model.ClassNames
import com.app.model.InferenceMetadata
import com.app.unet.data.ImageStitcherResult
import javax.inject.Inject

class SaveImageStitcherUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    /**
     * Сохраняет результаты сегментации в указанную папку
     *
     * @param unitedMask Финальная маска
     * @param classMasks Список масок для каждого класса
     * @param metadata Метаданные инференса
     * @return Путь к папке с результатами
     */
    operator fun invoke(
        unitedMask: Bitmap,
        classMasks: List<Bitmap>,
        metadata: InferenceMetadata
    ): String {
        // Создаем уникальную папку для сохранения результатов
        val resultsDir = imageRepository.createResultsDirectory()

        // Сохраняем путь для последующего использования
        val lastSavedPath = resultsDir.absolutePath

        // Сохраняем метаданные
        imageRepository.saveMetadata(metadata, resultsDir)

        // Сохраняем финальную маску
        val unitedMaskSaved = imageRepository.saveImage(
            unitedMask,
            resultsDir,
            "united_mask.png"
        )

        // Сохраняем маски для каждого класса с именами классов
        var allClassMasksSaved = true
        for (i in classMasks.indices) {
            val classMaskSaved = imageRepository.saveImage(
                classMasks[i],
                resultsDir,
                "${ClassNames.NAMES[i]}_prediction.png"
            )
            allClassMasksSaved = allClassMasksSaved && classMaskSaved
        }

        if (unitedMaskSaved && allClassMasksSaved) return lastSavedPath

        throw Exception("Не удалось сохранить маски")
    }
}