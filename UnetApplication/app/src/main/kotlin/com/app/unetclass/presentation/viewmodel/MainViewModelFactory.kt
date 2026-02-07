package com.app.unetclass.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.unetclass.domain.ISegmentationUseCase

class MainViewModelFactory(
    private val application: Application,
    private val segmentationUseCase: ISegmentationUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(application, segmentationUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}