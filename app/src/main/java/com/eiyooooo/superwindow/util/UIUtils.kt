package com.eiyooooo.superwindow.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.animation.Interpolator
import androidx.core.view.animation.PathInterpolatorCompat
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

fun View.startAlphaScaleAnimation(alphaStart: Float, alphaEnd: Float, alphaInterpolator: Interpolator, scaleStart: Float, scaleEnd: Float, scaleInterpolator: Interpolator, duration: Long) {
    val alphaAnimation = ObjectAnimator.ofFloat(this, "alpha", alphaStart, alphaEnd).apply {
        this.duration = duration
        this.interpolator = alphaInterpolator
    }
    val scaleXAnimation = ObjectAnimator.ofFloat(this, "scaleX", scaleStart, scaleEnd).apply {
        this.duration = duration
        this.interpolator = scaleInterpolator
    }
    val scaleYAnimation = ObjectAnimator.ofFloat(this, "scaleY", scaleStart, scaleEnd).apply {
        this.duration = duration
        this.interpolator = scaleInterpolator
    }
    val animSet = AnimatorSet()
    animSet.playTogether(alphaAnimation, scaleXAnimation, scaleYAnimation)
    animSet.start()
}

fun View.startPressHandleAnimation(isPress: Boolean) = startAlphaScaleAnimation(
    alphaStart = if (isPress) 0.5f else 1f,
    alphaEnd = if (isPress) 1f else 0.5f,
    alphaInterpolator = PathInterpolatorCompat.create(0.35f, 0f, 0.66f, 1f),
    scaleStart = if (isPress) 1f else 1.25f,
    scaleEnd = if (isPress) 1.25f else 1f,
    scaleInterpolator = PathInterpolatorCompat.create(0.25f, 1f, 0.5f, 1f),
    duration = 200L
)
