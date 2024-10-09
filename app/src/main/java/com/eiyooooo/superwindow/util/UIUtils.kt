package com.eiyooooo.superwindow.util

import android.animation.Animator
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

fun startShowElevatedViewAnimation(elevatedViewContainer: View, overlay: View, isShow: Boolean, onAnimationEnd: (() -> Unit)? = null) {
    if (isShow) {
        elevatedViewContainer.visibility = View.VISIBLE
        overlay.visibility = View.VISIBLE
    }

    val duration = if (isShow) 500L else 250L
    val listener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
        }

        override fun onAnimationEnd(animation: Animator) {
            onAnimationEnd?.invoke()
            if (!isShow) {
                elevatedViewContainer.visibility = View.GONE
                overlay.visibility = View.GONE
            }
        }

        override fun onAnimationCancel(animation: Animator) {
        }

        override fun onAnimationRepeat(animation: Animator) {
        }
    }

    val containerAlphaStart = if (isShow) 0f else 1f
    val containerAlphaEnd = if (isShow) 1f else 0f
    val containerAlphaInterpolator =
        if (isShow) PathInterpolatorCompat.create(0f, 0.2f, 0.6f, 1f)
        else PathInterpolatorCompat.create(0.2f, 0f, 1f, 0.6f)

    val containerScaleStart = if (isShow) 0.8f else 1f
    val containerScaleEnd = if (isShow) 1f else 0.8f
    val containerScaleInterpolator =
        if (isShow) PathInterpolatorCompat.create(0.3f, 0.5f, 0.5f, 1f)
        else PathInterpolatorCompat.create(0.5f, 0.3f, 1f, 0.5f)

    val overlayAlphaStart = if (isShow) 0f else 0.5f
    val overlayAlphaEnd = if (isShow) 0.5f else 0f
    val overlayAlphaInterpolator =
        if (isShow) PathInterpolatorCompat.create(0.6f, 0f, 0.75f, 1f)
        else PathInterpolatorCompat.create(0f, 0.6f, 1f, 0.75f)

    val containerAlphaAnimation = ObjectAnimator.ofFloat(elevatedViewContainer, "alpha", containerAlphaStart, containerAlphaEnd).apply {
        this.duration = duration
        this.interpolator = containerAlphaInterpolator
    }
    val containerScaleXAnimation = ObjectAnimator.ofFloat(elevatedViewContainer, "scaleX", containerScaleStart, containerScaleEnd).apply {
        this.duration = duration
        this.interpolator = containerScaleInterpolator
    }
    val containerScaleYAnimation = ObjectAnimator.ofFloat(elevatedViewContainer, "scaleY", containerScaleStart, containerScaleEnd).apply {
        this.duration = duration
        this.interpolator = containerScaleInterpolator
    }
    val overlayAlphaAnimation = ObjectAnimator.ofFloat(overlay, "alpha", overlayAlphaStart, overlayAlphaEnd).apply {
        this.duration = duration
        this.interpolator = overlayAlphaInterpolator
    }

    AnimatorSet().apply {
        playTogether(containerAlphaAnimation, containerScaleXAnimation, containerScaleYAnimation, overlayAlphaAnimation)
        addListener(listener)
        start()
    }
}
