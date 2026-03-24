package com.app.model

data class TransparencyState(
    val mitochondria: Float = 0.3f,
    val psd: Float = 0.7f,
    val vesicles: Float = 0.6f,
    val axon: Float = 0.8f,
    val boundaries: Float = 1.0f,
    val mitochondrialBoundaries: Float = 0.45f
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