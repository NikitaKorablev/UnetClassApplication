package ru.unet_app.unet.domain.usecases

import android.graphics.Bitmap
import ru.unet_app.model.Tile

class SplitImageIntoTilesUseCase {
    /**
     * Выполняет нарезку изображения на фрагменты с перекрытием.
     * Аналог логики нарезки в test_data.
     */
    operator fun invoke(inputBitmap: Bitmap): List<Tile> {
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
                if (currentX + TILE_SIZE > width) {
                    currentX = width - TILE_SIZE
                }

                // 2. Обработка нижнего края: сдвиг, если фрагмент выходит за границу
                if (currentY + TILE_SIZE > height) {
                    currentY = height - TILE_SIZE
                }

                // Обновляем lastY, чтобы использовать его для проверки выхода из внешнего цикла
                lastY = currentY

                // Вырезаем фрагмент (Bitmap.createBitmap безопасно работает с координатами)
                val tileBitmap = Bitmap.createBitmap(
                    inputBitmap,
                    currentX,
                    currentY,
                    TILE_SIZE,
                    TILE_SIZE
                )

                tiles.add(
                    Tile(
                        tileBitmap,
                        currentX,
                        currentY,
                        currentX + TILE_SIZE,
                        currentY + TILE_SIZE
                    )
                )

                // Шаг по горизонтали. Если достигли края, выход.
                if (currentX + TILE_SIZE == width) break
                x += STEP
            }

            // Шаг по вертикали. Если достигли края, выход.
            // Используем lastY (скорректированную координату), чтобы проверить выход
            if (lastY + TILE_SIZE == height) break
            y += STEP
        }

        return tiles
    }

    companion object {
        const val TILE_SIZE = 256
        const val STEP = TILE_SIZE - Tile.SHIFT*2
    }
}
