package com.eiyooooo.superwindow.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import com.eiyooooo.superwindow.util.getAttrColor
import java.util.concurrent.atomic.AtomicBoolean

class ControlBarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private var focusMode = AtomicBoolean(false)
    private var rectangleAlpha = 0f
    private var animator: ValueAnimator? = null

    private val rectanglePaint = Paint().apply {
        color = context.getAttrColor(com.google.android.material.R.attr.colorSurfaceInverse)
        style = Paint.Style.FILL
    }

    private val circlePaint = Paint().apply {
        color = context.getAttrColor(com.google.android.material.R.attr.colorSurfaceInverse)
        style = Paint.Style.FILL
    }

    fun changeFocusMode(focus: Boolean) {
        if (focusMode.compareAndSet(!focus, focus)) {
            animator?.cancel()
            val targetRectangleAlpha = if (focus) 1f else 0f
            animator = ValueAnimator.ofFloat(rectangleAlpha, targetRectangleAlpha).apply {
                duration = 150
                interpolator = AccelerateInterpolator()
                addUpdateListener {
                    rectangleAlpha = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (rectangleAlpha != 0f) {
            drawRectangle(canvas)
        }
        if (rectangleAlpha != 1f) {
            drawCircles(canvas)
        }
    }

    private fun drawRectangle(canvas: Canvas) {
        val radius = height / 2f
        val rectRight = width.toFloat()
        val rectBottom = height.toFloat()
        rectanglePaint.alpha = (rectangleAlpha * 255).toInt()
        canvas.drawRoundRect(0f, 0f, rectRight, rectBottom, radius, radius, rectanglePaint)
    }

    private fun drawCircles(canvas: Canvas) {
        val radius = height / 2f
        val totalCirclesWidth = 5 * radius * 2
        val spacing = (width - totalCirclesWidth) / 4f
        for (i in 0..4) {
            val cx = i * (2 * radius + spacing) + radius
            val cy = height / 2f
            canvas.drawCircle(cx, cy, radius, circlePaint)
        }
    }
}
