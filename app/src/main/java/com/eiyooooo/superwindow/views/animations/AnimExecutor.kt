package com.eiyooooo.superwindow.views.animations

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View

object AnimExecutor {

    fun pressHandleAnimation(view: View?, isPress: Boolean) {
        if (view == null) return

        val alphaStart = if (isPress) 0.5f else 1f
        val alphaEnd = if (isPress) 1f else 0.5f
        val alphaAnimation = ObjectAnimator.ofFloat(view, "alpha", alphaStart, alphaEnd).apply {
            duration = 200L
            interpolator = EaseCubicInterpolator(0.34f, 0f, 0.66f, 1f)
        }

        val scaleStart = if (isPress) 1f else 1.25f
        val scaleEnd = if (isPress) 1.25f else 1f
        val scaleXAnimation = ObjectAnimator.ofFloat(view, "scaleX", scaleStart, scaleEnd).apply {
            duration = 200L
            interpolator = EaseCubicInterpolator(0.24f, 1f, 0.51f, 1f)
        }

        val scaleYAnimation = ObjectAnimator.ofFloat(view, "scaleY", scaleStart, scaleEnd).apply {
            duration = 200L
            interpolator = EaseCubicInterpolator(0.24f, 1f, 0.51f, 1f)
        }

        val animSet = AnimatorSet()
        animSet.playTogether(alphaAnimation, scaleXAnimation, scaleYAnimation)
        animSet.start()
    }
}
