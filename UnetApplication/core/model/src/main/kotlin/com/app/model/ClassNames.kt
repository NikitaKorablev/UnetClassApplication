package com.app.model

enum class PredictedClasses(val className: String) {
    MITOCHONDRIA("mitochondria"),
    PSD("PSD"),
    VESICLES("vesicles"),
    AXON("axon"),
    BOUNDARIES("boundaries"),
    MITOCHONDRIAL_BOUNDARIES("mitochondrial_boundaries");

    fun label(): String = className

    operator fun get(index: Int): PredictedClasses = when (index) {
        0 -> MITOCHONDRIA
        1 -> PSD
        2 -> VESICLES
        3 -> AXON
        4 -> BOUNDARIES
        5 -> MITOCHONDRIAL_BOUNDARIES
        else -> throw IndexOutOfBoundsException("Невалидный индекс: $index")
    }

    companion object {
        const val NUM_CLASSES: Int = 6
    }
}

object ClassNames {
    val NAMES = PredictedClasses.entries.map { it.className }
}