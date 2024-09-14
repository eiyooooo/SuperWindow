package com.easycontrol.next.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class CustomLinearLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val MIN_HEIGHT_DP = 80
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val minHeightPx = (MIN_HEIGHT_DP * resources.displayMetrics.density).toInt()
        if (measuredHeight < minHeightPx) setMeasuredDimension(measuredWidth, minHeightPx)
    }
}
