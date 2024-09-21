package com.eiyooooo.superwindow.entities

import android.graphics.drawable.Drawable

data class WidgetCardData(
    val isControlPanel: Boolean,
    val identifier: String,
    val icon: Drawable? = null,
)
