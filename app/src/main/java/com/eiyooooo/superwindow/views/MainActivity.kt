package com.eiyooooo.superwindow.views

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.adapters.WidgetCardManager
import com.eiyooooo.superwindow.databinding.ActivityMainCompactBinding
import com.eiyooooo.superwindow.databinding.ActivityMainExpandedBinding
import com.eiyooooo.superwindow.databinding.ControlPanelBinding
import com.eiyooooo.superwindow.databinding.ControlPanelExpandedBinding
import com.eiyooooo.superwindow.entities.WindowMode
import com.eiyooooo.superwindow.viewmodels.MainActivityViewModel
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private var windowMode: WindowMode? = null
    private lateinit var bindingCompact: ActivityMainCompactBinding
    private lateinit var bindingExpanded: ActivityMainExpandedBinding

    private lateinit var bindingControlPanelCompact: ControlPanelBinding
    private lateinit var bindingControlPanelExpanded: ControlPanelExpandedBinding

    private val mainModel: MainActivityViewModel by viewModels()
    private val widgetCardManager: WidgetCardManager by lazy { WidgetCardManager(this, mainModel) }//TODO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        windowMode = when (resources.configuration.screenWidthDp) {
            in 0..599 -> {
                bindingCompact = ActivityMainCompactBinding.inflate(layoutInflater).also {
                    bindingControlPanelCompact = ControlPanelBinding.inflate(layoutInflater, it.root, true)
                    setContentView(it.root)
                }
                null
            }

            else -> {
                setupFullScreen()
                bindingExpanded = ActivityMainExpandedBinding.inflate(layoutInflater).also {
                    setContentView(it.root)
                }
                when (mainModel.windowMode.value) {
                    WindowMode.SINGLE -> {
                        bindingControlPanelExpanded = ControlPanelExpandedBinding.inflate(layoutInflater, null, false)//TODO
                    }

                    else -> {
                        bindingControlPanelCompact = ControlPanelBinding.inflate(layoutInflater, null, false)//TODO
                    }
                }
                mainModel.windowMode.value
            }
        }

        setupControlPanel()

        windowMode?.let {
            //TODO: init widgetCardManager with it
        }

        mainModel.windowMode.observe(this) {
            if (windowMode != it) {
                recreate()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //TODO: recycle widgetCardManager
    }

    private fun setupControlPanel() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        when (windowMode) {
            WindowMode.SINGLE -> {
                bindingControlPanelExpanded.navigationRail.setupWithNavController(navHostFragment.navController)
                setSupportActionBar(bindingControlPanelExpanded.toolbar)
            }

            else -> {
                bindingControlPanelCompact.bottomNavigation.setupWithNavController(navHostFragment.navController)
                setSupportActionBar(bindingControlPanelCompact.toolbar)
            }
        }
    }

    fun getControlPanelView(): View {
        return when (windowMode) {
            WindowMode.SINGLE -> bindingControlPanelExpanded.root
            else -> bindingControlPanelCompact.root
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
        when (windowMode) {
            WindowMode.SINGLE -> Snackbar.make(bindingExpanded.root, text, Snackbar.LENGTH_LONG).show()
            WindowMode.DUAL, WindowMode.TRIPLE -> Snackbar.make(bindingExpanded.root, text, Snackbar.LENGTH_LONG).setAnchorView(bindingControlPanelCompact.bottomNavigation).show()
            null -> Snackbar.make(bindingCompact.root, text, Snackbar.LENGTH_LONG).setAnchorView(bindingControlPanelCompact.bottomNavigation).show()
        }
    }
}
