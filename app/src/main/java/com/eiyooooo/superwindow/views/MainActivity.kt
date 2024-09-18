package com.eiyooooo.superwindow.views

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
import com.eiyooooo.superwindow.databinding.ActivityMainDualBinding
import com.eiyooooo.superwindow.databinding.ActivityMainMultiBinding
import com.eiyooooo.superwindow.databinding.ControlPanelBinding
import com.eiyooooo.superwindow.entities.WindowMode
import com.eiyooooo.superwindow.utils.dp2px
import com.eiyooooo.superwindow.viewmodels.MainActivityViewModel
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var windowMode: WindowMode
    private lateinit var bindingCompact: ActivityMainCompactBinding
    private lateinit var bindingDual: ActivityMainDualBinding
    private lateinit var bindingMulti: ActivityMainMultiBinding

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
                bindingDual = ActivityMainDualBinding.inflate(layoutInflater).also {
                    it.main.setTargetView(it.splitHandle)
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
                        X = bindingDual.splitHandle.x
                        touchX = event.rawX
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.rawX - touchX
                        val newX = X + deltaX
                        mainModel.updateDualSplitHandlePosition(newX)
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun updateDualLayout(newX: Float) {
        if (newX < 0) return

        val constraintSet = ConstraintSet()
        constraintSet.clone(bindingDual.main)

        constraintSet.connect(R.id.split_handle, ConstraintSet.START, R.id.main, ConstraintSet.START, newX.toInt())
        constraintSet.connect(R.id.split_handle, ConstraintSet.END, R.id.right_view, ConstraintSet.START, 0)
        constraintSet.connect(R.id.left_view, ConstraintSet.END, R.id.split_handle, ConstraintSet.START, dp2px(4))
        constraintSet.connect(R.id.right_view, ConstraintSet.START, R.id.split_handle, ConstraintSet.END, dp2px(4))

        constraintSet.applyTo(bindingDual.main)
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
            WindowMode.DUAL -> Snackbar.make(bindingDual.root, text, Snackbar.LENGTH_LONG).setAnchorView(bindingControlPanel.bottomNavigation).show()
            WindowMode.MULTI -> Snackbar.make(bindingMulti.root, text, Snackbar.LENGTH_LONG).setAnchorView(bindingControlPanel.bottomNavigation).show()
        }
    }
}
