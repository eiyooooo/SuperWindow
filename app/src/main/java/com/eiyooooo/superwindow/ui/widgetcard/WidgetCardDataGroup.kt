package com.eiyooooo.superwindow.ui.widgetcard

data class WidgetCardDataGroup(
    val firstWidgetCardData: WidgetCardData,
    val secondWidgetCardData: WidgetCardData? = null,
    val thirdWidgetCardData: WidgetCardData? = null,
    val dragging: Boolean = false,
    val backgroundWidgetCardData: List<WidgetCardData> = listOf()
) {
    val foregroundWidgetCardCount = when {
        secondWidgetCardData != null && thirdWidgetCardData != null -> 3
        secondWidgetCardData != null -> 2
        else -> 1
    }

    val isControlPanelForeground = firstWidgetCardData.isControlPanel || secondWidgetCardData?.isControlPanel == true || thirdWidgetCardData?.isControlPanel == true

    fun swap(first: WidgetCardData, second: WidgetCardData): WidgetCardDataGroup {
        if (first == second) return this

        var newFirstWidgetCardData = firstWidgetCardData
        var newSecondWidgetCardData = secondWidgetCardData
        var newThirdWidgetCardData = thirdWidgetCardData

        when (first) {
            firstWidgetCardData -> {
                newFirstWidgetCardData = second
                when (second) {
                    secondWidgetCardData -> newSecondWidgetCardData = first
                    thirdWidgetCardData -> newThirdWidgetCardData = first
                }
            }

            secondWidgetCardData -> {
                newSecondWidgetCardData = second
                when (second) {
                    firstWidgetCardData -> newFirstWidgetCardData = first
                    thirdWidgetCardData -> newThirdWidgetCardData = first
                }
            }

            thirdWidgetCardData -> {
                newThirdWidgetCardData = second
                when (second) {
                    firstWidgetCardData -> newFirstWidgetCardData = first
                    secondWidgetCardData -> newSecondWidgetCardData = first
                }
            }
        }

        return copy(
            firstWidgetCardData = newFirstWidgetCardData,
            secondWidgetCardData = newSecondWidgetCardData,
            thirdWidgetCardData = newThirdWidgetCardData
        )
    }
}
