package ru.unet_app.inference_details.presentation.fullscreen

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import ru.unet_app.inference_details.R
import com.github.chrisbanes.photoview.PhotoView
import com.bumptech.glide.Glide

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
            Glide.with(this)
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