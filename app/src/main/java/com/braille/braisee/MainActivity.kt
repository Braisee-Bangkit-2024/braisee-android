package com.braille.braisee

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.braille.braisee.databinding.ActivityMainBinding
import com.braille.braisee.ui.favorite.FavoriteFragment
import com.braille.braisee.ui.home.HomeFragment
import com.braille.braisee.ui.learn.LearnFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var activeFragmentTag: String = "HomeFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, HomeFragment(), "HomeFragment")
                .commit()
        }

        binding.navView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.navigation_home -> {
                    switchFragment(HomeFragment(), "HomeFragment")
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_learn -> {
                    switchFragment(LearnFragment(), "LearnFragment")
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_history -> {
                    switchFragment(FavoriteFragment(), "FavoriteFragment")
                    return@setOnItemSelectedListener true
                }
                else -> false
            }
        }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (activeFragmentTag != "HomeFragment") {
                    binding.navView.selectedItemId = R.id.navigation_home
                    switchFragment(HomeFragment(), "HomeFragment")
                } else {
                    showExitConfirmationDialog()
                }
            }
        })
    }

    private fun switchFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        if (tag == activeFragmentTag) return

        supportFragmentManager.beginTransaction()
            .replace(R.id.main, fragment, tag)
            .commit()
        activeFragmentTag = tag
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
