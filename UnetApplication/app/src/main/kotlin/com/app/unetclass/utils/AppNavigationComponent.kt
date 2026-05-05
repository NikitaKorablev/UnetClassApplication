package com.app.unetclass.utils

import android.content.Context
import android.content.Intent
import com.app.model.ClassNames
import ru.unet_app.transparency_settings.presentation.TransparencySettingsActivity
import ru.unet_app.inference_details.presentation.detail.DetailActivity

class AppNavigationComponent: MainActivityNav {
    override fun toTransparencySettings(context: Context, lastSavedPath: String) {
        val intent = Intent(context, TransparencySettingsActivity::class.java)
        .apply{
            // Передаем путь к последнему сохраненному результату
            require(lastSavedPath.isNotEmpty()) {
                "No segmentation results available. Please run segmentation first."
            }

            putExtra("result_path", lastSavedPath)
            // Формируем пути к маскам классов
            val classMaskPaths = mutableListOf<String>()
            for (className in ClassNames.NAMES) {
                val maskPath = "$lastSavedPath/${className}_prediction.png"
                classMaskPaths.add(maskPath)
            }
            putStringArrayListExtra("class_masks_paths", ArrayList(classMaskPaths))
        }

        context.startActivity(intent)
    }

    override fun toInferenceDetails(context: Context, outputPath: String) {
        val intent = Intent(context, DetailActivity::class.java).apply {
            putExtra("prediction_path", outputPath)
        }
        context.startActivity(intent)
    }
}