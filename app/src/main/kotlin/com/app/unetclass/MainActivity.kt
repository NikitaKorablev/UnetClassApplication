package com.app.unetclass

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.unetclass.data.ImageProcessor
import com.app.unetclass.data.ImageStitcher
import com.app.unetclass.data.TimeMeasurementService
import com.app.unetclass.databinding.ActivityMainBinding
import androidx.core.graphics.createBitmap
import androidx.lifecycle.lifecycleScope
import com.app.unetclass.domain.ISegmentationUseCase
import com.app.unetclass.domain.ITimeMeasurementUseCase
import com.app.unetclass.features.detail.DetailActivity
import com.app.unetclass.models.PredictionHistoryItem
import com.app.unetclass.presentation.HistoryAdapter
import com.app.unetclass.presentation.SegmentationPresenter
import com.app.unetclass.utils.ResultState
import com.app.unetclass.utils.UnetModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var model: UnetModel
    private lateinit var binding: ActivityMainBinding
    private var bitmap: Bitmap? = null
    private lateinit var presenter: SegmentationPresenter
    private lateinit var timeMeasurementService: ITimeMeasurementUseCase
    private lateinit var historyAdapter: HistoryAdapter
    private var selectedHistoryItem: PredictionHistoryItem? = null

    // Современный способ получения результата из другого Activity
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
            bitmap = toGrayscale(originalBitmap) // Конвертируем в оттенки серого
            binding.imageView.setImageBitmap(bitmap)
            binding.predictBtn.isEnabled = true // Активируем кнопку после выбора изображения
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.predictBtn.isEnabled = false // Кнопка "Predict" отключена по умолчанию
        binding.moreInfoButton.isEnabled = false
        model = UnetModel(applicationContext)

        // Инициализация новых компонентов
        timeMeasurementService = TimeMeasurementService()
        presenter = SegmentationPresenter(model, timeMeasurementService)

        // Инициализация RecyclerView и адаптера
        initRecyclerView()

        binding.selectImageBtn.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.predictBtn.setOnClickListener {
            bitmap?.let {
                runSegmentation(it)
            }
        }

        binding.moreInfoButton.setOnClickListener {
            selectedHistoryItem?.let { historyItem ->
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("prediction_path", historyItem.outputPath)
                }
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "Please select a prediction first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initRecyclerView() {
        historyAdapter = HistoryAdapter(onItemClick = { historyItem ->
            // Обработка клика по элементу истории - загрузка и отображение изображения
            selectedHistoryItem = historyItem
            displayImageFromHistory(historyItem)
        })

        binding.historyRecyclerView.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun displayImageFromHistory(historyItem: PredictionHistoryItem) {
        // Формируем путь к изображению из истории
        val imagePath = "${historyItem.outputPath}/united_mask.png"

        // Загружаем изображение и отображаем его в ImageView
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val bitmap = loadBitmapFromPath(imagePath)
                if (bitmap != null) {
                    withContext(Dispatchers.Main) {
                        binding.imageView.setImageBitmap(bitmap)
                        Toast.makeText(this@MainActivity, "History image loaded: ${historyItem.timestamp}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Failed to load image from: $imagePath", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error loading history image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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

    private fun runSegmentation(inputBitmap: Bitmap) {
        lockButtons()
        lifecycleScope.launch(Dispatchers.IO) {
            when(val res = presenter.startSegmentation(inputBitmap) { historyItems ->
                // Обновляем список истории
                lifecycleScope.launch(Dispatchers.Main) {
                    historyItems.forEach { item ->
                        historyAdapter.addItem(item)
                    }
                }
            }) {
                is ResultState.Success -> {
                    withContext(Dispatchers.Main) {
                        binding.imageView.setImageBitmap(res.data.bitmap)
                        unlockButtons()
                        Toast.makeText(
                            applicationContext,
                            "Results saved successfully in Pictures/UnetClass/",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                is ResultState.Error ->
                    withContext(Dispatchers.Main) {
                        unlockButtons()
                        Toast.makeText(
                            applicationContext,
                            "Error saving results: ${res.error}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun lockButtons() {
        binding.selectImageBtn.isEnabled = false
        binding.predictBtn.isEnabled = false
        binding.moreInfoButton.isEnabled = false
    }

    private fun unlockButtons() {
        binding.selectImageBtn.isEnabled = true
        binding.predictBtn.isEnabled = true
        binding.moreInfoButton.isEnabled = true
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
}