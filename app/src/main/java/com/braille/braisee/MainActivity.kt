package com.braille.braisee

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.braille.braisee.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up the navigation with the bottom navigation view
        AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_learn, R.id.navigation_favorite)
        )
        binding.navView.setupWithNavController(navController)

        // Add a listener to change the action bar visibility based on the destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_analyze -> {
                    supportActionBar?.hide() // Hide the action bar for AnalyzeFragment
                    binding.navView.visibility = View.GONE // Optionally hide the bottom nav bar
                }
                else -> {
                    supportActionBar?.show() // Show the action bar for other fragments
                    binding.navView.visibility = View.VISIBLE // Show the bottom nav bar for other fragments
                }
            }
        }

        // Handle the back button behavior
        onBackPressedDispatcher.addCallback(this) {
            if (navController.currentDestination?.id == R.id.navigation_home) {
                showExitConfirmationDialog()
            } else {
                navController.navigateUp()
            }
        }
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this).apply {
            setMessage(R.string.exit_confirm)
            setPositiveButton(R.string.ya) { _, _ -> finish() }
            setNegativeButton(R.string.tidak, null)
            create()
            show()
        }
    }
}
