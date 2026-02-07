package com.app.unetclass

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.viewModels
import com.app.unetclass.data.TimeMeasurementService
import com.app.unetclass.databinding.ActivityMainBinding
import androidx.core.graphics.createBitmap
import com.app.unetclass.domain.ITimeMeasurementUseCase
import com.app.unetclass.features.detail.DetailActivity
import com.app.unetclass.presentation.HistoryAdapter
import com.app.unetclass.presentation.SegmentationPresenter
import com.app.unetclass.features.transparency.TransparencySettingsActivity
import com.app.unetclass.presentation.viewmodel.MainViewModel
import com.app.unetclass.presentation.viewmodel.MainViewModelFactory
import com.app.unetclass.utils.UnetModel

class MainActivity : AppCompatActivity() {
    private lateinit var model: UnetModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var presenter: SegmentationPresenter
    private lateinit var timeMeasurementService: ITimeMeasurementUseCase
    private lateinit var historyAdapter: HistoryAdapter

    // Получаем ViewModel с использованием фабрики
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(application, model)
    }

    // Современный способ получения результата из другого Activity
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
            val grayscaleBitmap = toGrayscale(originalBitmap)
            viewModel.setSelectedImage(grayscaleBitmap, it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        model = UnetModel(applicationContext)

        // Инициализация новых компонентов
        timeMeasurementService = TimeMeasurementService()
        presenter = SegmentationPresenter(
            model,
            timeMeasurementService
        )

        // Инициализация RecyclerView и адаптера
        initRecyclerView()

        // Инициализация при первом запуске
        binding.predictBtn.isEnabled = false // Кнопка "Predict" отключена по умолчанию
        binding.moreInfoButton.isEnabled = false

        // Восстановление состояния больше не требуется,
        // так как данные теперь управляются через ViewModel

        // Подписка на LiveData из ViewModel
        observeViewModel()

        binding.selectImageBtn.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.predictBtn.setOnClickListener {
            viewModel.bitmap.value?.let { bitmap ->
                viewModel.buttonsStateManager.disableButtons()
                viewModel.runSegmentation(bitmap)
            }
        }

        binding.settingsBtn.setOnClickListener {
            // Открываем TransparencySettingsActivity с текущим изображением
            val intent = Intent(this, TransparencySettingsActivity::class.java).apply {
                // Передаем путь к последнему сохраненному результату
                val lastSavedPath = model.getLastSavedPath()
                if (lastSavedPath.isNotEmpty()) {
                    putExtra("result_path", lastSavedPath)

                    // Формируем пути к маскам классов
                    val classMaskPaths = mutableListOf<String>()
                    for (className in com.app.unetclass.utils.ClassNames.NAMES) {
                        val maskPath = "$lastSavedPath/${className}_prediction.png"
                        classMaskPaths.add(maskPath)
                    }
                    putStringArrayListExtra("class_masks_paths", ArrayList(classMaskPaths))
                } else {
                    Log.e(TAG, "No segmentation results available. Please run segmentation first.")
                    return@setOnClickListener
                }
            }
            startActivity(intent)
        }

        binding.moreInfoButton.setOnClickListener {
            viewModel.selectedHistoryItem.value?.let { historyItem ->
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("prediction_path", historyItem.outputPath)
                }
                startActivity(intent)
            } ?: run {
                Toast.makeText(
                    this,
                    "Please select a prediction first",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initRecyclerView() {
        historyAdapter = HistoryAdapter(onItemClick = { historyItem ->
            // Обработка клика по элементу истории через ViewModel
            viewModel.loadHistoryImage(historyItem)
        })

        binding.historyRecyclerView.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    // Метод displayImageFromHistory больше не нужен,
    // так как загрузка изображений из истории теперь происходит через ViewModel
    // При необходимости этот функционал можно вызвать через viewModel.loadHistoryImage(historyItem)

    private fun loadBitmapFromPath(imagePath: String): Bitmap? {
        return try {
            val file = java.io.File(imagePath)
            if (file.exists()) {
                android.graphics.BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
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

    // Методы для работы с временными файлами больше не требуются,
    // так как состояние теперь управляется через ViewModel

    private fun observeViewModel() {
        // Подписка на изменения bitmap
        viewModel.bitmap.observe(this) { bitmap ->
            binding.imageView.setImageBitmap(bitmap)
        }

        // Подписка на изменения URI изображения
        viewModel.selectedImageUri.observe(this) { uri ->
            // URI используется для передачи в другие компоненты при необходимости
        }

        // Подписка на изменения состояния кнопок
        viewModel.buttonsStateManager.selectImageButtonIsEnabled.observe(this) { isEnabled ->
            binding.selectImageBtn.isEnabled = isEnabled
        }
        viewModel.buttonsStateManager.predictButtonsIsEnabled.observe(this) { isEnabled ->
            binding.predictBtn.isEnabled = isEnabled
        }
        viewModel.buttonsStateManager.settingsButtonIsEnabled.observe(this) { isEnabled ->
            binding.settingsBtn.isEnabled = isEnabled
        }
        viewModel.buttonsStateManager.moreInfoButtonIsEnabled.observe(this) { isEnabled ->
            binding.moreInfoButton.isEnabled = isEnabled
        }

        // Подписка на список истории
        viewModel.historyItems.observe(this) { historyItems ->
            historyAdapter.updateItems(historyItems)
        }

        // Подписка на выбранный элемент истории
        viewModel.selectedHistoryItem.observe(this) { selectedHistoryItem ->
            // Обработка изменения выбранного элемента истории
            // Может потребоваться дополнительная логика
        }

        // Подписка на сообщения об ошибках
        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError() // Очищаем ошибку после показа
            }
        }

        // Подписка на результат сегментации
        viewModel.segmentationResult.observe(this) { resultBitmap ->
            resultBitmap?.let {
                binding.imageView.setImageBitmap(it)
                Toast.makeText(
                    this,
                    "Results saved successfully in Pictures/UnetClass/",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.clearSegmentationResultEvent()
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}