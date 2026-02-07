package com.app.unetclass.domain.repository

interface ITransparencyStateProvider {
    /**
     * Получает текущее состояние прозракости для каждого класса
     * @return список значений прозракости (0.0 - 1.0) для каждого класса
     */
    fun getTransparencyState(): List<Float>
    
    /**
     * Устанавливает состояние прозракости для классов
     * @param transparencyValues список значений прозракости
     */
    fun setTransparencyState(transparencyValues: List<Float>)
    
    /**
     * Сбрасывает значения прозракости к стандартным
     */
    fun resetToDefault()
}