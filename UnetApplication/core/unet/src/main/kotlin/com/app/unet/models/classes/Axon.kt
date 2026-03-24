package com.app.unet.models.classes

import com.app.model.PredictedClasses

class Axon(
    override val label: Array<FloatArray> = emptyArray()
): BaseLabel() {
    override val type = PredictedClasses.AXON
}