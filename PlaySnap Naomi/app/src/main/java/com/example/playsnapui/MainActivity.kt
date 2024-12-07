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
    lateinit var button1 : Button
    lateinit var button2 : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button1 = findViewById(R.id.btn_login)
        button2 = findViewById(R.id.btn_signup)

        button1.setOnClickListener{
            val explicit = Intent(this, LoginActivity::class.java)

            startActivity(explicit)
        }

        button2.setOnClickListener{
            val explicit = Intent(this, RegisterActivity::class.java)

            startActivity(explicit)
        }

    }
}