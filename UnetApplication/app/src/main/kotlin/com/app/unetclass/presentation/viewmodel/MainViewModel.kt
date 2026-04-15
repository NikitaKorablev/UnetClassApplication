package com.app.unetclass.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.graphics.createBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.datastore.data.PredictionHistoryItem
import com.app.model.ResultState
import com.app.unet.models.SegmentationResult
import com.app.unetclass.domain.usecases.SaveImageStitcherUseCase
import com.app.unetclass.domain.usecases.SegmentationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val startSegmentation: SegmentationUseCase,
    private val saveImageStitcher: SaveImageStitcherUseCase
): ViewModel() {
    // LiveData для изображения
    private val _bitmap = MutableLiveData<Bitmap?>()
    val bitmap: LiveData<Bitmap?> = _bitmap

    // LiveData для URI выбранного изображения
    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    // LiveData для списка истории
    private val _historyItems = MutableLiveData<List<PredictionHistoryItem>>(emptyList())
    val historyItems: LiveData<List<PredictionHistoryItem>> = _historyItems

    // LiveData для выбранного элемента истории
    private val _selectedHistoryItem = MutableLiveData<PredictionHistoryItem?>()
    val selectedHistoryItem: LiveData<PredictionHistoryItem?> = _selectedHistoryItem

    val buttonsStateManager = ButtonsStateManager()

    // LiveData для сообщений об ошибках
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // LiveData для результата сегментации
    private val _segmentationResult = MutableLiveData<Bitmap?>()
    val segmentationResult: LiveData<Bitmap?> = _segmentationResult

    private var segmentationIsStarted = false
    private var _lastSavedPath = ""
    val lastSavedPath: String
        get() = _lastSavedPath

    /**
     * Устанавливает выбранное изображение
     */
    fun setSelectedImage(bitmap: Bitmap, uri: Uri? = null) {
        _bitmap.value = toGrayscale(bitmap)
        _selectedImageUri.value = uri
        buttonsStateManager.imageSelected()
    }

    /**
     * Выполняет сегментацию изображения
     */
    fun runSegmentation(inputBitmap: Bitmap) {
        if (segmentationIsStarted) return // Предотвращение повторного запуска

        segmentationIsStarted = true
        viewModelScope.launch(
            Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
                throwable.printStackTrace()
                Log.e(TAG, "Segmentation error")
                _errorMessage.postValue(throwable.message)
                segmentationIsStarted = false
                buttonsStateManager.enableButtons()
            }
        ) {
            Log.i(TAG, "Segmentation started")
            val result = startSegmentation(inputBitmap) { historyItems ->
                // Обновляем список истории
                val currentItems = _historyItems.value ?: emptyList()
                _historyItems.value = historyItems + currentItems
            }

            when(result) {
                is ResultState.Success -> {
                    Log.i(TAG, "Segmentation success")
                    val memoryInMB = result.data.memoryUsageBytes / (1024 * 1024)
                    Log.d(TAG, "Memory usage: ${memoryInMB}Мб")

                    val unitedMask = result.data.labeledData.unitedMask()
                    val outputPath = saveImageStitcher(
                        unitedMask,
                        result.data.labeledData.labels.map { it.getMask().bitmap }
                    )
                    newHistoryItem(outputPath, result.data)

                    withContext(Dispatchers.Main) {
                        _lastSavedPath = outputPath
                        _segmentationResult.value = unitedMask
                        _bitmap.value = unitedMask
                        _errorMessage.value = null

                        segmentationIsStarted = false
                        buttonsStateManager.enableButtons()
                    }
                }
                is ResultState.Error -> throw Exception(result.error)
            }
        }
    }

    fun clearSegmentationResultEvent() {
        _segmentationResult.value = null
    }

    /**
     * Устанавливает выбранный элемент истории
     */
    fun setSelectedHistoryItem(historyItem: PredictionHistoryItem?) {
        _selectedHistoryItem.value = historyItem
    }

    /**
     * Загружает изображение из истории
     */
    fun loadHistoryImage(historyItem: PredictionHistoryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // В реальной реализации здесь будет логика загрузки изображения из файла
                // Пока просто устанавливаем outputPath как URI
                val imagePath = "${historyItem.outputPath}/united_mask.png"
                val bitmap = loadBitmapFromPath(imagePath)
                
                withContext(Dispatchers.Main) {
                    _bitmap.value = bitmap
                    _selectedHistoryItem.value = historyItem
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading history image: ${e.message}"
            }
        }
    }

    private suspend fun newHistoryItem(outputPath: String, data: SegmentationResult)
    = withContext(Dispatchers.Main) {
        val historyItem = PredictionHistoryItem(
            timestamp = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(Date()),
            executionTime = data.totalTimeMs,
            outputPath = outputPath,
            imageWidth = data.labeledData.width,
            imageHeight = data.labeledData.height
        )

        val currentItems = _historyItems.value ?: emptyList()
        _historyItems.value = listOf(historyItem) + currentItems
    }

    /**
     * Вспомогательная функция для загрузки bitmap из пути
     */
    private fun loadBitmapFromPath(imagePath: String): Bitmap? {
        return try {
            val file = File(imagePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Конвертирует цветное изображение в оттенки серого.
     */
    private fun toGrayscale(bmpOriginal: Bitmap): Bitmap {
        val width = bmpOriginal.width
        val height = bmpOriginal.height

        // Создаем новое изображение в формате RGB_565 (или ARGB_8888, в зависимости от нужд)
        val grayscaleBitmap = createBitmap(width, height)

        val pixels = IntArray(width * height)
        bmpOriginal.getPixels(pixels, 0, width, 0, 0, width, height)

        // Преобразовать RGB в градации серого
        for (i in pixels.indices) {
            val r = (pixels[i] shr 16) and 0xFF
            val g = (pixels[i] shr 8) and 0xFF
            val b = pixels[i] and 0xFF
            val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            pixels[i] = (gray shl 16) or (gray shl 8) or gray or (0xFF shl 24) // ARGB
        }

        grayscaleBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return grayscaleBitmap
    }

    /**
     * Очищает ошибку
     */
    fun clearError() {
        _errorMessage.value = null
    }

    companion object {
        const val TAG = "MainViewModel"
    }
}