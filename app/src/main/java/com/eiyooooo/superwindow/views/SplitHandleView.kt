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

class SplitHandleView(context: Context) {

    private val splitHandle: View = View(context).apply {
        id = View.generateViewId()
        layoutParams = LinearLayout.LayoutParams(
            context.dp2px(5),
            context.dp2px(80)
        ).apply {
            gravity = Gravity.CENTER
            setMargins(context.dp2px(4), context.dp2px(4), context.dp2px(4), context.dp2px(4))
        }
        alpha = 0.5f
        background = AppCompatResources.getDrawable(context, R.drawable.split_handle_background)
    }

    private val splitHandleListener by lazy {
        object : View.OnTouchListener {
            private var X: Float = 0F
            private var touchX: Float = 0F

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                return when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
//                        showBlurLayer()
                        X = v.x
                        touchX = event.rawX
//                        AnimExecutor.dragPressAnimation(bindingExpanded.splitHandle, true)
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.rawX - touchX
                        val newX = X + deltaX
//                        mainModel.updateDualSplitHandlePosition(newX)
                        true
                    }

                    MotionEvent.ACTION_UP -> {
//                        removeBlurLayerImmediately()
//                        AnimExecutor.dragPressAnimation(bindingExpanded.splitHandle, false)
                        true
                    }

                    MotionEvent.ACTION_CANCEL -> {
//                        removeBlurLayerImmediately()
//                        AnimExecutor.dragPressAnimation(bindingExpanded.splitHandle, false)
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
