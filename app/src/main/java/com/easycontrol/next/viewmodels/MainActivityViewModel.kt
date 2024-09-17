package com.easycontrol.next.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class MainActivityViewModel : ViewModel() {

    private val mDualSplitHandlePosition: MutableStateFlow<Float> by lazy { MutableStateFlow(-1F) }
    val dualSplitHandlePosition: LiveData<Float> = mDualSplitHandlePosition.asLiveData()

    fun updateDualSplitHandlePosition(position: Float) {
        mDualSplitHandlePosition.update { position }
    }
}
