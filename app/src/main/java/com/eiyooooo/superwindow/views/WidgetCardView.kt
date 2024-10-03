package com.eiyooooo.superwindow.views

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.animation.PathInterpolatorCompat
import com.eiyooooo.superwindow.databinding.ItemWidgetCardBinding
import com.eiyooooo.superwindow.entities.WidgetCardData
import com.eiyooooo.superwindow.utils.BlurUtils
import com.eiyooooo.superwindow.utils.startPressHandleAnimation
import com.eiyooooo.superwindow.wrappers.LocalContent
import java.util.concurrent.atomic.AtomicBoolean

@SuppressLint("ClickableViewAccessibility")
class WidgetCardView(context: Context, val widgetCardData: WidgetCardData) {

    constructor(view: View, widgetCardData: WidgetCardData) : this(view.context, widgetCardData) {
        widgetCard.contentContainer.addView(view)
    }

    private val widgetCard: ItemWidgetCardBinding = ItemWidgetCardBinding.inflate(LayoutInflater.from(context), null, false)

    private val covering = AtomicBoolean(false)
    private val coverTransitAnimationList = mutableListOf<ObjectAnimator>()
    private val blurring = AtomicBoolean(false)
    private val blurTransitAnimationList = mutableListOf<AnimatorSet>()

    private fun setContentView(view: View? = null) {
        widgetCard.contentContainer.removeAllViews()
        view?.let {
            widgetCard.contentContainer.addView(view)
        }
    }

    fun getRootView(): View {
        return widgetCard.root
    }

    fun getControlBar(): View {
        return widgetCard.controlBar
    }

    var displayId: Int? = null
        private set
    lateinit var packageName: String
        private set
    lateinit var providerName: String
        private set
    private var textureView: TextureView? = null

