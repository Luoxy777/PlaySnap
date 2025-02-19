package com.example.playsnapui.ui.gallery.scroll

import DetectionResponse
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ScrollGalleryViewModel : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> get() = _success

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _detectedObjects = MutableLiveData<List<String>>() //Store unique detected objects
    val detectedObjects: LiveData<List<String>> get() = _detectedObjects

    private val client = OkHttpClient()

    fun startGame(imageUris: List<Uri>) {
        _loading.value = true
        _error.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                imageUris.forEach { uri ->
                    sendDetectionRequest(uri)
                }
                _success.postValue(true)
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            } finally {
                _loading.postValue(false)
            }
        }
    }

    private fun sendDetectionRequest(uri: Uri) {
        val path = uri.path ?: return
        val file = File(path)
        if (!file.exists()) {
            _error.postValue("File does not exist: $path")
            return
        }

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, requestFile)
            .build()

        val request = Request.Builder()
            .url("http://174.139.116.194:8000/detect/")
            .post(multipartBody)
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            val detectionResponse = Gson().fromJson(responseBody, DetectionResponse::class.java)

            // Append only new unique detections
            val updatedList = (_detectedObjects.value ?: emptyList()).toMutableList()
            for (detection in detectionResponse.detections) {
                if (!updatedList.contains(detection)) { // Ensure uniqueness
                    updatedList.add(detection)
                }
            }
            _detectedObjects.postValue(updatedList)
        } else {
            _error.postValue("API request failed: ${response.message}")
        }
    }
}
