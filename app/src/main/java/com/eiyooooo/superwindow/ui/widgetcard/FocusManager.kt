package com.eiyooooo.superwindow.ui.widgetcard

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object FocusManager {

    private val mFocusingWidgetCard: MutableStateFlow<String> = MutableStateFlow("controlPanel")
    val focusingWidgetCard: StateFlow<String> = mFocusingWidgetCard

    fun updateFocusingWidgetCard(function: (String) -> String) {
        mFocusingWidgetCard.update(function)
    }
}
