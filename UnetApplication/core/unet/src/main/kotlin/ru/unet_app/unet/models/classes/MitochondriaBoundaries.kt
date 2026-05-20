package ru.unet_app.unet.models.classes

import ru.unet_app.model.PredictedClasses

class MitochondriaBoundaries(
    override val label: Array<FloatArray> = emptyArray()
): BaseLabel() {
    override val type = PredictedClasses.MITOCHONDRIAL_BOUNDARIES
}
