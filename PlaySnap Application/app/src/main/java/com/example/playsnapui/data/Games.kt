package com.example.playsnapui.data

import android.os.Parcelable

data class Games(
    val namaPermainan : String = "",
    val deskripsi : String = "",
    val pemainMin : Int = 0,
    val pemainMax : Int = 0,
    val jenisLokasi : String = "",
    val usiaMin : Int = 0,
    val usiaMax : Int = 0,
    val properti : String = "",
    val bahanProperti : String = "",
    val step : String = "",
    val tutorial : String = "",
    val linkVideo : String = "",
    val rating : Float,
    val totalLike : Int = 0,
    val totalShare : Int = 0
)
