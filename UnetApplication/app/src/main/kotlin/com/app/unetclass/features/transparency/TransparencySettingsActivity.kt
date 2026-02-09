package com.app.unetclass.features.transparency

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.app.unetclass.R
import com.app.unetclass.data.ImageSaver
import com.app.unetclass.data.TransparencyImageProcessor
import com.app.unetclass.domain.repository.IImageOverlayProcessor
import com.app.unetclass.models.TransparencyState
import java.io.File

class TransparencySettingsActivity : AppCompatActivity() {
    private lateinit var previewImageView: ImageView
    private lateinit var mitochondriaSeekBar: SeekBar
    private lateinit var psdSeekBar: SeekBar
    private lateinit var vesiclesSeekBar: SeekBar
    private lateinit var axonSeekBar: SeekBar
    private lateinit var boundariesSeekBar: SeekBar
    private lateinit var mitochondrialBoundariesSeekBar: SeekBar
    private lateinit var saveButton: Button

    private lateinit var classMasks: List<Bitmap>
    private lateinit var currentTransparencyState: TransparencyState
    private lateinit var imageSaver: ImageSaver
    private lateinit var imageProcessor: IImageOverlayProcessor
    private var outputPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transparency_settings)

        initViews()
        setupData()
        setupListeners()
        updatePreviewImage()
    }

    private fun initViews() {
        previewImageView = findViewById(R.id.previewImageView)
        mitochondriaSeekBar = findViewById(R.id.mitochondriaSeekBar)
        psdSeekBar = findViewById(R.id.psdSeekBar)
        vesiclesSeekBar = findViewById(R.id.vesiclesSeekBar)
        axonSeekBar = findViewById(R.id.axonSeekBar)
        boundariesSeekBar = findViewById(R.id.boundariesSeekBar)
        mitochondrialBoundariesSeekBar = findViewById(R.id.mitochondrialBoundariesSeekBar)
        saveButton = findViewById(R.id.saveButton)
    }

    private fun setupData() {
        // Получаем изображения из intent
        val bitmapPaths = intent.getStringArrayListExtra("class_masks_paths")
        val resultPath = intent.getStringExtra("result_path")
        
        if (bitmapPaths != null && resultPath != null) {
            // Загружаем изображения из файлов
            classMasks = bitmapPaths.map { path ->
                val file = File(path)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else {
                    throw IllegalArgumentException("Файл изображения не найден: $path")
                }
            }
            
            outputPath = resultPath
        } else {
            // Если изображения не переданы как пути, получаем как Bitmap
            val bitmapArray = intent.getParcelableArrayListExtra<Bitmap>("class_masks")
            if (bitmapArray != null) {
                classMasks = bitmapArray
            } else {
                throw IllegalArgumentException("Необходимо передать маски классов")
            }
            
            outputPath = intent.getStringExtra("result_path") ?: ""
        }

        currentTransparencyState = TransparencyState(TransparencyState.getDefaultValues())
        imageProcessor = TransparencyImageProcessor()
        
        // Устанавливаем начальные значения ползунков
        setSeekBarValues(currentTransparencyState.transparencyValues)
    }

    private fun setupListeners() {
        val onSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateTransparencyValues()
                    updatePreviewImage()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        mitochondriaSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener)
        psdSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener)
        vesiclesSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener)
        axonSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener)
        boundariesSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener)
        mitochondrialBoundariesSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener)

        saveButton.setOnClickListener {
            savePreviewImage()
        }
    }

    private fun setSeekBarValues(values: List<Float>) {
        mitochondriaSeekBar.progress = (values[0] * 100).toInt()
        psdSeekBar.progress = (values[1] * 100).toInt()
        vesiclesSeekBar.progress = (values[2] * 100).toInt()
        axonSeekBar.progress = (values[3] * 100).toInt()
        boundariesSeekBar.progress = (values[4] * 100).toInt()
        mitochondrialBoundariesSeekBar.progress = (values[5] * 100).toInt()
    }

    private fun updateTransparencyValues() {
        val values = listOf(
            mitochondriaSeekBar.progress / 100f,
            psdSeekBar.progress / 100f,
            vesiclesSeekBar.progress / 100f,
            axonSeekBar.progress / 100f,
            boundariesSeekBar.progress / 100f,
            mitochondrialBoundariesSeekBar.progress / 100f
        )
        currentTransparencyState = currentTransparencyState.updateMultipleTransparency(values)
    }

    private fun updatePreviewImage() {
        val transparencyValues = currentTransparencyState.transparencyValues
        val resultBitmap = imageProcessor.overlayMasksWithTransparency(classMasks, transparencyValues)
        previewImageView.setImageBitmap(resultBitmap)
    }

    private fun savePreviewImage() {
        val transparencyValues = currentTransparencyState.transparencyValues
        val resultBitmap = imageProcessor.overlayMasksWithTransparency(classMasks, transparencyValues)

        // Генерируем уникальное имя файла
        val fileName = generateUniqueFileName()
        val saved = ImageSaver.saveImage(resultBitmap, File(outputPath), fileName)

        if (saved) {
            // Закрываем активность после сохранения
            finish()
        }
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