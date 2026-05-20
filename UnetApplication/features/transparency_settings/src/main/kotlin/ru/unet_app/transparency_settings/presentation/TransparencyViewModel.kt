package ru.unet_app.transparency_settings.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ru.unet_app.datastore.domain.repository.ImageRepository
import ru.unet_app.model.InferenceMetadata
import ru.unet_app.model.PredictedClasses
import ru.unet_app.transparency_settings.domain.repository.TransparencyImageProcRepository
import ru.unet_app.model.TransparencyState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.io.File
import javax.inject.Inject
import kotlin.collections.mapIndexed

@OptIn(FlowPreview::class)
@HiltViewModel
class TransparencyViewModel @Inject constructor(
    val imageProcessor: TransparencyImageProcRepository,
    val imageRepository: ImageRepository
) : ViewModel() {
    private lateinit var outputPath: String
    private lateinit var classMasks: Map<PredictedClasses, Bitmap>

    private val _transparencyState = MutableStateFlow(TransparencyState())
    val transparencyState: StateFlow<TransparencyState> = _transparencyState.asStateFlow()

    private val _metadata = MutableStateFlow<InferenceMetadata?>(null)
    val metadata: StateFlow<InferenceMetadata?> = _metadata.asStateFlow()

    init {
        _transparencyState
            .debounce(200)
            .onEach {  }
            .launchIn(viewModelScope)
    }

    fun setupData(
        bitmapPaths: ArrayList<String>?,
        bitmapArray: ArrayList<Bitmap>?,
        resultPath: String,
    ) {
        if (bitmapPaths != null) {
            // Загружаем изображения из файлов
            classMasks = bitmapPaths.mapIndexed { index, path ->
                val file = File(path)
                require(file.exists()) { "Файл изображения не найден: $path" }
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                PredictedClasses.entries[index] to bitmap
            }.toMap()

            outputPath = resultPath
        } else {
            // Если изображения не переданы как пути, получаем как Bitmap
            require(bitmapArray != null) { "Необходимо передать маски классов" }

            classMasks = bitmapArray.mapIndexed { index, bitmap ->
                PredictedClasses.entries[index] to bitmap
            }.toMap()

            outputPath = resultPath
        }

        // Загружаем метаданные
        _metadata.value = imageRepository.loadMetadata(File(resultPath))
    }

    fun updateTransparency(type: PredictedClasses, value: Int) {
        val floatValue = value / 100f
        _transparencyState.update { current ->
            when (type) {
                PredictedClasses.MITOCHONDRIA -> current.copy(mitochondria = floatValue)
                PredictedClasses.PSD -> current.copy(psd = floatValue)
                PredictedClasses.MITOCHONDRIAL_BOUNDARIES ->
                    current.copy(mitochondrialBoundaries = floatValue)
                PredictedClasses.VESICLES -> current.copy(vesicles = floatValue)
                PredictedClasses.AXON -> current.copy(axon = floatValue)
                PredictedClasses.BOUNDARIES -> current.copy(boundaries = floatValue)
            }
        }
    }

    fun getPreviewImage(): Bitmap {
//        return imageProcessor.overlayMasksWithTransparency(
//            classMasks, transparencyState.value
//        )
        return imageProcessor.unitedMask(
            classMasks, transparencyState.value
        )
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
