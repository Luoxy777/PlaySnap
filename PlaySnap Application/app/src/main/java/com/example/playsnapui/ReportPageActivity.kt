package com.example.playsnapui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.playsnapui.databinding.ActivityReportPageBinding

class ReportPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportPageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}