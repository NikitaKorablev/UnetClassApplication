package com.app.unetclass.data

import android.graphics.Bitmap
import com.app.unetclass.models.Tile
import org.pytorch.Tensor

class ImageProcessor(
    private val tileSize: Int, // Размер фрагмента (e.g., 256)
    private val overlap: Int    // Размер перекрытия (e.g., 64)
) {
    private val step = tileSize - overlap // Уникальная область (e.g., 192)

    /**
     * Выполняет нарезку изображения на фрагменты с перекрытием.
     * Аналог логики нарезки в test_data.
     */
    fun splitImageIntoTiles(inputBitmap: Bitmap): List<Tile> {
        val width = inputBitmap.width
        val height = inputBitmap.height
        val tiles = mutableListOf<Tile>()

        var y = 0
        while (y < height) {
            var x = 0
            // Переменные для отслеживания крайних координат фрагмента
            var lastY = y

            while (x < width) {

                var currentX = x
                var currentY = y

                // 1. Обработка правого края: сдвиг, если фрагмент выходит за границу
                if (currentX + tileSize > width) {
                    currentX = width - tileSize
                }

                // 2. Обработка нижнего края: сдвиг, если фрагмент выходит за границу
                if (currentY + tileSize > height) {
                    currentY = height - tileSize
                }

                // Обновляем lastY, чтобы использовать его для проверки выхода из внешнего цикла
                lastY = currentY

                // Вырезаем фрагмент (Bitmap.createBitmap безопасно работает с координатами)
                val tileBitmap = Bitmap.createBitmap(
                    inputBitmap,
                    currentX,
                    currentY,
                    tileSize,
                    tileSize
                )

                tiles.add(
                    Tile(
                        tileBitmap,
                        currentX,
                        currentY,
                        currentX + tileSize,
                        currentY + tileSize
                    )
                )

                // Шаг по горизонтали. Если достигли края, выход.
                if (currentX + tileSize == width) break
                x += step
            }

            // Шаг по вертикали. Если достигли края, выход.
            // Используем lastY (скорректированную координату), чтобы проверить выход
            if (lastY + tileSize == height) break
            y += step
        }

        return tiles
    }

    /**
     * Преобразует список фрагментов (Tile) в список входных тензоров PyTorch.
     * Включает нормализацию 0-255 -> 0.0-1.0 (аналог to_0_1_format_img).
     * @param tiles Список фрагментов, полученный из splitImageIntoTiles.
     * @return Список готовых к инференсу тензоров.
     */
    fun getTensorsForInference(tiles: List<Tile>): List<Tensor> {
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