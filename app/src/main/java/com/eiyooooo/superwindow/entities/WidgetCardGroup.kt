package com.eiyooooo.superwindow.entities

data class WidgetCardGroup(
    val firstWidgetCard: WidgetCardData,
    val secondWidgetCard: WidgetCardData? = null,
    val thirdWidgetCard: WidgetCardData? = null,
    val pendingWidgetCard: WidgetCardData? = null,
    val backgroundWidgetCard: List<WidgetCardData> = listOf()
) {
    val foregroundWidgetCardCount = when {
        secondWidgetCard != null && thirdWidgetCard != null -> 3
        secondWidgetCard != null -> 2
        else -> 1
    }

    val hasPendingWidgetCard = pendingWidgetCard != null

    val isControlPanelForeground = firstWidgetCard.isControlPanel || secondWidgetCard?.isControlPanel == true || thirdWidgetCard?.isControlPanel == true
}
