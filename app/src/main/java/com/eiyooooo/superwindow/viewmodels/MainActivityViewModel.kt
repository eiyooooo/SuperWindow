package com.eiyooooo.superwindow.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.eiyooooo.superwindow.entities.WindowMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class MainActivityViewModel : ViewModel() {

    private val mWindowMode: MutableStateFlow<WindowMode> by lazy { MutableStateFlow(WindowMode.SINGLE) }
    val windowMode: LiveData<WindowMode> = mWindowMode.asLiveData()

    fun updateWindowMode(windowMode: WindowMode) {
        mWindowMode.update { windowMode }
    }
}
