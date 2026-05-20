package ru.unet_app.unetclass.utils

import android.content.Context
import ru.unet_app.domain.utils.Router

interface MainActivityNav: Router {
    fun toTransparencySettings(context: Context, lastSavedPath: String)
    fun toInferenceDetails(context: Context, outputPath: String)
}
