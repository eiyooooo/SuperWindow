package com.eiyooooo.superwindow.ui.widgetcard

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object WidgetCardFocusManager {

    private val mFocusing: MutableStateFlow<String> = MutableStateFlow("controlPanel")
    val focusing: StateFlow<String> = mFocusing

    fun updateFocusing(function: (String) -> String) {
        mFocusing.update(function)
    }

    private val mFocusModeUpdater: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val focusModeUpdater: StateFlow<Boolean> = mFocusModeUpdater

    fun refreshFocusMode() {
        mFocusModeUpdater.update { !it }
    }
}
