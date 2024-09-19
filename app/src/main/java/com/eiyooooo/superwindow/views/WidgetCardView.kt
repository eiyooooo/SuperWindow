package com.eiyooooo.superwindow.views

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.eiyooooo.superwindow.databinding.ItemWidgetCardBinding
import com.eiyooooo.superwindow.utils.BlurUtils

class WidgetCardView(layoutInflater: LayoutInflater) {

    private val widgetCard: ItemWidgetCardBinding = ItemWidgetCardBinding.inflate(layoutInflater)

    private fun makeBlur() {
        cancelBlurTransitAnimations()
        widgetCard.blurLayer.foreground = BlurUtils.blurView(widgetCard.contentContainer)
        widgetCard.blurLayer.visibility = View.VISIBLE
        widgetCard.iconContainer.visibility = View.VISIBLE
        widgetCard.contentContainer.visibility = View.GONE
    }

    private fun removeBlurImmediately() {
        widgetCard.contentContainer.visibility = View.VISIBLE
        widgetCard.iconContainer.visibility = View.GONE
        widgetCard.blurLayer.visibility = View.GONE
        widgetCard.blurLayer.foreground = null
    }

    private val mBlurTransitAnimationList = mutableListOf<ObjectAnimator>()

    private fun cancelBlurTransitAnimations() {
        mBlurTransitAnimationList.forEach {
            it.cancel()
        }
        mBlurTransitAnimationList.clear()
    }

    private fun removeBlurWithAnimation(view: FrameLayout) {
        if (view.foreground != null) {
            val removeForegroundAnim = ObjectAnimator.ofInt(view.foreground, "alpha", 255, 0).apply {
                duration = 400
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        //TODO: if(!isPressDrag.get())
                        view.foreground.alpha = 255
                        view.foreground = null
                        widgetCard.blurLayer.visibility = View.GONE
                    }

                    override fun onAnimationCancel(animation: Animator) {
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                    }
                })
                start()
            }
            mBlurTransitAnimationList.add(removeForegroundAnim)
        }
    }
}