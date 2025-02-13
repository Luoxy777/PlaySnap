package com.example.playsnapui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.playsnapui.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Auth instance
        val auth = FirebaseAuth.getInstance()

        // Add a delay before navigation
        Handler(Looper.getMainLooper()).postDelayed({
//            if (auth.currentUser != null) {
                startActivity(Intent(this, HomeActivity::class.java))
//            } else {
//                startActivity(Intent(this, AuthActivity::class.java))
//            }
            finish() // Close SplashActivity
        }, 1500) // 1.5 seconds delay
    }
}

