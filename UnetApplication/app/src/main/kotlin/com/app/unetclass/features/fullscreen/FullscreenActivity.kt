package com.app.unetclass.features.fullscreen

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.app.unetclass.R
import com.github.chrisbanes.photoview.PhotoView

class FullscreenActivity : AppCompatActivity() {

    private lateinit var fullscreenImageView: PhotoView
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)

        initViews()
        loadImage()
        setupBackButton()
    }

    private fun initViews() {
        fullscreenImageView = findViewById(R.id.fullscreenImageView)
        backButton = findViewById(R.id.backButton)
    }

    private fun loadImage() {
        val imagePath = intent.getStringExtra("image_path")
        if (imagePath != null) {
            // Загрузка изображения с помощью Glide
            com.bumptech.glide.Glide.with(this)
                .load(imagePath)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(fullscreenImageView)
        }
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            finish()
        }
    }
}