package com.eiyooooo.superwindow.views

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.eiyooooo.superwindow.databinding.ItemWidgetCardBinding
import com.eiyooooo.superwindow.utils.BlurUtils
import com.eiyooooo.superwindow.views.animations.EaseCubicInterpolator
import java.util.concurrent.atomic.AtomicBoolean

class WidgetCardView(context: Context) {

    constructor(view: View) : this(view.context) {
        widgetCard.contentContainer.addView(view)
    }

    private val widgetCard: ItemWidgetCardBinding = ItemWidgetCardBinding.inflate(LayoutInflater.from(context), null, false)

    private val blurring = AtomicBoolean(false)
    private val blurTransitAnimationList = mutableListOf<AnimatorSet>()

    fun setContentView(view: View?) {
        widgetCard.contentContainer.removeAllViews()
        view?.let {
            widgetCard.contentContainer.addView(view)
        }
    }

    fun setIcon(icon: Int) {
        widgetCard.icon.setImageResource(icon)
    }

    fun getRootView(): View {
        return widgetCard.root
    }

    init {
        widgetCard.widgetView.setTargetView(widgetCard.controlBar)
    }

    fun makeBlur() {
        cancelBlurTransitAnimations()
        widgetCard.blurLayer.foreground = BlurUtils.blurView(widgetCard.contentContainer)
        widgetCard.blurLayer.foreground.alpha = 255
        widgetCard.blurLayer.visibility = View.VISIBLE
        widgetCard.iconContainer.visibility = View.VISIBLE
        widgetCard.contentContainer.visibility = View.GONE
        widgetCard.contentContainer.alpha = 0F
        blurring.set(true)
    }

    fun removeBlurImmediately() {
        widgetCard.contentContainer.alpha = 1F
        widgetCard.contentContainer.visibility = View.VISIBLE
        widgetCard.iconContainer.visibility = View.GONE
        widgetCard.blurLayer.visibility = View.GONE
        widgetCard.blurLayer.foreground.alpha = 255
        widgetCard.blurLayer.foreground = null
        blurring.set(false)
    }

    fun startBlurTransitAnimation() {
        if (blurring.get()) {
            val blurLayerAnimation = ObjectAnimator.ofInt(widgetCard.blurLayer.foreground, "alpha", 255, 0).apply {
                duration = 300
                interpolator = EaseCubicInterpolator(0.35f, 0f, 0.35f, 1f)
            }
            val contentContainerAnimation = ObjectAnimator.ofFloat(widgetCard.contentContainer, "alpha", 0F, 1F).apply {
                duration = 300
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        removeBlurImmediately()
                    }

                    override fun onAnimationCancel(animation: Animator) {
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                    }
                })
                interpolator = EaseCubicInterpolator(0.35f, 0f, 0.35f, 1f)
            }
            widgetCard.iconContainer.visibility = View.GONE
            widgetCard.contentContainer.visibility = View.VISIBLE
            val animSet = AnimatorSet()
            animSet.playTogether(blurLayerAnimation, contentContainerAnimation)
            animSet.start()
            blurTransitAnimationList.add(animSet)
        }
    }

    private fun cancelBlurTransitAnimations() {
        blurTransitAnimationList.forEach {
            it.cancel()
        }
        blurTransitAnimationList.clear()
    }
}