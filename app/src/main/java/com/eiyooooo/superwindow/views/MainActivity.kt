package com.eiyooooo.superwindow.views

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.databinding.ActivityMainCompactBinding
import com.eiyooooo.superwindow.databinding.ActivityMainExpandedBinding
import com.eiyooooo.superwindow.databinding.ControlPanelBinding
import com.eiyooooo.superwindow.entities.WindowMode
import com.eiyooooo.superwindow.utils.BlurUtils
import com.eiyooooo.superwindow.utils.dp2px
import com.eiyooooo.superwindow.viewmodels.MainActivityViewModel
import com.eiyooooo.superwindow.views.animations.AnimExecutor
import com.eiyooooo.superwindow.views.animations.EaseCubicInterpolator
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity() {

    private lateinit var windowMode: WindowMode
    private lateinit var bindingCompact: ActivityMainCompactBinding
    private lateinit var bindingExpanded: ActivityMainExpandedBinding

    private lateinit var bindingControlPanel: ControlPanelBinding

    private val mainModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        windowMode = when (resources.configuration.screenWidthDp) {
            in 0..599 -> {
                bindingCompact = ActivityMainCompactBinding.inflate(layoutInflater).also {
                    bindingControlPanel = ControlPanelBinding.inflate(layoutInflater, it.root, true)
                    setContentView(it.root)
                }
                WindowMode.COMPACT
            }

            else -> {
                setupFullScreen()
                bindingExpanded = ActivityMainExpandedBinding.inflate(layoutInflater).also {
                    it.widgetContainer.setTargetView(it.splitHandle)
                    it.leftView.widgetView.setTargetView(it.leftView.controlBar)
                    it.rightView.widgetView.setTargetView(it.rightView.controlBar)

                    it.splitHandle.setOnTouchListener(dualSplitHandleListener)

                    bindingControlPanel = ControlPanelBinding.inflate(layoutInflater)
                    it.leftView.contentContainer.addView(bindingControlPanel.root)

                    setContentView(it.root)
                }
                mainModel.dualSplitHandlePosition.observe(this) {
                    updateDualLayout(it)
                }
                WindowMode.DUAL
            }
        }

        if (this::bindingControlPanel.isInitialized) {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            bindingControlPanel.bottomNavigation.setupWithNavController(navController)
            setSupportActionBar(bindingControlPanel.toolbar)
        }
    }

    private val dualSplitHandleListener by lazy {
        object : View.OnTouchListener {
            private var X: Float = 0F
            private var touchX: Float = 0F

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                return when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        makeBlur()
                        X = bindingExpanded.splitHandle.x
                        touchX = event.rawX
                        AnimExecutor.dragPressAnimation(bindingExpanded.splitHandle, true)
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.rawX - touchX
                        val newX = X + deltaX
                        mainModel.updateDualSplitHandlePosition(newX)
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        startBlurTransitAnimation()
                        AnimExecutor.dragPressAnimation(bindingExpanded.splitHandle, false)
                        true
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        startBlurTransitAnimation()
                        AnimExecutor.dragPressAnimation(bindingExpanded.splitHandle, false)
                        false
                    }

                    else -> false
                }
            }
        }
    }

    private val blurring = AtomicBoolean(false)
    private val blurTransitAnimationList = mutableListOf<AnimatorSet>()

    private fun makeBlur() {
        cancelBlurTransitAnimations()
        bindingExpanded.leftView.blurLayer.foreground = BlurUtils.blurView(bindingExpanded.leftView.contentContainer)
        bindingExpanded.leftView.blurLayer.foreground.alpha = 255
        bindingExpanded.leftView.blurLayer.visibility = View.VISIBLE
        bindingExpanded.leftView.iconContainer.visibility = View.VISIBLE
        bindingExpanded.leftView.contentContainer.visibility = View.GONE
        bindingExpanded.leftView.contentContainer.alpha = 0F
        blurring.set(true)
    }

    private fun removeBlurImmediately() {
        bindingExpanded.leftView.contentContainer.alpha = 1F
        bindingExpanded.leftView.contentContainer.visibility = View.VISIBLE
        bindingExpanded.leftView.iconContainer.visibility = View.GONE
        bindingExpanded.leftView.blurLayer.visibility = View.GONE
        bindingExpanded.leftView.blurLayer.foreground.alpha = 255
        bindingExpanded.leftView.blurLayer.foreground = null
        blurring.set(false)
    }

    private fun startBlurTransitAnimation() {
        if (blurring.get()) {
            val blurLayerAnimation = ObjectAnimator.ofInt(bindingExpanded.leftView.blurLayer.foreground, "alpha", 255, 0).apply {
                duration = 300
                interpolator = EaseCubicInterpolator(0.35f, 0f, 0.35f, 1f)
            }
            val contentContainerAnimation = ObjectAnimator.ofFloat(bindingExpanded.leftView.contentContainer, "alpha", 0F, 1F).apply {
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
            bindingExpanded.leftView.iconContainer.visibility = View.GONE
            bindingExpanded.leftView.contentContainer.visibility = View.VISIBLE
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

    private fun updateDualLayout(newX: Float) {
        if (newX < 0) return

        val constraintSet = ConstraintSet()
        constraintSet.clone(bindingExpanded.widgetContainer)

        constraintSet.connect(R.id.split_handle, ConstraintSet.START, R.id.widget_container, ConstraintSet.START, newX.toInt())
        constraintSet.connect(R.id.split_handle, ConstraintSet.END, R.id.right_view, ConstraintSet.START, 0)
        constraintSet.connect(R.id.left_view, ConstraintSet.END, R.id.split_handle, ConstraintSet.START, dp2px(4))
        constraintSet.connect(R.id.right_view, ConstraintSet.START, R.id.split_handle, ConstraintSet.END, dp2px(4))

        constraintSet.applyTo(bindingExpanded.widgetContainer)
    }

    @Suppress("DEPRECATION")
    private fun setupFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    internal fun showSnackBar(text: String) {
        when (windowMode) {
            WindowMode.COMPACT -> Snackbar.make(bindingCompact.root, text, Snackbar.LENGTH_LONG).setAnchorView(bindingControlPanel.bottomNavigation).show()
            WindowMode.DUAL -> Snackbar.make(bindingExpanded.root, text, Snackbar.LENGTH_LONG).setAnchorView(bindingControlPanel.bottomNavigation).show()
        }
    }
}
