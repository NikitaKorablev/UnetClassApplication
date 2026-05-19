package ru.unet_app.transparency_settings.presentation

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.model.PredictedClasses
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.unet_app.transparency_settings.databinding.ActivityTransparencySettingsBinding

@AndroidEntryPoint
class TransparencySettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransparencySettingsBinding
    private val viewModel: TransparencyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransparencySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSeekBars()
        setupData()
        setupSaveButton()
        subscribeOnPreviewImage()
        observeMetadata()
    }

    private fun setupSeekBars() {
        val baseState = viewModel.transparencyState.value
        binding.mitochondriaSeekBar.progress = (baseState.mitochondria * 100).toInt()
        binding.psdSeekBar.progress = (baseState.psd * 100).toInt()
        binding.vesiclesSeekBar.progress = (baseState.vesicles * 100).toInt()
        binding.axonSeekBar.progress = (baseState.axon * 100).toInt()
        binding.boundariesSeekBar.progress = (baseState.boundaries * 100).toInt()
        binding.mitochondrialBoundariesSeekBar.progress = (baseState.mitochondrialBoundaries * 100).toInt()

        val seekBarMap = mapOf(
            binding.mitochondriaSeekBar to PredictedClasses.MITOCHONDRIA,
            binding.psdSeekBar to PredictedClasses.PSD,
            binding.vesiclesSeekBar to PredictedClasses.VESICLES,
            binding.axonSeekBar to PredictedClasses.AXON,
            binding.boundariesSeekBar to PredictedClasses.BOUNDARIES,
            binding.mitochondrialBoundariesSeekBar to PredictedClasses.MITOCHONDRIAL_BOUNDARIES,
        )

        seekBarMap.forEach { (seekBar, type) ->
            seekBar.onProgressChanged { value ->
                viewModel.updateTransparency(type, value)
            }
        }
    }

    private fun setupSaveButton() {
        lifecycleScope.launch {
            binding.saveButton.setOnClickListener {
                savePreviewImage()
            }
        }
    }

    private fun setupData() {
        val bitmapPaths = intent.getStringArrayListExtra("class_masks_paths")
        val bitmapArray = intent.getParcelableArrayListExtra<Bitmap>("class_masks")
        val resultPath = intent.getStringExtra("result_path")

        require(resultPath != null) { "Не найден путь для сохранения изображений" }

        lifecycleScope.launch {
            viewModel.setupData(bitmapPaths, bitmapArray, resultPath)
        }
    }

    private fun subscribeOnPreviewImage() {
        lifecycleScope.launch(Dispatchers.Default) {
            viewModel.transparencyState.collect {
                val preview = viewModel.getPreviewImage()

                withContext(Dispatchers.Main) {
                    binding.previewImageView.setImageBitmap(preview)
                }
            }
        }
    }

    private fun savePreviewImage() {
        val previewSaved = viewModel.savePreviewImage()
        if (previewSaved) finish()
    }

    private fun observeMetadata() {
        lifecycleScope.launch {
            viewModel.metadata.collect { metadata ->
                metadata?.let {
                    binding.resolutionText.text = "Разрешение: ${it.width} x ${it.height}"
                    binding.executionTimeText.text = "Время выполнения: ${it.executionTimeMs} мс"
                    val memoryMB = it.memoryUsageBytes / (1024 * 1024)
                    binding.memoryUsageText.text = "Использование памяти: $memoryMB МБ"
                }
            }
        }
    }

    inline fun SeekBar.onProgressChanged(crossinline action: (Int) -> Unit) {
        this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) action(progress)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }
}