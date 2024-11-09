package com.eiyooooo.superwindow.ui.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.eiyooooo.superwindow.util.dp2px

class ExpandTargetViewsTouchAreaConstraintLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var expandTouchPx = context.dp2px(20)
    private var touchingTargetView: View? = null
    private var targetViews: MutableList<View> = mutableListOf()

    fun setExpandTouchDp(expandTouchDp: Int = 20) {
        this.expandTouchPx = context.dp2px(expandTouchDp)
    }

    fun addTargetView(view: View) {
        targetViews.add(view)
    }

    fun clearTargetViews() {
        targetViews.clear()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                getTouchingTargetView(event)?.let {
                    touchingTargetView = it
                    return it.dispatchTouchEvent(event)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                touchingTargetView?.let {
                    return it.dispatchTouchEvent(event)
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                touchingTargetView?.let {
                    val result = it.dispatchTouchEvent(event)
                    touchingTargetView = null
                    return result
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun getTouchingTargetView(event: MotionEvent): View? {
        for (view in targetViews) {
            if (!view.isShown) continue
            val rect = Rect()
            view.getHitRect(rect)
            rect.left -= expandTouchPx
            rect.right += expandTouchPx
            rect.top -= expandTouchPx
            rect.bottom += expandTouchPx
            if (rect.contains(event.x.toInt(), event.y.toInt())) {
                return view
            }
        }
        return null
    }
}
