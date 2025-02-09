package com.example.playsnapui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.playsnapui.databinding.ActivityFilterPageBinding

class FilterPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFilterPageBinding
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         binding = ActivityFilterPageBinding.inflate(layoutInflater)
         setContentView(binding.root)

         binding.btnBack.setOnClickListener {
             onBackPressedDispatcher.onBackPressed()
         }
     }
}