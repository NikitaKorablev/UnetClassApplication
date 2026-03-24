package com.app.unet.data

import com.app.model.PredictedClasses
import com.app.unet.models.classes.Axon
import com.app.unet.models.classes.BaseLabel
import com.app.unet.models.classes.Boundaries
import com.app.unet.models.classes.Mitochondria
import com.app.unet.models.classes.MitochondriaBoundaries
import com.app.unet.models.classes.PSD
import com.app.unet.models.classes.Vesicles

class LabelFactory {
    companion object {
        fun getLabel(className: String, label: Array<FloatArray>): BaseLabel {
            return when (className) {
                PredictedClasses.MITOCHONDRIA.className -> Mitochondria(label)
                PredictedClasses.PSD.className -> PSD(label)
                PredictedClasses.VESICLES.className -> Vesicles(label)
                PredictedClasses.AXON.className -> Axon(label)
                PredictedClasses.BOUNDARIES.className -> Boundaries(label)
                PredictedClasses.MITOCHONDRIAL_BOUNDARIES.className -> MitochondriaBoundaries(label)
                else -> throw IllegalArgumentException("Unknown class getClassName: $className")
            }
        }
    }
}