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
import com.eiyooooo.superwindow.adapters.WidgetCardAdapter
import com.eiyooooo.superwindow.databinding.ActivityMainCompactBinding
import com.eiyooooo.superwindow.databinding.ActivityMainExpandedBinding
import com.eiyooooo.superwindow.databinding.ControlPanelBinding
import com.eiyooooo.superwindow.entities.WindowMode
import com.eiyooooo.superwindow.viewmodels.MainActivityViewModel
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var windowMode: WindowMode
    private lateinit var bindingCompact: ActivityMainCompactBinding
    private lateinit var bindingExpanded: ActivityMainExpandedBinding

    private lateinit var bindingControlPanel: ControlPanelBinding

    private val mainModel: MainActivityViewModel by viewModels()
    private val widgetCardAdapter: WidgetCardAdapter by lazy { WidgetCardAdapter(this, mainModel) }

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
                    bindingControlPanel = ControlPanelBinding.inflate(layoutInflater, null, false)
                    setContentView(it.root)
                }
                WindowMode.EXPANDED
            }
        }

        if (this::bindingControlPanel.isInitialized) {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            bindingControlPanel.bottomNavigation.setupWithNavController(navController)
            setSupportActionBar(bindingControlPanel.toolbar)
        }
    }

    fun getControlPanelView(): View {
        return bindingControlPanel.root
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
            WindowMode.EXPANDED -> Snackbar.make(bindingExpanded.root, text, Snackbar.LENGTH_LONG).setAnchorView(bindingControlPanel.bottomNavigation).show()
        }
    }
}
