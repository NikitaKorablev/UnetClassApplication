package com.app.unetclass.models

data class TransparencyState(
    val transparencyValues: List<Float> = getDefaultValues()
) {
    companion object {
        const val NUM_CLASSES = 6

        fun getDefaultValues(): List<Float> = listOf(0.3f, 0.7f, 0.6f, 0.8f, 1.0f, 0.45f)
    }

    init {
        require(transparencyValues.size == NUM_CLASSES) {
            "Количество значений прозрачности должно быть равно $NUM_CLASSES"
        }
        transparencyValues.forEach { value ->
            require(value in 0.0f..1.0f) {
                "Значения прозрачности должны быть в диапазоне от 0.0 до 1.0"
            }
        }
    }
    
    fun updateTransparency(classIndex: Int, value: Float): TransparencyState {
        require(classIndex in 0 until NUM_CLASSES) { "Индекс класса должен быть от 0 до ${NUM_CLASSES - 1}" }
        require(value in 0.0f..1.0f) { "Значение прозрачности должно быть в диапазоне от 0.0 до 1.0" }
        
        val newValues = transparencyValues.toMutableList()
        newValues[classIndex] = value
        return TransparencyState(newValues)
    }
    
    fun updateMultipleTransparency(values: List<Float>): TransparencyState {
        require(values.size == NUM_CLASSES) { "Количество значений должно быть равно $NUM_CLASSES" }
        
        values.forEach { value ->
            require(value in 0.0f..1.0f) { "Значения прозрачности должны быть в диапазоне от 0.0 до 1.0" }
        }
        
        return TransparencyState(values)
    }
}