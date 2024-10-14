package com.eiyooooo.superwindow.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.eiyooooo.superwindow.util.startPressHandleAnimation
import kotlin.math.abs

class SplitHandleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private var widgetCards: Array<out WidgetCardView>? = null

    fun setWidgetCards(vararg widgetCards: WidgetCardView) {
        for (widgetCard in widgetCards) {
            widgetCard.removeCoverImmediately()
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
            private var initialViewX: Float = 0F
            private var initialRawX: Float = 0F
            private var initialX: Float = 0F
            private var initialY: Float = 0F
            val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (widgetCards.isNullOrEmpty()) return false
                return when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        for (widgetCard in widgetCards!!) {
                            widgetCard.makeBlur()
                        }
                        initialViewX = v.x
                        initialRawX = event.rawX
                        initialX = event.x
                        initialY = event.y
                        startPressHandleAnimation(true)
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = abs(event.x - initialX)
                        val deltaY = abs(event.y - initialY)
                        if (deltaX > touchSlop || deltaY > touchSlop) {
                            onDragHandle?.invoke(initialViewX + event.rawX - initialRawX)
                        }
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
