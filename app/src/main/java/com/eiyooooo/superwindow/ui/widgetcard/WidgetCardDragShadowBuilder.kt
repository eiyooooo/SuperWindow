package com.eiyooooo.superwindow.ui.widgetcard

import android.graphics.Canvas
import android.graphics.Point
import android.view.View
import com.eiyooooo.superwindow.databinding.ItemWidgetCardBinding

class WidgetCardDragShadowBuilder(private val widgetCard: ItemWidgetCardBinding) : View.DragShadowBuilder(widgetCard.widgetView) {

    override fun onProvideShadowMetrics(size: Point, touch: Point) {
        size.set(widgetCard.iconContainer.width, widgetCard.iconContainer.height)
        touch.set(widgetCard.iconContainer.width / 2, widgetCard.iconContainer.height)
    }

    override fun onDrawShadow(canvas: Canvas) {
        widgetCard.iconContainer.draw(canvas)
    }
}
