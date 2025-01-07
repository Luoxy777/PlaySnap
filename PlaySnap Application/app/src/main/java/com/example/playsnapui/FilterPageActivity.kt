package com.example.playsnapui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomsheet.BottomSheetBehavior

class FilterPageActivity : AppCompatActivity() {

    lateinit var titleFilterPage: TextView
    lateinit var subtitleFilterPage : TextView
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_filter_page)

            //initializing views
            titleFilterPage = findViewById(R.id.tv_filter_title)
            subtitleFilterPage = findViewById(R.id.tv_filter_subtitle)

        }
}