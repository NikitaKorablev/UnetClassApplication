package com.app.unetclass

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.app.unetclass.data.ImageProcessor
import com.app.unetclass.data.ImageStitcher
import com.app.unetclass.data.InferenceModel
import com.app.unetclass.databinding.ActivityMainBinding
import androidx.core.graphics.createBitmap

class MainActivity : AppCompatActivity() {
    private lateinit var imageProcessor: ImageProcessor
    private lateinit var imageStitcher: ImageStitcher
    private lateinit var inferenceModel: InferenceModel

    private lateinit var binding: ActivityMainBinding

    private var bitmap: Bitmap? = null

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

        binding.selectImageBtn.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.predictBtn.setOnClickListener {
            bitmap?.let {
                runSegmentation(it)
            }
        }

        // Инициализация. Параметры TILE_SIZE=256, OVERLAP=128, NUM_CLASSES=6
        imageProcessor = ImageProcessor(256, 128)
        imageStitcher = ImageStitcher(128, 6)

        // Инициализация модели (загрузка из assets/model.ptl)
        // Убедитесь, что model.ptl находится в папке assets
        inferenceModel = InferenceModel(applicationContext)
    }

    private fun runSegmentation(inputBitmap: Bitmap) {
        // --- 1. Нарезка и подготовка тензоров (распил) ---
        val tilesList = imageProcessor.splitImageIntoTiles(inputBitmap)
        val inputTensors = imageProcessor.getTensorsForInference(tilesList)

        // --- 2. Инференс (predict) ---
        val outputTensors = inferenceModel.predictBatch(inputTensors)

        // --- 3. Сборка (сборка) и Постобработка ---
        val result = imageStitcher.stitchMasks(
            outputTensors,
            tilesList,
            inputBitmap.width,
            inputBitmap.height
        )

        // Отображаем финальную маску (как и раньше)
        binding.imageView.setImageBitmap(result.unitedMask)
        
        // Сохраняем все изображения (финальная маска + маски для каждого класса)
        val saveSuccessful = imageStitcher.saveResults(result, applicationContext)
        
        // Показываем сообщение пользователю о результате сохранения
        if (saveSuccessful) {
            Toast.makeText(this, "Results saved successfully in Pictures/UnetClass/", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Error saving results", Toast.LENGTH_SHORT).show()
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
}
