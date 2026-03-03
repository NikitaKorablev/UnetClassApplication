package com.app.transparancy.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import com.app.datastore.domain.repository.ImageRepository
import com.app.model.TransparencyType
import com.app.transparancy.domain.repository.IImageOverlayProcessor
import com.app.transparancy.presentation.model.TransparencyState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import javax.inject.Inject
import kotlin.collections.mapIndexed

class TransparencyViewModel @Inject constructor(
    val imageProcessor: IImageOverlayProcessor,
    val imageRepository: ImageRepository
) : ViewModel() {
    lateinit var classMasks: Map<TransparencyType, Bitmap>
    lateinit var outputPath: String

    private val _state = MutableStateFlow(TransparencyState())
    val state: StateFlow<TransparencyState> = _state.asStateFlow()

    fun setupData(
        bitmapPaths: ArrayList<String>?,
        bitmapArray: ArrayList<Bitmap>?,
        resultPath: String,
    ) {
        val typeKeys = listOf(
            TransparencyType.MITOCHONDRIA,
            TransparencyType.PSD,
            TransparencyType.VESICLES,
            TransparencyType.AXON,
            TransparencyType.BOUNDARIES,
            TransparencyType.MITO_BOUNDARIES
        )


        if (bitmapPaths != null) {
            // Загружаем изображения из файлов
            classMasks = bitmapPaths.mapIndexed { index, path ->
                val file = File(path)
                require(file.exists()) { "Файл изображения не найден: $path" }
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                typeKeys[index] to bitmap
            }.toMap()

            outputPath = resultPath
        } else {
            // Если изображения не переданы как пути, получаем как Bitmap
            require(bitmapArray != null) { "Необходимо передать маски классов" }

            classMasks = bitmapArray.mapIndexed { index, bitmap ->
                typeKeys[index] to bitmap
            }.toMap()

            outputPath = resultPath
        }
    }

    fun updateTransparency(type: TransparencyType, value: Int) {
        val floatValue = value / 100f
        _state.update { current ->
            when (type) {
                TransparencyType.MITOCHONDRIA -> current.copy(mitochondria = floatValue)
                TransparencyType.PSD -> current.copy(psd = floatValue)
                TransparencyType.MITO_BOUNDARIES -> current.copy(mitochondrialBoundaries = floatValue)
                TransparencyType.VESICLES -> current.copy(vesicles = floatValue)
                TransparencyType.AXON -> current.copy(axon = floatValue)
                TransparencyType.BOUNDARIES -> current.copy(boundaries = floatValue)
            }
        }
    }

    fun getPreviewImage(): Bitmap {
        return imageProcessor.overlayMasksWithTransparency(classMasks, state.value)
    }

    fun savePreviewImage(): Boolean {
        val bitmap = getPreviewImage()

        // Генерируем уникальное имя файла
        val fileName = generateUniqueFileName()
        return imageRepository.saveImage(
            bitmap,
            File(outputPath),
            fileName
        )
    }


    private fun generateUniqueFileName(): String {
        var counter = 0
        var fileName: String

        do {
            fileName = "united_mask_${counter}.png"
            counter++
        } while (File(outputPath, fileName).exists() && counter < 10000) // Защита от бесконечного цикла

        return fileName
    }
}