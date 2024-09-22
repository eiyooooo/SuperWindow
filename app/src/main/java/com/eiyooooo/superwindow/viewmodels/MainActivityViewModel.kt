package com.eiyooooo.superwindow.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.eiyooooo.superwindow.entities.WidgetCardData
import com.eiyooooo.superwindow.entities.WidgetCardGroup
import com.eiyooooo.superwindow.utils.BlurUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class MainActivityViewModel : ViewModel() {

    private val mWidgetCardGroup: MutableStateFlow<WidgetCardGroup> by lazy { MutableStateFlow(WidgetCardGroup(firstWidgetCard = WidgetCardData(true, "controlPanel"))) }
    val widgetCardGroup: LiveData<WidgetCardGroup> = mWidgetCardGroup.asLiveData()

    fun updateWidgetCardGroup(widgetCardGroup: WidgetCardGroup) {
        mWidgetCardGroup.update { widgetCardGroup }
    }

    var lastWidgetCardGroup: WidgetCardGroup? = null

    override fun onCleared() {
        super.onCleared()
        BlurUtils.destroy()
    }
}
