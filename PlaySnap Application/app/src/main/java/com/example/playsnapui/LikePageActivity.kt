package com.example.playsnapui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.playsnapui.databinding.ActivityBookmarkPageBinding
import com.example.playsnapui.databinding.ActivityLikePageBinding

class LikePageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLikePageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLikePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.include.bottomNavigationView.selectedItemId = R.id.like

        binding.include.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> startActivity(Intent(this, HomePageActivity::class.java))
                R.id.bookmark -> startActivity(Intent(this, BookmarkPageActivity::class.java))
                R.id.profile -> startActivity(Intent(this, ProfilePageActivity::class.java))
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()

        // Reset navbar selection using View Binding
        binding.include.bottomNavigationView.menu.findItem(R.id.like).isChecked = true
    }
}