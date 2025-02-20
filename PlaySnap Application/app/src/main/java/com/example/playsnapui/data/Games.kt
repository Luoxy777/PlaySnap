package com.example.playsnapui.data

import android.os.Parcel
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
    val rating : Float = 0f,
    val totalLike : Int = 0,
    val totalShare : Int = 0,
    val game_id : String = ""
) : Parcelable {
    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(parcel: Parcel, p1: Int) {
        parcel.writeString(namaPermainan)
        parcel.writeString(deskripsi)
        parcel.writeInt(pemainMin)
        parcel.writeInt(pemainMax)
        parcel.writeString(jenisLokasi)
        parcel.writeInt(usiaMin)
        parcel.writeInt(usiaMax)
        parcel.writeString(properti)
        parcel.writeString(bahanProperti)
        parcel.writeString(step)
        parcel.writeString(tutorial)
        parcel.writeString(linkVideo)
        parcel.writeFloat(rating)
        parcel.writeInt(totalLike)
        parcel.writeInt(totalShare)
        parcel.writeString(game_id)
    }
    companion object CREATOR : Parcelable.Creator<Games> {
        override fun createFromParcel(parcel: Parcel): Games {
            return Games(
                namaPermainan = parcel.readString() ?: "",
                deskripsi = parcel.readString() ?: "",
                pemainMin = parcel.readInt(),
                pemainMax = parcel.readInt(),
                jenisLokasi = parcel.readString() ?: "",
                usiaMin = parcel.readInt(),
                usiaMax = parcel.readInt(),
                properti = parcel.readString() ?: "",
                bahanProperti = parcel.readString() ?: "",
                step = parcel.readString() ?: "",
                tutorial = parcel.readString() ?: "",
                linkVideo = parcel.readString() ?: "",
                rating = parcel.readFloat(),
                totalLike = parcel.readInt(),
                totalShare = parcel.readInt(),
                game_id = parcel.readString() ?: ""
            )
        }

        override fun newArray(size: Int): Array<Games?> {
            return arrayOfNulls(size)
        }
    }
}
