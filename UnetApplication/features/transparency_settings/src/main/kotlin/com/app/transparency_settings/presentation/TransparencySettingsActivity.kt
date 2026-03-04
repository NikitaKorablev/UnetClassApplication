package com.app.transparency_settings.presentation

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.model.TransparencyType
import com.app.transparency_settings.databinding.ActivityTransparencySettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    }

    private fun setupSeekBars() {
        val seekBarMap = mapOf(
            binding.mitochondriaSeekBar to TransparencyType.MITOCHONDRIA,
            binding.psdSeekBar to TransparencyType.PSD,
            binding.vesiclesSeekBar to TransparencyType.VESICLES,
            binding.axonSeekBar to TransparencyType.AXON,
            binding.boundariesSeekBar to TransparencyType.BOUNDARIES,
            binding.mitochondrialBoundariesSeekBar to TransparencyType.MITO_BOUNDARIES,
        )

        seekBarMap.forEach { (seekBar, type) ->
            seekBar.onProgressChanged { value ->
                viewModel.updateTransparency(type, value)
//                updatePreviewImage()
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
            viewModel.state.collect {
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