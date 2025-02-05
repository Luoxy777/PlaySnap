package com.example.playsnapui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
    lateinit var daftarNonClickable : TextView
    lateinit var daftarClickable : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        buttonBack = findViewById(R.id.btn_back)
        textSelamat = findViewById(R.id.tv_login_title)
        subText = findViewById(R.id.tv_login_subtitle)
        imageOrang = findViewById(R.id.image_icon_account)
        imageGembok = findViewById(R.id.ic_email)
        email = findViewById(R.id.et_email)
        password = findViewById(R.id.et_password)
        lupaSandi = findViewById(R.id.tv_forgot_pw)
        buttonMasuk = findViewById(R.id.masukButton)
        daftarNonClickable = findViewById(R.id.tv_havent_account)
        daftarClickable = findViewById(R.id.daftar)

        buttonBack.setOnClickListener {
            val explicit1 = Intent(this, MainActivity::class.java)

            startActivity(explicit1)
            finish()
        }

        daftarClickable.setOnClickListener{
            val explicit2 = Intent(this, RegisterActivity::class.java)

            startActivity(explicit2)
            finish()
        }

        buttonMasuk.setOnClickListener{
            val email = email.text.toString()
            val password = password.text.toString()
            if(email.isNotEmpty() && password.isNotEmpty()){
                MainActivity.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                    if(it.isSuccessful){
                        val explicit2 = Intent(this, HomePageActivity::class.java)
                        startActivity(explicit2)
                        finish()
                    }
                }.addOnFailureListener{
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}