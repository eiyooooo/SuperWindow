package com.eiyooooo.superwindow.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.animation.PathInterpolatorCompat
import com.eiyooooo.superwindow.content.LocalContent
import com.eiyooooo.superwindow.databinding.ItemWidgetCardBinding
import com.eiyooooo.superwindow.ui.main.MainActivity
import com.eiyooooo.superwindow.ui.widgetcard.IconDragShadowBuilder
import com.eiyooooo.superwindow.ui.widgetcard.WidgetCardData
import com.eiyooooo.superwindow.ui.widgetcard.WidgetCardFocusManager
import com.eiyooooo.superwindow.ui.widgetcard.placeholderWidgetCardData
import com.eiyooooo.superwindow.util.BlurUtils
import com.eiyooooo.superwindow.util.dp2px
import com.eiyooooo.superwindow.util.getAttrColor
import com.eiyooooo.superwindow.util.startPopupMenuAnimation
import com.eiyooooo.superwindow.util.startPressHandleAnimation
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class WidgetCardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, val widgetCardData: WidgetCardData = placeholderWidgetCardData) : CardView(context, attrs, defStyleAttr) {

    constructor(view: View, widgetCardData: WidgetCardData) : this(view.context, widgetCardData = widgetCardData) {
        widgetCard.contentContainer.addView(view)
    }

    private val colorSurface = context.getAttrColor(com.google.android.material.R.attr.colorSurface)
    private val colorSurfaceDimmed = ColorUtils.blendARGB(colorSurface, 0xFF000000.toInt(), 0.8F)

    private val widgetCard: ItemWidgetCardBinding = ItemWidgetCardBinding.inflate(LayoutInflater.from(context), this, true)

    fun setControlBarVisibility(visibility: Int) {
        if (widgetCardData.isPlaceholder) return
        widgetCard.controlBar.visibility = visibility
    }

    val dragging = AtomicBoolean(false)

    private val controlBarListener by lazy {
        object : OnTouchListener {
            private var initialX: Float = 0F
            private var initialY: Float = 0F
            private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
            private val shadowBuilder = IconDragShadowBuilder(widgetCard.icon)

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                return when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = event.x
                        initialY = event.y
                        widgetCard.controlBar.startPressHandleAnimation(true)
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = abs(event.x - initialX)
                        val deltaY = abs(event.y - initialY)
                        if ((deltaX > touchSlop || deltaY > touchSlop) && dragging.compareAndSet(false, true)) {
                            Timber.d("ACTION_MOVE -> startDragAndDrop -> WidgetCardView@${widgetCardData.identifier}")
                            makeCover()
                            (context as? MainActivity)?.startWaitDragEvent()
                            ObjectAnimator.ofFloat(this@WidgetCardView, "alpha", 1F, 0.3F).apply {
                                duration = 200
                                interpolator = PathInterpolatorCompat.create(0.25f, 0.1f, 0.25f, 1f)
                            }.start()
                            startDragAndDrop(ClipData.newPlainText("WidgetCardView@${widgetCardData.identifier}", null), shadowBuilder, this@WidgetCardView, DRAG_FLAG_OPAQUE)
                        }
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        widgetCard.controlBar.startPressHandleAnimation(false)
                        if (!dragging.get()) {
                            if (!showingPopupMenu.get()) {
                                showPopupMenu()
                            } else {
                                hidePopupMenu()
                            }
                        }
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

    fun onDragEnded() {
        if (dragging.compareAndSet(true, false)) {
            ObjectAnimator.ofFloat(this, "alpha", 0.3F, 1F).apply {
                duration = 200
                interpolator = PathInterpolatorCompat.create(0.25f, 0.1f, 0.25f, 1f)
            }.start()
            removeCover()
        }
    }

    private var expandTouchPx = 0
    private var touchingTargetView = false
    private var targetView: View? = null

    private fun setTargetView(view: View?, expandTouchDp: Int = 10) {
        this.targetView = view
        this.expandTouchPx = context.dp2px(expandTouchDp)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            WidgetCardFocusManager.updateFocusing { widgetCardData.identifier }
        }
        val view = targetView ?: return super.dispatchTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isTouchingTargetViewWithExpandRegion(event)) {
                    touchingTargetView = true
                    return view.dispatchTouchEvent(event)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchingTargetView) {
                    return view.dispatchTouchEvent(event)
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (touchingTargetView) {
                    touchingTargetView = false
                    return view.dispatchTouchEvent(event)
                } else {
                    touchingTargetView = false
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun isTouchingTargetViewWithExpandRegion(event: MotionEvent): Boolean {
        targetView?.let {
            if (!it.isShown) return false
            val rect = Rect()
            it.getHitRect(rect)
            rect.left -= expandTouchPx
            rect.right += expandTouchPx
            rect.top -= expandTouchPx
            rect.bottom += expandTouchPx
            if (rect.contains(event.x.toInt(), event.y.toInt())) {
                return true
            }
        }
        return false
    }

    var displayId: Int? = null
        private set
    private var textureView: TextureView? = null

    private val textureViewTouchListener by lazy {
        OnTouchListener { _, event ->
            if (widgetCardData.isLocalProvider) {
                displayId?.let {
                    LocalContent.injectEvent(event, it)
                }
            }
            true
        }
    }

    fun injectBackEvent() {
        if (widgetCardData.isLocalProvider) {
            displayId?.let {
                LocalContent.injectBackEvent(it)
            }
        }
    }

    private fun setContentView(view: View? = null) {
        widgetCard.contentContainer.removeAllViews()
        view?.let {
            widgetCard.contentContainer.addView(view)
        }
    }

    init {
        id = generateViewId()
        layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT)
        radius = context.dp2px(8).toFloat()
        setCardBackgroundColor(0x00FFFFFF)

        setTargetView(widgetCard.controlBar)
        widgetCard.controlBar.setOnTouchListener(controlBarListener)
        widgetCard.replace.setOnClickListener {
            hidePopupMenuImmediately()
            (context as? MainActivity)?.replaceWidgetCard(widgetCardData)
        }
        widgetCard.minimize.setOnClickListener {
            hidePopupMenuImmediately()
            (context as? MainActivity)?.minimizeWidgetCard(widgetCardData)
        }
        widgetCard.close.setOnClickListener {
            hidePopupMenuImmediately()
            (context as? MainActivity)?.removeWidgetCard(this@WidgetCardView)
        }
        widgetCard.minimizeControlPanel.setOnClickListener {
            hidePopupMenuImmediately()
            (context as? MainActivity)?.minimizeWidgetCard(widgetCardData)
        }

        widgetCardData.icon?.let {
            widgetCard.icon.setImageDrawable(it)
        }

        if (widgetCardData.identifierValidated) {
            if (widgetCardData.isLocalProvider) {
                textureView = TextureView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        var surface: Surface? = null

                        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                            surface = Surface(surfaceTexture).also { surface ->
                                LocalContent.getVirtualDisplayIdForPackage(widgetCardData.packageName, width, height, context.resources.displayMetrics.densityDpi, surface)?.let {
                                    displayId = it
                                } ?: post {
                                    release()
                                    (context as? MainActivity)?.removeWidgetCard(this@WidgetCardView)
                                }
                            }
                        }

                        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                            surface?.let { surface ->
                                LocalContent.getVirtualDisplayIdForPackage(widgetCardData.packageName, width, height, context.resources.displayMetrics.densityDpi, surface)?.let {
                                    displayId = it
                                } ?: post {
                                    release()
                                    (context as? MainActivity)?.removeWidgetCard(this@WidgetCardView)
                                }
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
        } else if (widgetCardData.isPlaceholder) {
            widgetCard.widgetView.setBackgroundColor(colorSurfaceDimmed)
            widgetCard.controlBar.visibility = GONE
            widgetCard.contentContainer.visibility = GONE
            widgetCard.iconContainer.visibility = GONE
            widgetCard.icon.setImageDrawable(null)
        }

        post {
            widgetCard.popupMenu.pivotX = widgetCard.popupMenu.width / 2f
            widgetCard.popupMenu.pivotY = 0f
            widgetCard.minimizeControlPanel.pivotX = widgetCard.minimizeControlPanel.width / 2f
            widgetCard.minimizeControlPanel.pivotY = 0f
            widgetCard.popupMenu.visibility = GONE
            widgetCard.minimizeControlPanel.visibility = GONE
        }
    }

    fun release() {
        setContentView()
        makeCover()
    }

    fun changeFocusMode(focus: Boolean) {
        widgetCard.controlBar.changeFocusMode(focus)
    }

    val hinting = AtomicBoolean(false)
    private var hintAnimatorSet: AnimatorSet? = null
    private val showingPopupMenu = AtomicBoolean(false)
    private var popupMenuAnimatorSet: AnimatorSet? = null

    fun showHint() {
        if (widgetCardData.isPlaceholder) return
        if (hinting.get() && hintAnimatorSet?.isRunning == false) return
        hintAnimatorSet?.cancel()
        widgetCard.replaceHint.visibility = VISIBLE
        val interpolator = PathInterpolatorCompat.create(0.35f, 0f, 0f, 1f)
        val hintAlphaAnimation = ObjectAnimator.ofFloat(widgetCard.replaceHint, "alpha", 0.3F, 1F).apply {
            this.duration = duration
            this.interpolator = interpolator
        }
        val cardColorAnimation = ObjectAnimator.ofArgb(widgetCard.widgetView, "backgroundColor", colorSurface, colorSurfaceDimmed).apply {
            this.duration = duration
            this.interpolator = interpolator
        }
        hintAnimatorSet = AnimatorSet().apply {
            playTogether(cardColorAnimation, hintAlphaAnimation)
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    hinting.set(true)
                }

                override fun onAnimationCancel(animation: Animator) {
                }

                override fun onAnimationRepeat(animation: Animator) {
                }
            })
            start()
        }
    }

    fun hideHint() {
        if (widgetCardData.isPlaceholder) return
        if (hinting.get() || hintAnimatorSet?.isRunning == true) {
            hintAnimatorSet?.cancel()
            val interpolator = PathInterpolatorCompat.create(0.35f, 0f, 0f, 1f)
            val hintAlphaAnimation = ObjectAnimator.ofFloat(widgetCard.replaceHint, "alpha", 1F, 0.3F).apply {
                this.duration = duration
                this.interpolator = interpolator
            }
            val cardColorAnimation = ObjectAnimator.ofArgb(widgetCard.widgetView, "backgroundColor", colorSurfaceDimmed, colorSurface).apply {
                this.duration = duration
                this.interpolator = interpolator
            }
            hintAnimatorSet = AnimatorSet().apply {
                playTogether(cardColorAnimation, hintAlphaAnimation)
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        widgetCard.replaceHint.visibility = GONE
                        hinting.set(false)
                    }

                    override fun onAnimationCancel(animation: Animator) {
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                    }
                })
                start()
            }
        }
    }

    private fun showPopupMenu() {
        if (widgetCardData.isPlaceholder) return
        if (showingPopupMenu.get() && popupMenuAnimatorSet?.isRunning == false) return
        popupMenuAnimatorSet?.cancel()
        val popupMenu = if (widgetCardData.isControlPanel) {
            widgetCard.minimizeControlPanel
        } else {
            widgetCard.popupMenu
        }
        popupMenu.visibility = VISIBLE
        (context as? MainActivity)?.setTouchEventInterceptor {
            if (it.action == MotionEvent.ACTION_DOWN) {
                val rect = Rect()
                popupMenu.getGlobalVisibleRect(rect)
                if (!rect.contains(it.rawX.toInt(), it.rawY.toInt())) {
                    hidePopupMenu()
                    return@setTouchEventInterceptor true
                }
            }
            return@setTouchEventInterceptor false
        }
        popupMenuAnimatorSet = popupMenu.startPopupMenuAnimation(true) {
            showingPopupMenu.set(true)
        }
    }

    private fun hidePopupMenuImmediately() {
        if (widgetCardData.isPlaceholder) return
        if (showingPopupMenu.get() || popupMenuAnimatorSet?.isRunning == true) {
            popupMenuAnimatorSet?.cancel()
            (context as? MainActivity)?.setTouchEventInterceptor(null)
            widgetCard.popupMenu.visibility = GONE
            widgetCard.minimizeControlPanel.visibility = GONE
            showingPopupMenu.set(false)
        }
    }

    private fun hidePopupMenu() {
        if (widgetCardData.isPlaceholder) return
        if (showingPopupMenu.get() || popupMenuAnimatorSet?.isRunning == true) {
            popupMenuAnimatorSet?.cancel()
            val popupMenu = if (widgetCardData.isControlPanel) {
                widgetCard.minimizeControlPanel
            } else {
                widgetCard.popupMenu
            }
            (context as? MainActivity)?.setTouchEventInterceptor(null)
            popupMenuAnimatorSet = popupMenu.startPopupMenuAnimation(false) {
                popupMenu.visibility = GONE
                showingPopupMenu.set(false)
            }
        }
    }

    private val covering = AtomicBoolean(false)
    private val coverTransitAnimationList = mutableListOf<ObjectAnimator>()
    private val blurring = AtomicBoolean(false)
    private val blurTransitAnimationList = mutableListOf<AnimatorSet>()

    fun makeCover() {
        if (widgetCardData.isPlaceholder) return
        hidePopupMenuImmediately()
        if (blurring.get()) {
            removeBlurImmediately()
        }
        cancelCoverTransitAnimations()
        widgetCard.iconContainer.visibility = VISIBLE
        widgetCard.contentContainer.visibility = GONE
        widgetCard.contentContainer.alpha = 0F
        covering.set(true)
    }

    fun removeCoverImmediately() {
        if (widgetCardData.isPlaceholder) return
        hidePopupMenuImmediately()
        if (blurring.get()) return
        cancelCoverTransitAnimations()
        widgetCard.contentContainer.alpha = 1F
        widgetCard.contentContainer.visibility = VISIBLE
        widgetCard.iconContainer.visibility = GONE
        covering.set(false)
    }

    fun removeCover() {
        if (widgetCardData.isPlaceholder) return
        hidePopupMenuImmediately()
        if (blurring.get()) return
        if (covering.get()) {
            ObjectAnimator.ofFloat(widgetCard.contentContainer, "alpha", 0F, 1F).apply {
                duration = 300
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        widgetCard.iconContainer.visibility = GONE
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        widgetCard.contentContainer.alpha = 1F
                        widgetCard.contentContainer.visibility = VISIBLE
                        widgetCard.iconContainer.visibility = GONE
                        covering.set(false)
                    }

                    override fun onAnimationCancel(animation: Animator) {
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                    }
                })
                interpolator = PathInterpolatorCompat.create(0.35f, 0f, 0.35f, 1f)
                widgetCard.contentContainer.visibility = VISIBLE
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
        if (widgetCardData.isPlaceholder) return
        hidePopupMenuImmediately()
        if (covering.get()) {
            removeCoverImmediately()
        }
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
        widgetCard.blurLayer.visibility = VISIBLE
        widgetCard.iconContainer.visibility = VISIBLE
        widgetCard.contentContainer.visibility = GONE
        widgetCard.contentContainer.alpha = 0F
        blurring.set(true)
    }

    fun removeBlurImmediately() {
        if (widgetCardData.isPlaceholder) return
        hidePopupMenuImmediately()
        if (covering.get()) return
        cancelBlurTransitAnimations()
        widgetCard.contentContainer.alpha = 1F
        widgetCard.contentContainer.visibility = VISIBLE
        widgetCard.iconContainer.visibility = GONE
        widgetCard.blurLayer.visibility = GONE
        widgetCard.blurLayer.foreground?.let {
            it.alpha = 255
        }
        widgetCard.blurLayer.foreground = null
        blurring.set(false)
    }

    fun removeBlur() {
        if (widgetCardData.isPlaceholder) return
        hidePopupMenuImmediately()
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
                        widgetCard.iconContainer.visibility = GONE
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        widgetCard.contentContainer.alpha = 1F
                        widgetCard.contentContainer.visibility = VISIBLE
                        widgetCard.iconContainer.visibility = GONE
                        widgetCard.blurLayer.visibility = GONE
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
            widgetCard.contentContainer.visibility = VISIBLE
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
