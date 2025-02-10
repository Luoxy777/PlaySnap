package com.example.playsnapui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomsheet.BottomSheetBehavior

class BottomSheet : AppCompatActivity() {
    lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bottom_sheet_filter_page)

        //initializing views
//        val bottomSheet: FrameLayout = findViewById(R.id.bottom_sheet)
//        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply{
//            peekHeight = 200
//            this.state = BottomSheetBehavior.STATE_COLLAPSED
//        }
//
//        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
//            override fun onStateChanged(bottomSheet: View, newState: Int) {
//                when (newState) {
//                    BottomSheetBehavior.STATE_EXPANDED -> println("STATE_EXPANDED")
//                    BottomSheetBehavior.STATE_COLLAPSED -> println("STATE_COLLAPSED")
//                    BottomSheetBehavior.STATE_HIDDEN -> println("STATE_HIDDEN")
//                }
//            }
//
//            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                println("Slide offset: $slideOffset")
//            }
//        })

    }

}