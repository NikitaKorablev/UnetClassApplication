package com.app.unet.models.classes

import com.app.model.PredictedClasses

class PSD(
    override val label: Array<FloatArray> = emptyArray()
): BaseLabel() {
    override val type: PredictedClasses = PredictedClasses.PSD
}