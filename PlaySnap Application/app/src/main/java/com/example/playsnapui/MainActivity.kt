package com.example.playsnapui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import org.w3c.dom.Text
import java.text.DateFormat

class MainActivity : AppCompatActivity() {
    lateinit var startTitle : TextView
    lateinit var startSub : TextView
    lateinit var logo : ImageView
    lateinit var cloud : ImageView
    lateinit var masuk : AppCompatButton
    lateinit var daftar : AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startTitle = findViewById(R.id.started_title)
        startSub = findViewById(R.id.started_subtitle)
        logo = findViewById(R.id.image_illustration_started)
        cloud = findViewById(R.id.image_ilustration_cloud)
        masuk = findViewById(R.id.btn_login)
        daftar = findViewById(R.id.btn_signup)

        masuk.setOnClickListener {
            val explicit1 = Intent(this, LoginActivity::class.java)

            startActivity(explicit1)
        }

        daftar.setOnClickListener {
            val explicit2 = Intent(this, RegisterActivity::class.java)

            startActivity(explicit2)
        }

    }
}