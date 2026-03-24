package com.app.model

enum class PredictedClasses(val className: String) {
    MITOCHONDRIA("mitochondria"),
    PSD("PSD"),
    VESICLES("vesicles"),
    AXON("axon"),
    BOUNDARIES("boundaries"),
    MITOCHONDRIAL_BOUNDARIES("mitochondrial_boundaries");

    fun name(): String = className
}

object ClassNames {
    val NAMES = PredictedClasses.entries.map { it.className }
}