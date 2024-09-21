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

class SplitHandleView(context: Context) : View(context) {

    private var widgetCards: List<WidgetCardView>? = null

    fun setWidgetCards(widgetCards: List<WidgetCardView>?) {
        this.widgetCards = widgetCards
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
                        AnimExecutor.pressHandleAnimation(this@SplitHandleView, true)
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.rawX - touchX
                        val newX = X + deltaX
                        //TODO: position.update { newX }
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        for (widgetCard in widgetCards!!) {
                            widgetCard.startBlurTransitAnimation()
                        }
                        AnimExecutor.pressHandleAnimation(this@SplitHandleView, false)
                        true
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        for (widgetCard in widgetCards!!) {
                            widgetCard.startBlurTransitAnimation()
                        }
                        AnimExecutor.pressHandleAnimation(this@SplitHandleView, false)
                        false
                    }

                    else -> false
                }
            }
        }
    }

    init {
        id = generateViewId()
        layoutParams = LinearLayout.LayoutParams(context.dp2px(5), context.dp2px(80)).apply {
            gravity = Gravity.CENTER
            val margin = context.dp2px(4)
            setMargins(margin, 0, margin, 0)
        }
        alpha = 0.5f
        background = AppCompatResources.getDrawable(context, R.drawable.split_handle_background)
        setOnTouchListener(touchListener)
    }
}
