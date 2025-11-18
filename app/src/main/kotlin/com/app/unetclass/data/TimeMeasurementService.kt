package com.app.unetclass.data

import com.app.unetclass.domain.ITimeMeasurementUseCase

class TimeMeasurementService : ITimeMeasurementUseCase {
    override fun measureTime(block: () -> Unit): Long {
        val startTime = System.currentTimeMillis()
        block()
        return System.currentTimeMillis() - startTime
    }
    
    override suspend fun measureTimeSuspend(block: suspend () -> Unit): Long {
        val startTime = System.currentTimeMillis()
        block()
        return System.currentTimeMillis() - startTime
    }
}