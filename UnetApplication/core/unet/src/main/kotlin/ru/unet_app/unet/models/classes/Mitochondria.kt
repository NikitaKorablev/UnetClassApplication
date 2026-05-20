package ru.unet_app.unet.models.classes

import ru.unet_app.model.PredictedClasses

class Mitochondria(
    override val label: Array<FloatArray> = emptyArray()
): BaseLabel() {
    override val type = PredictedClasses.MITOCHONDRIA
}
