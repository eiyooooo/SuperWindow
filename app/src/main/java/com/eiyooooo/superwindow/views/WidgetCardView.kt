package com.eiyooooo.superwindow.views

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import androidx.core.view.animation.PathInterpolatorCompat
import com.eiyooooo.superwindow.databinding.ItemWidgetCardBinding
import com.eiyooooo.superwindow.entities.WidgetCardData
import com.eiyooooo.superwindow.utils.BlurUtils
import com.eiyooooo.superwindow.views.animations.AnimExecutor
import java.util.concurrent.atomic.AtomicBoolean

class WidgetCardView(context: Context, val widgetCardData: WidgetCardData) {

    constructor(view: View, widgetCardData: WidgetCardData) : this(view.context, widgetCardData) {
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

    fun getContentView(): View? {
        return widgetCard.contentContainer.getChildAt(0)
    }

    fun setIcon(icon: Int) {
        widgetCard.icon.setImageResource(icon)
    }

    fun getRootView(): View {
        return widgetCard.root
    }

    fun getControlBar(): View {
        return widgetCard.controlBar
    }

    private val touchListener by lazy {
        object : OnTouchListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                return when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        //TODO: handle X, Y
                        AnimExecutor.pressHandleAnimation(widgetCard.controlBar, true)
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        //TODO: handle X, Y
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        AnimExecutor.pressHandleAnimation(widgetCard.controlBar, false)
                        true
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        AnimExecutor.pressHandleAnimation(widgetCard.controlBar, false)
                        false
                    }

                    else -> false
                }
            }
        }
    }

    init {
        widgetCard.widgetView.setTargetView(widgetCard.controlBar)
        widgetCard.root.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        widgetCardData.icon?.let {
            widgetCard.icon.setImageDrawable(it)
        }
        widgetCard.controlBar.setOnTouchListener(touchListener)
    }

    private var canBlur = true

    fun makeBlur() {
        cancelBlurTransitAnimations()
        val blurredDrawable = BlurUtils.blurView(widgetCard.contentContainer)
        widgetCard.blurLayer.foreground = blurredDrawable
        if (blurredDrawable != null) {
            canBlur = true
            widgetCard.blurLayer.foreground.alpha = 255
        } else {
            canBlur = false
        }
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
        if (canBlur) {
            widgetCard.blurLayer.foreground.alpha = 255
        }
        widgetCard.blurLayer.foreground = null
        blurring.set(false)
    }

    fun startBlurTransitAnimation() {
        if (blurring.get()) {
            val blurLayerAnimation = if (canBlur) {
                ObjectAnimator.ofInt(widgetCard.blurLayer.foreground, "alpha", 255, 0).apply {
                    duration = 300
                    interpolator = PathInterpolatorCompat.create(0.35f, 0f, 0.35f, 1f)
                }
            } else null
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
                interpolator = PathInterpolatorCompat.create(0.35f, 0f, 0.35f, 1f)
            }
            widgetCard.iconContainer.visibility = View.GONE
            widgetCard.contentContainer.visibility = View.VISIBLE
            val animSet = AnimatorSet()
            if (blurLayerAnimation != null) {
                animSet.playTogether(blurLayerAnimation, contentContainerAnimation)
            } else {
                animSet.play(contentContainerAnimation)
            }
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