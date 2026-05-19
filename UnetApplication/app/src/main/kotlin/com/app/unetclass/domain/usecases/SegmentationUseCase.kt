package com.app.unetclass.domain.usecases

import android.graphics.Bitmap
import android.os.Debug
import ru.unet_app.datastore.data.PredictionHistoryItem
import com.app.model.ImageData
import com.app.model.ResultState
import ru.unet_app.unet.di.LiteRTModel
import ru.unet_app.unet.di.PytorchModel
import ru.unet_app.unet.domain.UnetModel
import ru.unet_app.unet.models.SegmentationResult
import ru.unet_app.unet.domain.usecases.SplitImageIntoTilesUseCase
import javax.inject.Inject
import javax.inject.Provider

class SegmentationUseCase @Inject constructor(
    @LiteRTModel private val liteRTModelProvider: Provider<UnetModel>,
    @PytorchModel private val pytorchModelProvider: Provider<UnetModel>,
    private val imageToTiles: SplitImageIntoTilesUseCase,
) {
    private var currentModel: UnetModel? = null
    private var isUsingPyTorch: Boolean? = null

    operator fun invoke(
        bitmap: Bitmap,
        usePyTorch: Boolean = false,
        onHistoryUpdate: (List<PredictionHistoryItem>) -> Unit
    ): ResultState<SegmentationResult, String> {
        // Проверяем, нужно ли сменить или инициализировать модель
        if (currentModel == null || isUsingPyTorch != usePyTorch) {
            currentModel?.close()
            currentModel = if (usePyTorch) pytorchModelProvider.get() else liteRTModelProvider.get()
            isUsingPyTorch = usePyTorch
        }

        val model = currentModel!!

        val tiles = imageToTiles(bitmap)

        val (javaMemoryBefore, nativeMemoryBefore) = ramUsage()

        val startTime = System.currentTimeMillis()
        val result = model.predict(
            ImageData(bitmap.width, bitmap.height, tiles)
        )
        val totalTime = System.currentTimeMillis() - startTime

        val (javaMemoryAfter, nativeMemoryAfter) = ramUsage()
        val javaUsed = (javaMemoryAfter - javaMemoryBefore).coerceAtLeast(0L)
        val nativeUsed = (nativeMemoryAfter - nativeMemoryBefore).coerceAtLeast(0L)
        val totalMemoryBytes = javaUsed + nativeUsed

        return when(result) {
            is ResultState.Error -> ResultState.Error(result.error)
            is ResultState.Success -> ResultState.Success(
                SegmentationResult(
                    labeledData = result.data,
                    totalTimeMs = totalTime,
                    memoryUsageBytes = totalMemoryBytes
                )
            )
        }
    }

    /**
     * Измеряет текущее потребление оперативной памяти.
     *
     * @return Pair, где:
     * - first (Long): Занятая память в JVM Heap (в байтах).
     *   Здесь хранятся объекты Kotlin/Java, такие как массивы FloatArray в [TFLiteModel.kt],
     *   объекты Tile и Bitmap.
     *
     * - second (Long): Занятая память в Native Heap (в байтах).
     *   Это критически важный показатель для ML, так как библиотеки LiteRT (TFLite)
     *   и PyTorch Mobile выделяют основную память под веса моделей и тензоры
     *   на уровне C++, минуя Garbage Collector.
     */
    private fun ramUsage(): Pair<Long, Long> {
        val runtime = Runtime.getRuntime()
        val javaMemory = runtime.totalMemory() - runtime.freeMemory()
        val nativeMemory = Debug.getNativeHeapAllocatedSize()

        return Pair(javaMemory, nativeMemory)
    }
}