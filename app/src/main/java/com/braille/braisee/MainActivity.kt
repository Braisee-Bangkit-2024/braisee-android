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
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Konfigurasi AppBar dan BottomNavigationView
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_learn,
                R.id.navigation_favorite
            )
        )
        binding.navView.setupWithNavController(navController)

        // Set listener untuk BottomNavigationView
        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(
                        R.id.navigation_home, null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.navigation_analyze, true) // Hapus AnalyzeFragment dari stack
                            .build()
                    )
                    true
                }
                R.id.navigation_learn -> {
                    navController.navigate(R.id.navigation_learn)
                    true
                }
                R.id.navigation_favorite -> {
                    navController.navigate(R.id.navigation_favorite)
                    true
                }
                else -> false
            }
        }

        // Back button behavior
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
