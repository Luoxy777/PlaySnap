package com.example.playsnapui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.playsnapui.databinding.ActivityHomePageBinding

class HomePageActivity : AppCompatActivity() {

    lateinit var binding: ActivityHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.include.bottomNavigationView.selectedItemId = R.id.home

        binding.btnScanObject.setOnClickListener{
            val explicit = Intent(this, SearchByCameraActivity::class.java)

            startActivity(explicit)
        }

        binding.btnFilterGame.setOnClickListener{
            val explicit = Intent(this, FilterPageActivity::class.java)

            startActivity(explicit)
        }

        binding.btnTypeObject.setOnClickListener{
            val explicit = Intent(this, SearchByTypeActivity::class.java)

            startActivity(explicit)
        }

        binding.helpButton.setOnClickListener{
            val intent = Intent(this, HelpPageActivity::class.java)
            startActivity(intent)
        }

        binding.include.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.like -> startActivity(Intent(this, LikePageActivity::class.java))
                R.id.bookmark -> startActivity(Intent(this, BookmarkPageActivity::class.java))
                R.id.profile -> startActivity(Intent(this, ProfilePageActivity::class.java))
            }
            true
        }


    }

    override fun onResume() {
        super.onResume()

        // Reset navbar selection using View Binding
        binding.include.bottomNavigationView.menu.findItem(R.id.home).isChecked = true
    }

}