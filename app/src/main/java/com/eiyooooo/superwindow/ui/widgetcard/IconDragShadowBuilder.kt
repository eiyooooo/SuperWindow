package com.eiyooooo.superwindow.ui.widgetcard

import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Shader
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.eiyooooo.superwindow.R

class IconDragShadowBuilder(private val iconView: ImageView) : View.DragShadowBuilder() {

    private val iconSize = iconView.context.resources.getDimensionPixelSize(R.dimen.icon_size)
    private val cornerRadius = iconView.context.resources.getDimensionPixelSize(R.dimen.icon_corner_radius)

    override fun onProvideShadowMetrics(outShadowSize: Point, outShadowTouchPoint: Point) {
        outShadowSize.set(iconSize, iconSize)
        outShadowTouchPoint.set(iconSize / 2, iconSize)
    }

    override fun onDrawShadow(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            val bitmap = iconView.drawable.toBitmap()
            shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
                setLocalMatrix(Matrix().apply {
                    setScale(iconSize.toFloat() / bitmap.width, iconSize.toFloat() / bitmap.height)
                })
            }
        }
        canvas.drawRoundRect(0f, 0f, iconSize.toFloat(), iconSize.toFloat(), cornerRadius.toFloat(), cornerRadius.toFloat(), paint)
    }
}
