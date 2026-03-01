package com.app.unet.domain.usecases

import com.app.datastore.domain.repository.ImageRepository
import com.app.unet.data.ImageStitcherResult
import com.app.unet.utils.ClassNames
import javax.inject.Inject

class SaveImageStitcherUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    /**
     * Сохраняет результаты сегментации в указанную папку
     *
     * @param result Результат сегментации
     * @param context Контекст приложения для доступа к файловой системе
     * @return true, если сохранение прошло успешно, иначе false
     */
    operator fun invoke(result: ImageStitcherResult): String? {
        try {
            // Создаем уникальную папку для сохранения результатов
            val resultsDir = imageRepository.createResultsDirectory()

            // Сохраняем путь для последующего использования
            val lastSavedPath = resultsDir.absolutePath

            // Сохраняем финальную маску
            val unitedMaskSaved = imageRepository.saveImage(
                result.unitedMask,
                resultsDir,
                "united_mask.png"
            )

            // Сохраняем маски для каждого класса с именами классов
            var allClassMasksSaved = true
            for (i in result.classMasks.indices) {
                val classMaskSaved = imageRepository.saveImage(
                    result.classMasks[i],
                    resultsDir,
                    "${ClassNames.NAMES[i]}_prediction.png"
                )
                allClassMasksSaved = allClassMasksSaved && classMaskSaved
            }

            return if (unitedMaskSaved && allClassMasksSaved) lastSavedPath
            else null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}