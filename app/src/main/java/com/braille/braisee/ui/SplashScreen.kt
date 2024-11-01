package com.braille.braisee.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.braille.braisee.MainActivity
import com.braille.braisee.R

class Splashscreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@Splashscreen, MainActivity::class.java))
            finish()
        }, 2500)
    }
}