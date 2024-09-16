package com.easycontrol.next.views

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.easycontrol.next.R
import com.easycontrol.next.databinding.ActivityMainBinding
import com.easycontrol.next.viewmodels.MainActivityViewModel
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val mainModel: MainActivityViewModel by viewModels() //TODO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.contentMain.bottomNavigation?.let {
            it.setupWithNavController(navController)
            setSupportActionBar(binding.contentMain.toolbar)
        } ?: binding.contentMain.navigationRail?.setupWithNavController(navController)
    }

    internal fun showSnackBar(text: String) {
        Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG).setAnchorView(binding.contentMain.bottomNavigation).show()
    }
}
