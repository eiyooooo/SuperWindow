package com.eiyooooo.superwindow.ui.widgetcard

import android.graphics.drawable.Drawable

data class WidgetCardData(
    val isControlPanel: Boolean,
    val identifier: String,
    val icon: Drawable? = null,
) {
    constructor(packageName: String, providerName: String, icon: Drawable? = null) : this(false, "$packageName@$providerName", icon)

    val identifierValidated = identifier.count { it == '@' } == 1

    val packageName = identifier.substringBeforeLast("@")

    val providerName = identifier.substringAfterLast("@")

    val isLocalProvider = providerName == "local"
}
