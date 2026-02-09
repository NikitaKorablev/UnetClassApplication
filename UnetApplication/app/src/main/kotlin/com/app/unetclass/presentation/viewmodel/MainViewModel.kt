package com.app.unetclass.presentation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.model.ResultState
import com.app.unet.utils.TimeMeasurementService
import com.app.unetclass.domain.usecases.SegmentationUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MainViewModel @Inject constructor(
    @param:ApplicationContext
    private val application: Application,
    private val startSegmentation: SegmentationUseCase

    ) : AndroidViewModel(application) {

    private val timeMeasurementService = TimeMeasurementService()
    private val presenter = SegmentationPresenter(segmentationUseCase, timeMeasurementService)

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

    /**
     * Устанавливает выбранное изображение
     */
    fun setSelectedImage(bitmap: Bitmap?, uri: Uri? = null) {
        _bitmap.value = bitmap
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
     * Очищает ошибку
     */
    fun clearError() {
        _errorMessage.value = null
    }

    companion object {
        const val TAG = "MainViewModel"
    }
}