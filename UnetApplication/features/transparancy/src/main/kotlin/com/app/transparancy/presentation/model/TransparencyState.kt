package com.app.transparancy.presentation.model

import com.app.model.TransparencyType

data class TransparencyState(
    val mitochondria: Float = 1.0f,
    val psd: Float = 1.0f,
    val vesicles: Float = 1.0f,
    val axon: Float = 1.0f,
    val boundaries: Float = 1.0f,
    val mitochondrialBoundaries: Float = 1.0f
) {
    operator fun get(index: Int): Float = when (index) {
        0 -> mitochondria
        1 -> psd
        2 -> vesicles
        3 -> axon
        4 -> boundaries
        5 -> mitochondrialBoundaries
        else -> throw IndexOutOfBoundsException("Невалидный индекс: $index")
    }

    operator fun get(type: TransparencyType): Float = when (type) {
        TransparencyType.MITOCHONDRIA -> mitochondria
        TransparencyType.PSD -> psd
        TransparencyType.VESICLES -> vesicles
        TransparencyType.AXON -> axon
        TransparencyType.BOUNDARIES -> boundaries
        TransparencyType.MITO_BOUNDARIES -> mitochondrialBoundaries
    }
}