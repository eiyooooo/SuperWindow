package com.eiyooooo.superwindow.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.eiyooooo.superwindow.entities.WidgetCardData
import com.eiyooooo.superwindow.entities.WindowMode
import com.eiyooooo.superwindow.utils.BlurUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class MainActivityViewModel : ViewModel() {

    private val mWindowMode: MutableStateFlow<WindowMode> by lazy { MutableStateFlow(WindowMode.SINGLE) }
    val windowMode: LiveData<WindowMode> = mWindowMode.asLiveData()

    fun updateWindowMode(windowMode: WindowMode) {
        mWindowMode.update { windowMode }
    }

    private val mFirstWidgetCardData: MutableStateFlow<WidgetCardData> by lazy { MutableStateFlow(WidgetCardData(true, "controlPanel")) }
    val firstWidgetCardData: LiveData<WidgetCardData> = mFirstWidgetCardData.asLiveData()

    fun updateFirstWidgetCardData(widgetCardData: WidgetCardData) {
        mFirstWidgetCardData.update { widgetCardData }
    }

    private val mSecondWidgetCardData: MutableStateFlow<WidgetCardData?> by lazy { MutableStateFlow(null) }
    val secondWidgetCardData: LiveData<WidgetCardData?> = mSecondWidgetCardData.asLiveData()

    fun updateSecondWidgetCardData(widgetCardData: WidgetCardData?) {
        mSecondWidgetCardData.update { widgetCardData }
    }

    private val mThirdWidgetCardData: MutableStateFlow<WidgetCardData?> by lazy { MutableStateFlow(null) }
    val thirdWidgetCardData: LiveData<WidgetCardData?> = mThirdWidgetCardData.asLiveData()

    fun updateThirdWidgetCardData(widgetCardData: WidgetCardData?) {
        mThirdWidgetCardData.update { widgetCardData }
    }

    private val mBackgroundWidgetCardData: MutableStateFlow<List<WidgetCardData>> by lazy { MutableStateFlow(listOf()) }
    val backgroundWidgetCardData: LiveData<List<WidgetCardData>> = mBackgroundWidgetCardData.asLiveData()

    fun updateBackgroundWidgetCardData(widgetCardData: List<WidgetCardData>) {
        mBackgroundWidgetCardData.update { widgetCardData }
    }

    override fun onCleared() {
        super.onCleared()
        BlurUtils.destroy()
    }
}
