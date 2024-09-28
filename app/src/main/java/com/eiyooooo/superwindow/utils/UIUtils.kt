package com.eiyooooo.superwindow.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.TypedValue
import android.view.View
import androidx.core.view.drawToBitmap

fun Context.dp2px(dp: Int): Int {
    return (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics) + 0.5f).toInt()
}

fun View.getBitmap(): Bitmap? {
    return try {
        this.drawToBitmap()
    } catch (_: Throwable) {
        null
    }
}