    private val textureViewTouchListener by lazy {
        object : OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                displayId?.let {
                    LocalContent.injectMotionEvent(event, it)
                }
                return true
            }
        }
    }

    private val controlBarListener by lazy {
        object : OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                return when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        //TODO: handle X, Y
                        widgetCard.controlBar.startPressHandleAnimation(true)
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        //TODO: handle X, Y
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        widgetCard.controlBar.startPressHandleAnimation(false)
                        true
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        widgetCard.controlBar.startPressHandleAnimation(false)
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
        widgetCard.controlBar.setOnTouchListener(controlBarListener)

        if (widgetCardData.identifier.contains("@")) {
            packageName = widgetCardData.identifier.split("@")[0]
            providerName = widgetCardData.identifier.split("@")[1]
            LocalContent.getPackageIcon(packageName)?.let {
                widgetCard.icon.setImageDrawable(it)
            }
            textureView = TextureView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    var surface: Surface? = null

                    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                        surface = Surface(surfaceTexture).also {
                            displayId = LocalContent.getVirtualDisplayIdForPackage(packageName, width, height, context.resources.displayMetrics.densityDpi, it)
                        }
                    }

                    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                        surface?.let {
                            displayId = LocalContent.getVirtualDisplayIdForPackage(packageName, width, height, context.resources.displayMetrics.densityDpi, it)
                        }
                    }

                    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                        surface?.release()
                        return true
                    }

                    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
                    }
                }
                setOnTouchListener(textureViewTouchListener)
            }
            setContentView(textureView)
        }
    }

    fun release() {
        setContentView()
        makeCover()
        if (::packageName.isInitialized) {
            LocalContent.releaseVirtualDisplayForPackage(packageName)
        }
    }

    fun makeCover() {
        if (blurring.get()) return
        cancelCoverTransitAnimations()
        widgetCard.iconContainer.visibility = View.VISIBLE
        widgetCard.contentContainer.visibility = View.GONE
        widgetCard.contentContainer.alpha = 0F
        covering.set(true)
    }

    fun removeCoverImmediately() {
        if (blurring.get()) return
        cancelCoverTransitAnimations()
        widgetCard.contentContainer.alpha = 1F
        widgetCard.contentContainer.visibility = View.VISIBLE
        widgetCard.iconContainer.visibility = View.GONE
        covering.set(false)
    }

    fun startCoverTransitAnimation() {
        if (blurring.get()) return
        if (covering.get()) {
            ObjectAnimator.ofFloat(widgetCard.contentContainer, "alpha", 0F, 1F).apply {
                duration = 300
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        widgetCard.iconContainer.visibility = View.GONE
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        widgetCard.contentContainer.alpha = 1F
                        widgetCard.contentContainer.visibility = View.VISIBLE
                        widgetCard.iconContainer.visibility = View.GONE
                        covering.set(false)
                    }

                    override fun onAnimationCancel(animation: Animator) {
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                    }
                })
                interpolator = PathInterpolatorCompat.create(0.35f, 0f, 0.35f, 1f)
                widgetCard.contentContainer.visibility = View.VISIBLE
                startDelay = 250
                start()
                coverTransitAnimationList.add(this)
            }
        }
    }

    private fun cancelCoverTransitAnimations() {
        coverTransitAnimationList.forEach {
            it.cancel()
        }
        coverTransitAnimationList.clear()
    }

    fun makeBlur() {
        if (covering.get()) return
        cancelBlurTransitAnimations()
        textureView?.let { textureView ->
            BlurUtils.blurBitmap(textureView.bitmap)?.let {
                widgetCard.blurLayer.foreground = it.toDrawable(textureView.resources)
                widgetCard.blurLayer.foreground.alpha = 255
            }
        } ?: BlurUtils.blurView(widgetCard.contentContainer)?.let {
            widgetCard.blurLayer.foreground = it
            widgetCard.blurLayer.foreground.alpha = 255
        }
        widgetCard.blurLayer.visibility = View.VISIBLE
        widgetCard.iconContainer.visibility = View.VISIBLE
        widgetCard.contentContainer.visibility = View.GONE
        widgetCard.contentContainer.alpha = 0F
        blurring.set(true)
    }

    fun removeBlurImmediately() {
        if (covering.get()) return
        cancelBlurTransitAnimations()
        widgetCard.contentContainer.alpha = 1F
        widgetCard.contentContainer.visibility = View.VISIBLE
        widgetCard.iconContainer.visibility = View.GONE
        widgetCard.blurLayer.visibility = View.GONE
        widgetCard.blurLayer.foreground?.let {
            it.alpha = 255
        }
        widgetCard.blurLayer.foreground = null
        blurring.set(false)
    }

    fun startBlurTransitAnimation() {
        if (covering.get()) return
        if (blurring.get()) {
            val blurLayerAnimation = widgetCard.blurLayer.foreground.let {
                ObjectAnimator.ofInt(it, "alpha", 255, 0).apply {
                    duration = 300
                    interpolator = PathInterpolatorCompat.create(0.35f, 0f, 0.35f, 1f)
                }
            } ?: null
            val contentContainerAnimation = ObjectAnimator.ofFloat(widgetCard.contentContainer, "alpha", 0F, 1F).apply {
                duration = 300
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        widgetCard.iconContainer.visibility = View.GONE
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        widgetCard.contentContainer.alpha = 1F
                        widgetCard.contentContainer.visibility = View.VISIBLE
                        widgetCard.iconContainer.visibility = View.GONE
                        widgetCard.blurLayer.visibility = View.GONE
                        widgetCard.blurLayer.foreground?.let {
                            it.alpha = 255
                        }
                        widgetCard.blurLayer.foreground = null
                        blurring.set(false)
                    }

                    override fun onAnimationCancel(animation: Animator) {
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                    }
                })
                interpolator = PathInterpolatorCompat.create(0.35f, 0f, 0.35f, 1f)
            }
            widgetCard.contentContainer.visibility = View.VISIBLE
            AnimatorSet().apply {
                blurLayerAnimation?.let {
                    playTogether(it, contentContainerAnimation)
                } ?: play(contentContainerAnimation)
                startDelay = 250
                start()
                blurTransitAnimationList.add(this)
            }
        }
    }

    private fun cancelBlurTransitAnimations() {
        blurTransitAnimationList.forEach {
            it.cancel()
        }
        blurTransitAnimationList.clear()
    }
}