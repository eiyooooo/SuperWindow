package com.easycontrol.next.views

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.easycontrol.next.R
import com.easycontrol.next.databinding.ActivityMainCompactBinding
import com.easycontrol.next.databinding.ActivityMainExpandedBinding
import com.easycontrol.next.databinding.ActivityMainMediumBinding
import com.easycontrol.next.databinding.ControlPanelBinding
import com.easycontrol.next.entities.WindowMode
import com.easycontrol.next.viewmodels.MainActivityViewModel
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var windowMode: WindowMode
    private lateinit var bindingCompact: ActivityMainCompactBinding
    private lateinit var bindingMedium: ActivityMainMediumBinding
    private lateinit var bindingExpanded: ActivityMainExpandedBinding

    private lateinit var bindingControlPanel: ControlPanelBinding

    private val mainModel: MainActivityViewModel by viewModels() //TODO

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

            in 600..839 -> {
                bindingMedium = ActivityMainMediumBinding.inflate(layoutInflater).also {
                    //TODO
                    setContentView(it.root)
                }
                WindowMode.MEDIUM
            }

            else -> {
                bindingExpanded = ActivityMainExpandedBinding.inflate(layoutInflater).also {
                    //TODO
                    setContentView(it.root)
                }
                WindowMode.EXPANDED
            }
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        bindingControlPanel.bottomNavigation.setupWithNavController(navController)
        setSupportActionBar(bindingControlPanel.toolbar)
    }

    internal fun showSnackBar(text: String) {
        when (windowMode) {
            WindowMode.COMPACT -> Snackbar.make(bindingCompact.root, text, Snackbar.LENGTH_LONG).setAnchorView(bindingControlPanel.bottomNavigation).show()
            WindowMode.MEDIUM -> Snackbar.make(bindingMedium.root, text, Snackbar.LENGTH_LONG).setAnchorView(bindingControlPanel.bottomNavigation).show()
            WindowMode.EXPANDED -> Snackbar.make(bindingExpanded.root, text, Snackbar.LENGTH_LONG).setAnchorView(bindingControlPanel.bottomNavigation).show()
        }
    }
}
