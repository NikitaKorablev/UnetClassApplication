package com.app.unetclass.features.detail

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.unetclass.R
import com.app.unetclass.features.fullscreen.FullscreenActivity
import java.io.File

class DetailActivity : AppCompatActivity() {

    private lateinit var imagesRecyclerView: RecyclerView
    private lateinit var backButton: ImageButton
    private lateinit var titleTextView: androidx.appcompat.widget.AppCompatTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        initViews()
        setupRecyclerView()
        setupBackButton()
    }

    private fun initViews() {
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView)
        backButton = findViewById(R.id.backButton)
        titleTextView = findViewById(R.id.titleTextView)
    }

    private fun setupRecyclerView() {
        val predictionPath = intent.getStringExtra("prediction_path")
        if (predictionPath != null) {
            val imageFiles = getImageFilesFromDirectory(predictionPath)
            val detailItems = imageFiles.map { file ->
                DetailImageItem(
                    imagePath = file.absolutePath,
                    imageName = file.name
                )
            }

            val adapter = DetailAdapter(detailItems) { detailImageItem ->
                // Открытие полноэкранного просмотра при клике на изображение
                val intent = Intent(this, FullscreenActivity::class.java).apply {
                    putExtra("image_path", detailImageItem.imagePath)
                    putExtra("image_name", detailImageItem.imageName)
                }
                startActivity(intent)
            }

            imagesRecyclerView.apply {
                this.adapter = adapter
                layoutManager = GridLayoutManager(this@DetailActivity, 2) // 2 колонки
            }
        }
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun getImageFilesFromDirectory(directoryPath: String): List<File> {
        val directory = File(directoryPath)
        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }

        return directory.listFiles { file ->
            file.isFile && isImageFile(file)
        }?.sortedBy { it.name } ?: emptyList()
    }

    private fun isImageFile(file: File): Boolean {
        val name = file.name.lowercase()
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")
    }
}