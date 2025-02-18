package com.example.playsnapui.data.api

import com.google.gson.annotations.SerializedName

data class DetectionResponse(
    val detections: List<Detection>
)

data class Detection(
    @SerializedName("class_id") val classId: Int,
    val confidence: Float
)
