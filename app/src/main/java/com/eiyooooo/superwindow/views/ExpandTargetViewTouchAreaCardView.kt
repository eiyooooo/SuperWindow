package com.eiyooooo.superwindow.views

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.cardview.widget.CardView
import com.eiyooooo.superwindow.utils.dp2px

class ExpandTargetViewTouchAreaCardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : CardView(context, attrs, defStyleAttr) {

    private var expandTouchPx = 0
    private var touchingTargetView = false
    private var targetView: View? = null

    fun setTargetView(view: View?, expandTouchDp: Int = 10) {
        this.targetView = view
        this.expandTouchPx = context.dp2px(expandTouchDp)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val view = targetView ?: return super.dispatchTouchEvent(event)
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isTouchingTargetViewRegion(event)) {
                    touchingTargetView = true
                    return view.dispatchTouchEvent(event)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchingTargetView) {
                    return view.dispatchTouchEvent(event)
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (touchingTargetView) {
                    touchingTargetView = false
                    return view.dispatchTouchEvent(event)
                } else {
                    touchingTargetView = false
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun isTouchingTargetViewRegion(event: MotionEvent?): Boolean {
        targetView?.let {
            if (!it.isShown) return false
            val rect = Rect()
            it.getHitRect(rect)
            rect.left -= expandTouchPx
            rect.right += expandTouchPx
            rect.top -= expandTouchPx
            rect.bottom += expandTouchPx
            val touchX: Int = event?.x?.toInt() ?: 0
            val touchY: Int = event?.y?.toInt() ?: 0
            if (rect.contains(touchX, touchY)) {
                return true
            }
        }
        return false
    }
}
