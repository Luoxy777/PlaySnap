package com.example.playsnapui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.playsnapui.databinding.ActivityProfilePageBinding

class ProfilePageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfilePageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.include.bottomNavigationView.selectedItemId = R.id.profile


        binding.include.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> startActivity(Intent(this, HomePageActivity::class.java))
                R.id.like -> startActivity(Intent(this, LikePageActivity::class.java))
                R.id.bookmark -> startActivity(Intent(this, BookmarkPageActivity::class.java))
            }
            true
        }

        binding.btnReport.setOnClickListener {
            val intent = Intent(this, ReportPageActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        // Reset navbar selection using View Binding
        binding.include.bottomNavigationView.menu.findItem(R.id.profile).isChecked = true
    }

}