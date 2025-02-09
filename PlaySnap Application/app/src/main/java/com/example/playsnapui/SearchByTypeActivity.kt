package com.example.playsnapui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.playsnapui.databinding.ActivityHomePageBinding
import com.example.playsnapui.databinding.ActivitySearchObjectByTypePageBinding

class SearchByTypeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchObjectByTypePageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchObjectByTypePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }


}