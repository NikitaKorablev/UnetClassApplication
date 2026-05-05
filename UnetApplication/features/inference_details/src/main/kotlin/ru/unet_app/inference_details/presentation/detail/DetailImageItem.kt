package ru.unet_app.inference_details.presentation.detail

data class DetailImageItem(
    val imagePath: String,
    val imageName: String,
    val className: String? = null
)