package com.app.unet.data.unetmodels

import android.content.Context
import android.util.Log
import com.app.model.ImageData
import com.app.model.PredictedClasses
import com.app.model.ResultState
import com.app.model.Tile
import com.app.unet.data.LabelFactory
import com.app.unet.data.Utils
import com.app.unet.domain.UnetModel
import com.app.unet.models.LabeledData
import com.app.unet.models.classes.Axon
import com.app.unet.models.classes.Boundaries
import com.app.unet.models.classes.Mitochondria
import com.app.unet.models.classes.MitochondriaBoundaries
import com.app.unet.models.classes.PSD
import com.app.unet.models.classes.Vesicles
import dagger.hilt.android.qualifiers.ApplicationContext
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import javax.inject.Inject
import kotlin.math.min

class PyTorchModel @Inject constructor(
    val context: Context
): UnetModel {
    private val module: Module
    init {
        val modelPath = Utils.assetFilePath(context, MODEL_ASSET_NAME)
        module = Module.load(modelPath)

        Log.d(TAG, "PyTorch Mobile Model loaded successfully from $MODEL_ASSET_NAME")
    }

    override fun predict(inputImageData: ImageData): ResultState<LabeledData, String> {
        try {
            // --- 1. Нарезка и подготовка тензоров (распил) ---
            val inputTensors = toPyTorchTensors(inputImageData.tiles)

            // --- 2. Инференс (predict) ---
            val outputTensors = predictBatch(inputTensors)

            if (outputTensors.size != inputImageData.tiles.size) throw IllegalArgumentException(
                "Количество тензоров должно совпадать с количеством фрагментов."
            )

            // --- 3. Сборка (сборка) и Постобработка ---
            val result = labelData(outputTensors, inputImageData)
            return ResultState.Success(result)
        } catch (err: Exception) {
            err.printStackTrace()
            return ResultState.Error(err.message.toString())
        }
    }

    override fun close() {
        module.destroy()
        Log.d(TAG, "PyTorch model destroyed.")
    }

    /**
     * Выполняет предсказание для батча тензоров.
     * Аналог model_pipeliner.predict(img_generator)
     */
    private fun predictBatch(tensors: List<Tensor>): List<Tensor> {
        // PyTorch Mobile может принимать батч, если сконкатенировать тензоры.

        return tensors.map { tensor ->
            // IValue.from(tensor) создает аргумент для forward()
            // .output.toTensor() извлекает тензор из результата
            module.forward(IValue.from(tensor)).toTensor()
        }
    }

    /**
     * Преобразует список фрагментов (Tile) в список входных тензоров PyTorch.
     * Включает нормализацию 0-255 -> 0.0-1.0 (аналог to_0_1_format_img).
     * @param tiles Список фрагментов, полученный из splitImageIntoTiles.
     * @return Список готовых к инференсу тензоров.
     */
    private fun toPyTorchTensors(tiles: List<Tile>): List<Tensor> {
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
                val grayValue =
                    (pixel shr 16) and 0xFF // Берем R канал, но в градации серого все каналы одинаковы
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

    /**
     * Собирает предсказанные маски в одно полноразмерное изображение и маски для каждого класса.
     * Аналог glit_image.
     *
     * @param tensors Список выходных тензоров (результатов инференса).
     * @param inputImageData Список объектов Tile, содержащих координаты нарезки.
     * @return Результат соединения: финальная маска и маски для каждого класса.
     */
    private fun labelData(
        tensors: List<Tensor>,
        inputImageData: ImageData
    ): LabeledData {
        // Инициализация полноразмерного выходного массива (float32)
        // Размер: [H, W, numClasses]
        val finalMaskArray = Array(PredictedClasses.NUM_CLASSES) {
            Array(inputImageData.height) {
                FloatArray(inputImageData.width)
            }
        }

        // Обход всех фрагментов и их результатов
        for (i in tensors.indices) {
            val tileInfo = inputImageData.tiles[i]
            stitchTile(tensors[i].dataAsFloatArray, tileInfo, finalMaskArray)
        }

        return LabeledData(
            mitochondria = LabelFactory.getLabel(Mitochondria().type.className, finalMaskArray[0]) as Mitochondria,
            PSD = LabelFactory.getLabel(PSD().type.className, finalMaskArray[1]) as PSD,
            vesicles = LabelFactory.getLabel(Vesicles().type.className, finalMaskArray[2]) as Vesicles,
            axon = LabelFactory.getLabel(Axon().type.className, finalMaskArray[3]) as Axon,
            boundaries = LabelFactory.getLabel(Boundaries().type.className, finalMaskArray[4]) as Boundaries,
            mitochondriaBoundaries = LabelFactory.getLabel(MitochondriaBoundaries().type.className, finalMaskArray[5]) as MitochondriaBoundaries,
        )

        // --- 2. Постобработка (Аналог to_0_255_format_img) ---
//        val unitedMask = createUnitedMask(finalMaskArray, originalWidth, originalHeight)
//        return createClassMasks(finalMaskArray, inputImageData.width, inputImageData.height)
    }

//    private fun stitchTile(
//        outputData: FloatArray,
//        tileInfo: Tile, // или как называется объект в inputImageData.tiles
//        finalMaskArray: Array<Array<FloatArray>>
//    ) {
//        val tileSize = Tile.SIZE // H или W (256)
//
//        val outStartX = tileInfo.startX
//        val outStartY = tileInfo.startY
//        val outEndX = tileInfo.endX
//        val outEndY = tileInfo.endY
//
//        // Внутренние границы для уникальной области (получаем [64:192])
//        val uniqueStart = Tile.SHIFT
//
//        val tileYRange = uniqueStart until min(tileSize, outEndY - outStartY) - Tile.SHIFT
//        val tileXRange = uniqueStart until min(tileSize, outEndX - outStartX) - Tile.SHIFT
//
//        for (imgType in 0 until PredictedClasses.NUM_CLASSES) {
//            for (ty in tileYRange) {
//                val outY = outStartY + ty
//                for (tx in tileXRange) {
//                    val outX = outStartX + tx
//
//                    val index = imgType * tileSize * tileSize + ty * tileSize + tx
//                    finalMaskArray[imgType][outY][outX] = outputData[index]
//                }
//            }
//        }
//    }

    companion object {
        const val TAG = "PyTorchModel"
        const val MODEL_ASSET_NAME = "traced_model.pt"
    }
}