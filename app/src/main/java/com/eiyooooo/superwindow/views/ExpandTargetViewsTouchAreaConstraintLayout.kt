package com.eiyooooo.superwindow.views

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.eiyooooo.superwindow.utils.dp2px

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

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                touchingTargetView = getTouchingTargetView(event)
                if (touchingTargetView != null) {
                    return touchingTargetView!!.dispatchTouchEvent(event)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchingTargetView != null) {
                    return touchingTargetView!!.dispatchTouchEvent(event)
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (touchingTargetView != null) {
                    val result = touchingTargetView!!.dispatchTouchEvent(event)
                    touchingTargetView = null
                    return result
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun getTouchingTargetView(event: MotionEvent?): View? {
        val touchX: Int = event?.x?.toInt() ?: 0
        val touchY: Int = event?.y?.toInt() ?: 0

        for (view in targetViews) {
            val rect = Rect()
            view.getHitRect(rect)
            rect.left -= expandTouchPx
            rect.right += expandTouchPx
            rect.top -= expandTouchPx
            rect.bottom += expandTouchPx

            if (rect.contains(touchX, touchY)) {
                return view
            }
        }
        return null
    }
}
