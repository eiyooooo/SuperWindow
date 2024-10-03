package com.eiyooooo.superwindow.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import androidx.core.view.drawToBitmap
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.eiyooooo.superwindow.R
import com.google.android.material.navigation.NavigationBarView

fun Context.dp2px(dp: Int): Int {
    return (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics) + 0.5f).toInt()
}

fun Context.sp2px(sp: Int): Int {
    return (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), resources.displayMetrics) + 0.5f).toInt()
}

fun View.getBitmap(): Bitmap? {
    return try {
        this.drawToBitmap()
    } catch (_: Throwable) {
        null
    }
}

fun NavigationBarView.setupWithViewPager(viewPager: ViewPager2, onPositionChanged: ((Int) -> Unit)? = null) {
    setOnItemSelectedListener { item: MenuItem ->
        when (item.itemId) {
            R.id.homeFragment -> viewPager.currentItem = 0
            R.id.settingsFragment -> viewPager.currentItem = 1
        }
        true
    }
    viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            menu.getItem(position).isChecked = true
            onPositionChanged?.invoke(position)
        }
    })
}
