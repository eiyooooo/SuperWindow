package com.eiyooooo.superwindow.utils

import android.content.Context
import android.util.TypedValue

fun Context.dp2px(dp: Int): Int {
    return (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics) + 0.5f).toInt()
}
