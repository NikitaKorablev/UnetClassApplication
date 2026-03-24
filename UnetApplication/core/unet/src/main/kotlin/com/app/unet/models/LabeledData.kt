package com.app.unet.models

data class LabeledData(
    val mitochondria: Array<FloatArray>,
    val PSD: Array<FloatArray>,
    val vesicles: Array<FloatArray>,
    val axon: Array<FloatArray>,
    val boundaries: Array<FloatArray>,
    val mitochondriaBoundaries: Array<FloatArray>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LabeledData

        if (!mitochondria.contentDeepEquals(other.mitochondria)) return false
        if (!PSD.contentDeepEquals(other.PSD)) return false
        if (!vesicles.contentDeepEquals(other.vesicles)) return false
        if (!axon.contentDeepEquals(other.axon)) return false
        if (!boundaries.contentDeepEquals(other.boundaries)) return false
        if (!mitochondriaBoundaries.contentDeepEquals(other.mitochondriaBoundaries)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mitochondria.contentDeepHashCode()
        result = 31 * result + PSD.contentDeepHashCode()
        result = 31 * result + vesicles.contentDeepHashCode()
        result = 31 * result + axon.contentDeepHashCode()
        result = 31 * result + boundaries.contentDeepHashCode()
        result = 31 * result + mitochondriaBoundaries.contentDeepHashCode()
        return result
    }
}