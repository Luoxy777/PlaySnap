package com.example.playsnapui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    lateinit var buttonBack : AppCompatButton
    lateinit var textSelamat : TextView
    lateinit var subText : TextView
    lateinit var imageOrang : ImageView
    lateinit var imageGembok : ImageView
    lateinit var email : TextInputEditText
    lateinit var password : TextInputEditText
    lateinit var lupaSandi : TextView
    lateinit var buttonMasuk : AppCompatButton
    lateinit var daftar : TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        buttonBack = findViewById(R.id.btn_back)
        textSelamat = findViewById(R.id.tv_login_title)
        subText = findViewById(R.id.tv_login_subtitle)
        imageOrang = findViewById(R.id.image_icon_account)
        imageGembok = findViewById(R.id.ic_email)
        email = findViewById(R.id.et_email)
        password = findViewById(R.id.et_password)
        lupaSandi = findViewById(R.id.tv_forgot_pw)
        buttonMasuk = findViewById(R.id.masukButton)
        daftar = findViewById(R.id.tv_havent_account)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }
}