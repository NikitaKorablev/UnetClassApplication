package com.app.unetclass.presentation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.graphics.createBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.datastore.data.PredictionHistoryItem
import com.app.model.ResultState
import com.app.unetclass.domain.usecases.SegmentationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val startSegmentation: SegmentationUseCase,
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
        viewModelScope.launch(Dispatchers.Default) {
            try {
                Log.i(TAG, "Segmentation started")
                val result = startSegmentation(inputBitmap) { historyItems ->
                    // Обновляем список истории
                    val currentItems = _historyItems.value ?: emptyList()
                    _historyItems.value = historyItems + currentItems
                }

                withContext(Dispatchers.Main) {
                    when (result) {
                        is ResultState.Success -> {
                            Log.i(TAG, "Segmentation success")
                            _lastSavedPath = result.data.outputPath
                            _segmentationResult.value = result.data.bitmap
                            _bitmap.value = result.data.bitmap
                            _errorMessage.value = null
                        }
                        is ResultState.Error -> {
                            Log.e(TAG, "Segmentation error")
                            _errorMessage.value = result.error
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Segmentation error")
                    _errorMessage.value = e.message
                }
            } finally {
                segmentationIsStarted = false
                withContext(Dispatchers.Main) {
                    buttonsStateManager.enableButtons()
                }
//                _isButtonsEnabled.value = true
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

    /**
     * Вспомогательная функция для загрузки bitmap из пути
     */
    private fun loadBitmapFromPath(imagePath: String): Bitmap? {
        return try {
            val file = java.io.File(imagePath)
            if (file.exists()) {
                android.graphics.BitmapFactory.decodeFile(file.absolutePath)
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