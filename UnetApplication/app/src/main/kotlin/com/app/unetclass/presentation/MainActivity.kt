package com.app.unetclass.presentation

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.domain.utils.Router
import com.app.unetclass.R
import com.app.unetclass.databinding.ActivityMainBinding
import ru.unet_app.inference_details.presentation.detail.DetailActivity
import com.app.unetclass.presentation.viewmodel.MainViewModel
import com.app.unetclass.utils.AppNavigationComponent
import com.app.unetclass.utils.MainActivityNav
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var router: Router

    private lateinit var binding: ActivityMainBinding
    private lateinit var historyAdapter: HistoryAdapter

    // Получаем ViewModel с использованием фабрики
    private val viewModel: MainViewModel by viewModels()

    // Современный способ получения результата из другого Activity
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
            viewModel.setSelectedImage(bitmap, it)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Storage permission is required to save results", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkStoragePermission()

        // Инициализация RecyclerView и адаптера
        initRecyclerView()

        // Инициализация при первом запуске
        binding.predictBtn.isEnabled = false // Кнопка "Predict" отключена по умолчанию
        binding.moreInfoButton.isEnabled = false

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
            (router as MainActivityNav).toTransparencySettings(
                this,
                viewModel.lastSavedPath
            )
        }

        binding.menuBtn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }

        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.model_tflite -> viewModel.setModelType(false)
                R.id.model_pytorch -> viewModel.setModelType(true)
            }
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            true
        }

        binding.moreInfoButton.setOnClickListener {
            viewModel.selectedHistoryItem.value?.let { historyItem ->
                (router as MainActivityNav).toInferenceDetails(
                    this, historyItem.outputPath
                )
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
            val file = File(imagePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

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

        // Подписка на тип модели для синхронизации меню
        viewModel.usePyTorch.observe(this) { usePyTorch ->
            val menu = binding.navigationView.menu
            if (usePyTorch) {
                menu.findItem(R.id.model_pytorch).isChecked = true
            } else {
                menu.findItem(R.id.model_tflite).isChecked = true
            }
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

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}