package com.eiyooooo.superwindow.ui.widgetcard

import android.graphics.drawable.Drawable

val controlPanelWidgetCardData = WidgetCardData(isControlPanel = true, isPlaceholder = false, identifier = "controlPanel", icon = null)
val placeholderWidgetCardData = WidgetCardData(isControlPanel = false, isPlaceholder = true, identifier = "placeholder", icon = null)

data class WidgetCardData(
    val isControlPanel: Boolean,
    val isPlaceholder: Boolean,
    val identifier: String,
    val icon: Drawable?
) {
    constructor(packageName: String, providerName: String, icon: Drawable? = null) : this(false, false, "$packageName@$providerName", icon)

    val identifierValidated = identifier.count { it == '@' } == 1

    val packageName = identifier.substringBeforeLast("@")

    val providerName = identifier.substringAfterLast("@")

    val isLocalProvider = providerName == "local"
}
