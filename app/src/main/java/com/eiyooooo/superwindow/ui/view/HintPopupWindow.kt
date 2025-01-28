package com.eiyooooo.superwindow.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatTextView
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.util.dp2px
import com.eiyooooo.superwindow.util.getAttrColor

class HintPopupWindow(context: Context, text: String) : PopupWindow() {

    constructor(context: Context, textResId: Int) : this(context, context.getString(textResId))

    private val showAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.show_hint_popup_view)
    }

    private val dismissAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.dismiss_hint_popup_view)
    }

    init {
        contentView = HintView(context).apply {
            setText(text)
        }
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
    }

    override fun showAtLocation(parent: View, gravity: Int, x: Int, y: Int) {
        super.showAtLocation(parent, gravity, x, y)
        contentView.startAnimation(showAnimation)
    }

    override fun dismiss() {
        dismissAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                super@HintPopupWindow.dismiss()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        contentView.clearAnimation()
        contentView.startAnimation(dismissAnimation)
    }

    private class HintView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
    ) : AppCompatTextView(context, attrs, defStyleAttr) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val path = Path()
        private val bubblePadding = context.dp2px(12)
        private val arrowHeight = context.dp2px(8)
        private val cornerRadius = context.dp2px(8).toFloat()

        init {
            setPadding(bubblePadding, bubblePadding, bubblePadding, bubblePadding + arrowHeight)
            paint.color = context.getAttrColor(com.google.android.material.R.attr.colorSurfaceContainerHighest)
        }

        override fun onDraw(canvas: Canvas) {
            path.reset()
            val width = width.toFloat()
            val height = height.toFloat()
            val arrowTop = height - arrowHeight

            path.moveTo(0f, cornerRadius)
            path.quadTo(0f, 0f, cornerRadius, 0f)
            path.lineTo(width - cornerRadius, 0f)
            path.quadTo(width, 0f, width, cornerRadius)
            path.lineTo(width, arrowTop - cornerRadius)
            path.quadTo(width, arrowTop, width - cornerRadius, arrowTop)
            path.lineTo(width / 2 + arrowHeight, arrowTop)
            path.lineTo(width / 2, height)
            path.lineTo(width / 2 - arrowHeight, arrowTop)
            path.lineTo(cornerRadius, arrowTop)
            path.quadTo(0f, arrowTop, 0f, arrowTop - cornerRadius)
            path.lineTo(0f, cornerRadius)
            path.close()

            canvas.drawPath(path, paint)
            super.onDraw(canvas)
        }
    }
}
