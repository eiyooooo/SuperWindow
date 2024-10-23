package com.eiyooooo.superwindow.ui.widgetcard

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Point
import android.view.View
import com.eiyooooo.superwindow.databinding.ItemWidgetCardBinding

class WidgetCardDragShadowBuilder(private val widgetCard: ItemWidgetCardBinding, private val cornerRadius: Float) : View.DragShadowBuilder(widgetCard.widgetView) {

    override fun onProvideShadowMetrics(size: Point, touch: Point) {
        val width = widgetCard.widgetView.width
        val height = widgetCard.widgetView.height
        size.set(width, height)

        val controlBarLocation = IntArray(2)
        widgetCard.controlBar.getLocationOnScreen(controlBarLocation)

        val widgetViewLocation = IntArray(2)
        widgetCard.widgetView.getLocationOnScreen(widgetViewLocation)

        val touchX = controlBarLocation[0] - widgetViewLocation[0] + widgetCard.controlBar.width / 2
        val touchY = controlBarLocation[1] - widgetViewLocation[1] + widgetCard.controlBar.height / 2
        touch.set(touchX, touchY)
    }

    private val path = Path()

    override fun onDrawShadow(canvas: Canvas) {
        val width = widgetCard.widgetView.width.toFloat()
        val height = widgetCard.widgetView.height.toFloat()

        path.reset()
        path.addRoundRect(0f, 0f, width, height, cornerRadius, cornerRadius, Path.Direction.CW)
        val save = canvas.save()
        canvas.clipPath(path)
        widgetCard.root.draw(canvas)
        canvas.restoreToCount(save)
    }
}
