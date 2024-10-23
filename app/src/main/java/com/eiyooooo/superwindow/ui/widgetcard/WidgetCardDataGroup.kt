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

    fun swap(firstPosition: Int, secondPosition: Int): WidgetCardDataGroup {
        var newFirstWidgetCardData = firstWidgetCardData
        var newSecondWidgetCardData = secondWidgetCardData
        var newThirdWidgetCardData = thirdWidgetCardData

        when (firstPosition) {
            1 -> when (secondPosition) {
                2 -> {
                    newFirstWidgetCardData = secondWidgetCardData!!
                    newSecondWidgetCardData = firstWidgetCardData
                }

                3 -> {
                    newFirstWidgetCardData = thirdWidgetCardData!!
                    newThirdWidgetCardData = firstWidgetCardData
                }
            }

            2 -> when (secondPosition) {
                1 -> {
                    newSecondWidgetCardData = firstWidgetCardData
                    newFirstWidgetCardData = secondWidgetCardData!!
                }

                3 -> {
                    newSecondWidgetCardData = thirdWidgetCardData!!
                    newThirdWidgetCardData = secondWidgetCardData
                }
            }

            3 -> when (secondPosition) {
                1 -> {
                    newThirdWidgetCardData = firstWidgetCardData
                    newFirstWidgetCardData = thirdWidgetCardData!!
                }

                2 -> {
                    newThirdWidgetCardData = secondWidgetCardData!!
                    newSecondWidgetCardData = thirdWidgetCardData
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
