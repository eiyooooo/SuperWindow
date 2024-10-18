package com.eiyooooo.superwindow.ui.widgetcard

data class WidgetCardDataGroup(
    val firstWidgetCardData: WidgetCardData,
    val secondWidgetCardData: WidgetCardData? = null,
    val thirdWidgetCardData: WidgetCardData? = null,
    var pendingPosition: Int = 0,
    val backgroundWidgetCardData: List<WidgetCardData> = listOf()
) {
    val foregroundWidgetCardCount = when {
        secondWidgetCardData != null && thirdWidgetCardData != null -> 3
        secondWidgetCardData != null -> 2
        else -> 1
    }

    val isControlPanelForeground = firstWidgetCardData.isControlPanel || secondWidgetCardData?.isControlPanel == true || thirdWidgetCardData?.isControlPanel == true

    fun updatePendingPosition(position: Int): WidgetCardDataGroup {
        pendingPosition = position
        return this
    }
}
