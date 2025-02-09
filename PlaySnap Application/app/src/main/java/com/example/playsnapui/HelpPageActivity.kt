package com.example.playsnapui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.playsnapui.databinding.ActivityHelpPageBinding

class HelpPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpPageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}