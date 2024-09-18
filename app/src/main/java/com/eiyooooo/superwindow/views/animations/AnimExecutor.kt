package com.eiyooooo.superwindow.views.animations

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View

object AnimExecutor {

    private const val DRAG_BAR_PRESS_ALPHA = 1f
    private const val DRAG_BAR_RELEASE_ALPHA = 0.5f
    private const val DRAG_BAR_PRESS_RELEASE_DURATION = 200L
    private val DRAG_BAR_ALPHA_INTERPOLATOR_ARRAY = floatArrayOf(0.34f, 0f, 0.66f, 1f)

    private const val DRAG_BAR_PRESS_SCALE = 1.25f
    private const val DRAG_BAR_RELEASE_SCALE = 1f
    private const val DRAG_BAR_SCALE_DURATION = 200L
    private val DRAG_BAR_SCALE_INTERPOLATOR_ARRAY = floatArrayOf(0.24f, 1f, 0.51f, 1f)

    fun dragPressAnimation(view: View?, isPress: Boolean) {
        if (view == null) return

        val alphaStart = if (isPress) DRAG_BAR_RELEASE_ALPHA else DRAG_BAR_PRESS_ALPHA
        val alphaEnd = if (isPress) DRAG_BAR_PRESS_ALPHA else DRAG_BAR_RELEASE_ALPHA
        val alphaAnim = ObjectAnimator.ofFloat(view, "alpha", alphaStart, alphaEnd)
        alphaAnim.interpolator = EaseCubicInterpolator(
            DRAG_BAR_ALPHA_INTERPOLATOR_ARRAY[0],
            DRAG_BAR_ALPHA_INTERPOLATOR_ARRAY[1],
            DRAG_BAR_ALPHA_INTERPOLATOR_ARRAY[2],
            DRAG_BAR_ALPHA_INTERPOLATOR_ARRAY[3]
        )
        alphaAnim.duration = DRAG_BAR_PRESS_RELEASE_DURATION

        val scaleXStart = if (isPress) DRAG_BAR_RELEASE_SCALE else DRAG_BAR_PRESS_SCALE
        val scaleXEnd = if (isPress) DRAG_BAR_PRESS_SCALE else DRAG_BAR_RELEASE_SCALE
        val scaleXAnim = ObjectAnimator.ofFloat(view, "scaleX", scaleXStart, scaleXEnd)
        scaleXAnim.interpolator = EaseCubicInterpolator(
            DRAG_BAR_SCALE_INTERPOLATOR_ARRAY[0],
            DRAG_BAR_SCALE_INTERPOLATOR_ARRAY[1],
            DRAG_BAR_SCALE_INTERPOLATOR_ARRAY[2],
            DRAG_BAR_SCALE_INTERPOLATOR_ARRAY[3]
        )
        scaleXAnim.duration = DRAG_BAR_SCALE_DURATION

        val scaleYStart = if (isPress) DRAG_BAR_RELEASE_SCALE else DRAG_BAR_PRESS_SCALE
        val scaleYEnd = if (isPress) DRAG_BAR_PRESS_SCALE else DRAG_BAR_RELEASE_SCALE
        val scaleYAnim = ObjectAnimator.ofFloat(view, "scaleY", scaleYStart, scaleYEnd)
        scaleYAnim.interpolator = EaseCubicInterpolator(
            DRAG_BAR_SCALE_INTERPOLATOR_ARRAY[0],
            DRAG_BAR_SCALE_INTERPOLATOR_ARRAY[1],
            DRAG_BAR_SCALE_INTERPOLATOR_ARRAY[2],
            DRAG_BAR_SCALE_INTERPOLATOR_ARRAY[3]
        )
        scaleYAnim.duration = DRAG_BAR_SCALE_DURATION

        val animSet = AnimatorSet()
        animSet.playTogether(alphaAnim, scaleXAnim, scaleYAnim)
        animSet.start()
    }
}
