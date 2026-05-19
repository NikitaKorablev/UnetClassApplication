package ru.unet_app.unet.models.classes

import com.app.model.PredictedClasses

class Vesicles(
    override val label: Array<FloatArray> = emptyArray()
): BaseLabel() {
    override val type = PredictedClasses.VESICLES
}