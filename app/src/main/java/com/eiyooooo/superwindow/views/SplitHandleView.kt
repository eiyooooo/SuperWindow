package com.eiyooooo.superwindow.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.eiyooooo.superwindow.utils.startPressHandleAnimation

class SplitHandleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private var widgetCards: Array<out WidgetCardView>? = null

    fun setWidgetCards(vararg widgetCards: WidgetCardView) {
        for (widgetCard in widgetCards) {
            widgetCard.removeBlurImmediately()
        }
        if (widgetCards.isEmpty()) {
            this.widgetCards = null
        } else {
            this.widgetCards = widgetCards
        }
    }

    private var onDragHandle: ((newX: Float) -> Unit)? = null

    fun setOnDragHandle(onDragHandle: ((newX: Float) -> Unit)?) {
        this.onDragHandle = onDragHandle
    }

    private val touchListener by lazy {
        object : OnTouchListener {
            private var X: Float = 0F
            private var touchX: Float = 0F

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (widgetCards.isNullOrEmpty()) return false
                return when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        for (widgetCard in widgetCards!!) {
                            widgetCard.makeBlur()
                        }
                        X = v.x
                        touchX = event.rawX
                        startPressHandleAnimation(true)
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.rawX - touchX
                        val newX = X + deltaX
                        onDragHandle?.invoke(newX)
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        for (widgetCard in widgetCards!!) {
                            widgetCard.startBlurTransitAnimation()
                        }
                        startPressHandleAnimation(false)
                        true
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        for (widgetCard in widgetCards!!) {
                            widgetCard.startBlurTransitAnimation()
                        }
                        startPressHandleAnimation(false)
                        false
                    }

                    else -> false
                }
            }
        }
    }

    init {
        setOnTouchListener(touchListener)
    }
}
