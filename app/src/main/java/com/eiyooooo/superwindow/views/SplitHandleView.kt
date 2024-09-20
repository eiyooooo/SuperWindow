package com.eiyooooo.superwindow.views

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.utils.dp2px
import com.eiyooooo.superwindow.views.animations.AnimExecutor

class SplitHandleView(context: Context) {

    private val splitHandle: View = View(context).apply {
        val margin = context.dp2px(4)
        id = View.generateViewId()
        layoutParams = LinearLayout.LayoutParams(
            context.dp2px(5),
            context.dp2px(80)
        ).apply {
            gravity = Gravity.CENTER
            setMargins(margin, margin, margin, margin)
        }
        alpha = 0.5f
        background = AppCompatResources.getDrawable(context, R.drawable.split_handle_background)
    }

    private var initialized = false

    private lateinit var leftWidgetCard: WidgetCardView
    private lateinit var rightWidgetCard: WidgetCardView

    fun setWidgetCard(leftWidgetCard: WidgetCardView, rightWidgetCard: WidgetCardView) {
        this.leftWidgetCard = leftWidgetCard
        this.rightWidgetCard = rightWidgetCard
        initialized = true
    }

    private val splitHandleListener by lazy {
        object : View.OnTouchListener {
            private var X: Float = 0F
            private var touchX: Float = 0F

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (!initialized) return false
                return when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        leftWidgetCard.makeBlur()
                        rightWidgetCard.makeBlur()
                        X = v.x
                        touchX = event.rawX
                        AnimExecutor.dragPressAnimation(splitHandle, true)
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.rawX - touchX
                        val newX = X + deltaX
//TODO                        position.update { newX }
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        leftWidgetCard.startBlurTransitAnimation()
                        rightWidgetCard.startBlurTransitAnimation()
                        AnimExecutor.dragPressAnimation(splitHandle, false)
                        true
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        leftWidgetCard.startBlurTransitAnimation()
                        rightWidgetCard.startBlurTransitAnimation()
                        AnimExecutor.dragPressAnimation(splitHandle, false)
                        false
                    }

                    else -> false
                }
            }
        }
    }

    init {
        splitHandle.setOnTouchListener(splitHandleListener)
    }
}
