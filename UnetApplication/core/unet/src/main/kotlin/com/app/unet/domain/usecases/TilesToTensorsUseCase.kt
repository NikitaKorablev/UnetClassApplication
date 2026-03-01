package com.app.unet.domain.usecases

import com.app.model.Tile
import org.pytorch.Tensor

class TilesToTensorsUseCase {
    /**
     * Преобразует список фрагментов (Tile) в список входных тензоров PyTorch.
     * Включает нормализацию 0-255 -> 0.0-1.0 (аналог to_0_1_format_img).
     * @param tiles Список фрагментов, полученный из splitImageIntoTiles.
     * @return Список готовых к инференсу тензоров.
     */
    operator fun invoke(tiles: List<Tile>): List<Tensor> {
        val tensors = mutableListOf<Tensor>()

        for (tile in tiles) {
            // Создаем одноканальный тензор вручную
            val bitmap = tile.bitmap
            val width = bitmap.width
            val height = bitmap.height

            // Убедимся, что изображение действительно в градациях серого
            // Значения пикселей будут нормализованы от 0 до 1
            val floatArray = FloatArray(1 * 1 * height * width)

            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            for ((i, pixel) in pixels.withIndex()) {
                // Берем значение одного из каналов (так как в градациях серого R=G=B)
                val grayValue = (pixel shr 16) and 0xFF // Берем R канал, но в градации серого все каналы одинаковы
                floatArray[i] = grayValue / 255.0f // Нормализация к [0, 1]
            }

            // Создаем тензор с правильной формой [1, 1, height, width]
            val inputTensor = Tensor.fromBlob(
                floatArray,
                longArrayOf(1, 1, height.toLong(), width.toLong())
            )
            tensors.add(inputTensor)
        }
        return tensors
    }
}