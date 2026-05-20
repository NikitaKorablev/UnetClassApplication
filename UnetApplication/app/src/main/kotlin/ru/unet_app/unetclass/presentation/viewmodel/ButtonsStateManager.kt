package ru.unet_app.unetclass.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ButtonsStateManager {
    private val _selectImageButtonIsEnabled = MutableLiveData(true)
    val selectImageButtonIsEnabled: LiveData<Boolean> = _selectImageButtonIsEnabled

    private val _predictButtonIsEnabled = MutableLiveData(false)
    val predictButtonsIsEnabled: LiveData<Boolean> = _predictButtonIsEnabled

    private val _settingsButtonIsEnabled = MutableLiveData(false)
    val settingsButtonIsEnabled: LiveData<Boolean> = _settingsButtonIsEnabled

    private val _moreInfoButtonIsEnabled = MutableLiveData(false)
    val moreInfoButtonIsEnabled: LiveData<Boolean> = _moreInfoButtonIsEnabled

    fun imageSelected() {
        _predictButtonIsEnabled.value = true
    }

    fun imageNotSelected() {
        _predictButtonIsEnabled.value = false
    }

    fun enableButtons() {
        _selectImageButtonIsEnabled.value = true
        _predictButtonIsEnabled.value = true
        _settingsButtonIsEnabled.value = true
        _moreInfoButtonIsEnabled.value = true
    }

    fun disableButtons() {
        _selectImageButtonIsEnabled.value = false
        _predictButtonIsEnabled.value = false
        _settingsButtonIsEnabled.value = false
        _moreInfoButtonIsEnabled.value = false
    }

}
