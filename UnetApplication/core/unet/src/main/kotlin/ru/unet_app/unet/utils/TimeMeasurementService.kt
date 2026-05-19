package ru.unet_app.unet.utils

class TimeMeasurementService {
    fun measureTime(block: () -> Unit): Long {
        val startTime = System.currentTimeMillis()
        block()
        return System.currentTimeMillis() - startTime
    }

    suspend fun measureTimeSuspend(block: suspend () -> Unit): Long {
        val startTime = System.currentTimeMillis()
        block()
        return System.currentTimeMillis() - startTime
    }
}