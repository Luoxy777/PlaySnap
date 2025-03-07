package com.example.playsnapui

import SharedData.userProfile
import UserProfile
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.playsnapui.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val deepLinkUri: Uri? = intent?.data
        Log.d("Full link", "$deepLinkUri")
        val gameId = deepLinkUri?.getQueryParameter("id")
        Log.d("You get the link", "$gameId")

        // Firebase Auth instance
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            val user = auth.currentUser
            user?.let {
                val userId = user.uid // Get UID of authenticated user
                val db = FirebaseFirestore.getInstance()
                // Fetch user data
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val email = document.getString("email")
                            val fullName = document.getString("fullName")
                            val username = document.getString("username")
                            val profilePicture = document.getString("profilePicture") ?: "https://drive.google.com/uc?export=view&id=1nY1KAhHQ4mDa-wFGAkg34dCRW8qM1qYH"
                            val gender = document.getString("gender") ?: "-"
                            // Store user profile globally
                            userProfile = UserProfile(email, fullName, username, profilePicture, gender)
                            Log.d("Splash", "User Profile Loaded: ${userProfile?.fullName ?: "N/A"}")

                            // ✅ Navigate **after** user profile is set
                            if(gameId!=null){
                                Log.d("You get the link splash2", "$gameId haha")
                                val intent = Intent(this, HomeActivity::class.java)
                                gameId.let { intent.putExtra("gameId", it) }
                                startActivity(intent)
                                finish()
                            }else{
                                startActivity(Intent(this, HomeActivity::class.java))
                                finish()
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("Error", "Error getting document: ", exception)
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
            }
        } else {
            val intent = Intent(this, AuthActivity::class.java)
            gameId?.let { intent.putExtra("gameId", it) }
            startActivity(intent)
            finish()
        }
    }
}

