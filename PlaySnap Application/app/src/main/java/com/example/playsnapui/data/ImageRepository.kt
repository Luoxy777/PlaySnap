package com.example.playsnapui.data

import DetectionResponse
import YOLOApiService
import android.util.Log
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ImageRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.68.111.243:8000")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(YOLOApiService::class.java)

    fun uploadImageToServer(imagePart: MultipartBody.Part) {
        val call = apiService.uploadImage(imagePart)

        call.enqueue(object : Callback<DetectionResponse> {
            override fun onResponse(call: Call<DetectionResponse>, response: Response<DetectionResponse>) {
                if (response.isSuccessful) {
                    val detections = response.body()?.detections
                    Log.d("API_SUCCESS", "Detections: $detections")
                } else {
                    Log.e("API_ERROR", "Failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<DetectionResponse>, t: Throwable) {
                Log.e("API_FAILURE", "Error: ${t.message}")
            }
        })
    }
}
