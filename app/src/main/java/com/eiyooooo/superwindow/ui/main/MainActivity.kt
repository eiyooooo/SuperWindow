package com.eiyooooo.superwindow.ui.main

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.eiyooooo.superwindow.databinding.ActivityMainCompactBinding
import com.eiyooooo.superwindow.databinding.ActivityMainExpandedBinding
import com.eiyooooo.superwindow.databinding.ControlPanelCompactBinding
import com.eiyooooo.superwindow.databinding.ControlPanelExpandedBinding
import com.eiyooooo.superwindow.entity.Preferences
import com.eiyooooo.superwindow.ui.controlpanel.ControlPanelAdapter
import com.eiyooooo.superwindow.ui.widgetcard.WidgetCardManager
import com.eiyooooo.superwindow.util.setupWithViewPager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var isExpanded: Boolean = false
    private lateinit var bindingCompact: ActivityMainCompactBinding
    internal lateinit var bindingExpanded: ActivityMainExpandedBinding
    private lateinit var bindingControlPanelCompact: ControlPanelCompactBinding
    internal lateinit var bindingControlPanelExpanded: ControlPanelExpandedBinding

    private val mainModel: MainActivityViewModel by viewModels()
    private val controlPanelAdapter: ControlPanelAdapter by lazy { ControlPanelAdapter(this) }
    internal val widgetCardManager: WidgetCardManager by lazy { WidgetCardManager(this, mainModel) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        isExpanded = when (resources.configuration.screenWidthDp) {
            in 0..599 -> {
                bindingCompact = ActivityMainCompactBinding.inflate(layoutInflater).also {
                    bindingControlPanelCompact = ControlPanelCompactBinding.inflate(layoutInflater, it.root, true)
                    setContentView(it.root)
                }
                false
            }

            else -> {
                setupFullScreen()
                bindingExpanded = ActivityMainExpandedBinding.inflate(layoutInflater).also {
                    bindingControlPanelExpanded = ControlPanelExpandedBinding.inflate(layoutInflater, null, false)
                    it.root.post {
                        windowManager.updateViewLayout(bindingExpanded.root.rootView, bindingExpanded.root.rootView.layoutParams.apply {
                            val flagsField = this.javaClass.getDeclaredField("flags")
                            val flags = flagsField.getInt(this)
                            flagsField.set(this, flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
                        })//TODO: only add when localContent is presented
                    }
                    it.widgetContainer.addTargetView(it.leftSplitHandle)
                    it.widgetContainer.addTargetView(it.rightSplitHandle)
                    widgetCardManager.init()
                    setContentView(it.root)
                }
                lifecycleScope.launch {
                    Preferences.topBottomPaddingFlow.collect {
                        bindingExpanded.root.setPadding(0, it, 0, it)
                    }
                }
                true
            }
        }

        setupControlPanel()

        mainModel.addShizukuListener()
    }

    override fun onResume() {
        super.onResume()
        mainModel.checkShizukuPermission()
    }

    override fun recreate() {
        widgetCardManager.destroy()
        super.recreate()
    }

    override fun onDestroy() {
        widgetCardManager.destroy()
        mainModel.removeShizukuListener()
        super.onDestroy()
    }

    private fun setupControlPanel() {
        val lastControlPanelPage = mainModel.currentControlPanelPage
        if (isExpanded) {
            bindingControlPanelExpanded.viewPager.apply {
                adapter = controlPanelAdapter
                offscreenPageLimit = 2
                bindingControlPanelExpanded.bottomNavigation.setupWithViewPager(this) {
                    mainModel.currentControlPanelPage = it
                }
                bindingControlPanelExpanded.navigationRail.setupWithViewPager(this)
                post {
                    setCurrentItem(lastControlPanelPage, false)
                }
            }
            setSupportActionBar(bindingControlPanelExpanded.toolbar)
        } else {
            bindingControlPanelCompact.viewPager.apply {
                adapter = controlPanelAdapter
                offscreenPageLimit = 2
                bindingControlPanelCompact.bottomNavigation.setupWithViewPager(this) {
                    mainModel.currentControlPanelPage = it
                }
                post {
                    setCurrentItem(lastControlPanelPage, false)
                }
            }
            setSupportActionBar(bindingControlPanelCompact.toolbar)
        }
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
        if (isExpanded) {
            val widgetCardGroup = mainModel.widgetCardGroup.value!!
            if (widgetCardGroup.isControlPanelForeground) {
                if (widgetCardGroup.foregroundWidgetCardCount == 1) {
                    Snackbar.make(bindingExpanded.root, text, Snackbar.LENGTH_LONG).setAnchorView(bindingExpanded.bar).show()
                } else {
                    Snackbar.make(bindingExpanded.root, text, Snackbar.LENGTH_LONG).setAnchorView(bindingControlPanelExpanded.bottomNavigation).show()
                }
            } else {
                Snackbar.make(bindingExpanded.root, text, Snackbar.LENGTH_LONG).setAnchorView(bindingExpanded.bar).show()
            }
        } else {
            Snackbar.make(bindingCompact.root, text, Snackbar.LENGTH_LONG).setAnchorView(bindingControlPanelCompact.bottomNavigation).show()
        }
    }

    internal fun makeCardsBlur(blur: Boolean) = widgetCardManager.makeCardsBlur(blur)
}
