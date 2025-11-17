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
import com.app.unetclass.models.InferenceModel
import com.app.unetclass.databinding.ActivityMainBinding
import androidx.core.graphics.createBitmap
import androidx.lifecycle.lifecycleScope
import com.app.unetclass.utils.ResultState
import com.app.unetclass.utils.UnetModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var model: UnetModel
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
        model = UnetModel(applicationContext)

        binding.selectImageBtn.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.predictBtn.setOnClickListener {
            bitmap?.let {
                runSegmentation(it)
            }
        }
    }

    private fun runSegmentation(inputBitmap: Bitmap) {
        lockButtons()
        lifecycleScope.launch(Dispatchers.IO) {
            when(val res = model.startSegmentation(inputBitmap)) {
                is ResultState.Success -> {
                    withContext(Dispatchers.Main) {
                        binding.imageView.setImageBitmap(res.data)
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
                            "Error saving results",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun lockButtons() {
        binding.selectImageBtn.isEnabled = false
        binding.predictBtn.isEnabled = false
    }

    private fun unlockButtons() {
        binding.selectImageBtn.isEnabled = true
        binding.predictBtn.isEnabled = true
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
