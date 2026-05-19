package ru.unet_app.unet.models.classes

import com.app.model.PredictedClasses

class Boundaries(
    override val label: Array<FloatArray> = emptyArray()
): BaseLabel() {
    override val type = PredictedClasses.BOUNDARIES
}
