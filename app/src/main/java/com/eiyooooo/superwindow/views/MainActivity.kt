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
import com.eiyooooo.superwindow.databinding.ControlPanelCompactBinding
import com.eiyooooo.superwindow.databinding.ControlPanelExpandedBinding
import com.eiyooooo.superwindow.viewmodels.MainActivityViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

class MainActivity : AppCompatActivity() {

    private var isExpanded: Boolean = false
    private lateinit var bindingCompact: ActivityMainCompactBinding
    internal lateinit var bindingExpanded: ActivityMainExpandedBinding

    private val controlPanelExpandedInitialized = MutableStateFlow(false)
    private lateinit var bindingControlPanelCompact: ControlPanelCompactBinding
    internal lateinit var bindingControlPanelExpanded: ControlPanelExpandedBinding

    private val mainModel: MainActivityViewModel by viewModels()
    private val widgetCardManager: WidgetCardManager by lazy { WidgetCardManager(this, mainModel) }

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
                    bindingControlPanelExpanded = ControlPanelExpandedBinding.inflate(layoutInflater, it.controlPanelCreator, true)
                    it.controlPanelCreator.post {
                        bindingExpanded.controlPanelCreator.removeAllViews()
                        controlPanelExpandedInitialized.update { true }
                    }
                    it.widgetContainer.addTargetView(it.leftSplitHandle)
                    it.widgetContainer.addTargetView(it.rightSplitHandle)
                    widgetCardManager.init()
                    setContentView(it.root)
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

    override fun onDestroy() {
        super.onDestroy()
        mainModel.removeShizukuListener()
    }

    private fun setupControlPanel() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        if (isExpanded) {
            bindingControlPanelExpanded.navigationRail.setupWithNavController(navHostFragment.navController)
            bindingControlPanelExpanded.bottomNavigation.setupWithNavController(navHostFragment.navController)
            setSupportActionBar(bindingControlPanelExpanded.toolbar)
        } else {
            bindingControlPanelCompact.bottomNavigation.setupWithNavController(navHostFragment.navController)
            setSupportActionBar(bindingControlPanelCompact.toolbar)
        }
    }

    suspend fun getControlPanelExpandedView(): View {
        controlPanelExpandedInitialized.filter { it }.first()
        return bindingControlPanelExpanded.root
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
}
