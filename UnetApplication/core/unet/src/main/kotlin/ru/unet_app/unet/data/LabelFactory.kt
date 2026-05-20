package ru.unet_app.unet.data

import ru.unet_app.model.PredictedClasses
import ru.unet_app.unet.models.classes.Axon
import ru.unet_app.unet.models.classes.BaseLabel
import ru.unet_app.unet.models.classes.Boundaries
import ru.unet_app.unet.models.classes.Mitochondria
import ru.unet_app.unet.models.classes.MitochondriaBoundaries
import ru.unet_app.unet.models.classes.PSD
import ru.unet_app.unet.models.classes.Vesicles

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
