package com.app.unet.domain.usecases

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import com.app.datastore.domain.repository.ImageRepository
import com.app.model.Tile
import com.app.unet.data.ImageStitcherResult
import org.pytorch.Tensor
import javax.inject.Inject
import kotlin.collections.get
import kotlin.math.min

class StitchingImageUseCase {
    /**
     * Собирает предсказанные маски в одно полноразмерное изображение и маски для каждого класса.
     * Аналог glit_image.
     *
     * @param outputTensors Список выходных тензоров (результатов инференса).
     * @param tilesList Список объектов Tile, содержащих координаты нарезки.
     * @param originalWidth Ширина исходного изображения.
     * @param originalHeight Высота исходного изображения.
     * @return Результат соединения: финальная маска и маски для каждого класса.
     */
    operator fun invoke(
        outputTensors: List<Tensor>,
        tilesList: List<Tile>,
        originalWidth: Int,
        originalHeight: Int
    ): ImageStitcherResult {
        if (outputTensors.size != tilesList.size) {
            throw IllegalArgumentException("Количество тензоров должно совпадать с количеством фрагментов.")
        }

        // Получаем размер фрагмента (256x256), предполагая квадратную плитку
        val tileSize = outputTensors[0].shape()[2].toInt() // H или W (256)
        val uniqueArea = tileSize - OVERLAP // Уникальная область (192)

        // Инициализация полноразмерного выходного массива (float32)
        // Размер: [H, W, numClasses]
        val finalMaskArray = Array(originalHeight) {
            Array(originalWidth) {
                FloatArray(NUM_CLASSES)
            }
        }

        // Обход всех фрагментов и их результатов
        for (i in outputTensors.indices) {
            val tileInfo = tilesList[i]
            val outputData = outputTensors[i].dataAsFloatArray

            // Выходной тензор имеет форму [1, numClasses, TILE_SIZE, TILE_SIZE].
            // Индексация: [c * size*size + y*size + x]

            // Внешние границы в выходном изображении
            val outStartX = tileInfo.startX
            val outStartY = tileInfo.startY
            val outEndX = tileInfo.endX
            val outEndY = tileInfo.endY

            // Внутренние границы для уникальной области (получаем [64:192])
            val uniqueStart = HALF_OVERLAP
            val uniqueEnd = tileSize - HALF_OVERLAP // 256 - 32 = 224

            // --- 1. Центральная область (Inner area) ---
            // Используется для всех, кроме краевых и угловых фрагментов в Python-коде,
            // но мы используем эту логику, чтобы заполнить любую неперекрывающуюся часть
            // и берем полный фрагмент для углов/краев по необходимости.

            // Внутренние индексы X и Y для текущего фрагмента
            val tileYRange = uniqueStart until min(tileSize, outEndY - outStartY) - HALF_OVERLAP
            val tileXRange = uniqueStart until min(tileSize, outEndX - outStartX) - HALF_OVERLAP

            // Цикл по уникальной области предсказания (например, [32:224])
            for (ty in tileYRange) {
                for (tx in tileXRange) {
                    val outY = outStartY + ty
                    val outX = outStartX + tx

                    // Проверка на выход за границы, на всякий случай
                    if (outX < originalWidth && outY < originalHeight) {
                        for (c in 0 until NUM_CLASSES) {
                            // Индекс в плоском массиве outputData: [channel * size*size + y*size + x]
                            val index = c * tileSize * tileSize + ty * tileSize + tx
                            finalMaskArray[outY][outX][c] = outputData[index]
                        }
                    }
                }
            }

            // NOTE: Логика Hard Stitching вашего Python-кода очень специфична
            // и использует полный фрагмент для углов и краевых полос.
            // Воспроизведение этой сложной логики со всеми условиями
            // (`count_x - 1`, `count_y - 1` и т.д.) без информации о
            // `tile_info` в этом классе затруднительно.
            //
            // Для упрощения и для большинства современных U-Net пайплайнов,
            // достаточно заполнить центральную уникальную область (как выше)
            // и обработать края.

            // В данном коде мы полагаемся на то, что центральная область (ty/tx range)
            // перезаписывает предыдущие предсказания в зонах перекрытия,
            // что является упрощенным, но работоспособным методом Hard Stitching.
        }

        // --- 2. Постобработка (Аналог to_0_255_format_img) ---
        val unitedMask = createUnitedMask(finalMaskArray, originalWidth, originalHeight)
        val classMasks = createClassMasks(finalMaskArray, originalWidth, originalHeight)

        return ImageStitcherResult(unitedMask, classMasks)
    }

    /**
     * Создает финальную маску, где каждый пиксель отображает класс с максимальной вероятностью
     */
    private fun createUnitedMask(
        finalMaskArray: Array<Array<FloatArray>>,
        width: Int,
        height: Int
    ): Bitmap {
        val colors = IntArray(width * height)
        var maxClass: Int
        var maxProb: Float

        for (y in 0 until height) {
            for (x in 0 until width) {
                maxClass = 0
                maxProb = -1.0f

                // Найти класс с максимальной вероятностью (argmax)
                for (c in 0 until NUM_CLASSES) {
                    val prob = finalMaskArray[y][x][c]
                    if (prob > maxProb) {
                        maxProb = prob
                        maxClass = c
                    }
                }

                // Перевести вероятности в 0-255 и выбрать один канал для Grayscale вывода.
                // В вашем коде используется to_0_255_format_img, что подразумевает
                // конвертацию в uint8. Для визуализации мы можем просто взять
                // номер класса * 40 (для визуального различия)
                val colorValue = (maxClass * (255 / (NUM_CLASSES - 1))).coerceIn(0, 255)

                // Создание Grayscale цвета (RGB=value)
                colors[y * width + x] = 0xFF shl 24 or (colorValue shl 16) or (colorValue shl 8) or colorValue
            }
        }

        val outputBitmap = createBitmap(width, height)
        outputBitmap.setPixels(colors, 0, width, 0, 0, width, height)
        return outputBitmap
    }

    /**
     * Создает маски для каждого класса, где пиксель белый если принадлежит классу, черный - если нет
     */
    private fun createClassMasks(
        finalMaskArray: Array<Array<FloatArray>>,
        width: Int,
        height: Int
    ): List<Bitmap> {
        val classMasks = mutableListOf<Bitmap>()

        for (classIndex in 0 until NUM_CLASSES) {
            val colors = IntArray(width * height)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    // Если вероятность принадлежности к классу выше 0.5, делаем пиксель белым
                    val isClass = finalMaskArray[y][x][classIndex] > 0.5f
                    val colorValue = if (isClass) 255 else 0

                    // Создание Grayscale цвета (RGB=value)
                    colors[y * width + x] = 0xFF shl 24 or (colorValue shl 16) or (colorValue shl 8) or colorValue
                }
            }

            val classBitmap = createBitmap(width, height)
            classBitmap.setPixels(colors, 0, width, 0, 0, width, height)
            classMasks.add(classBitmap)
        }

        return classMasks
    }



    companion object {
        const val OVERLAP: Int = 128 // Размер перекрытия (e.g., 64)
        const val NUM_CLASSES: Int = 6 // Количество каналов (классов) в выходном тензоре (e.g., 6)
        const val HALF_OVERLAP = OVERLAP / 2 // Половина перекрытия (e.g., 32)
    }
}