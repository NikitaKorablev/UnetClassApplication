package com.app.unetclass.domain

interface ITimeMeasurementUseCase {
    fun measureTime(block: () -> Unit): Long
    suspend fun measureTimeSuspend(block: suspend () -> Unit): Long
}