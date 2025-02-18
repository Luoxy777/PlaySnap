package com.example.playsnapui.ui.gallery.scroll

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun startGame(imageUris: List<Uri>) {
        _loading.value = true
        _error.value = null

        viewModelScope.launch(Dispatchers.IO) { // Ensure network operations run on background thread
            try {
                val client = OkHttpClient()

                val multipartBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)

                for (uri in imageUris) {
                    val path = uri.path ?: continue
                    val file = File(path)
                    if (!file.exists()) {
                        _error.postValue("File does not exist: $path")
                        _loading.postValue(false)
                        return@launch
                    }
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    multipartBody.addFormDataPart("file", file.name, requestFile)
                }

                val request = Request.Builder()
                    .url("http://10.68.111.243:8000/detect/")
                    .post(multipartBody.build())
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    _success.postValue(true)
                } else {
                    _error.postValue("API request failed: ${response.message}")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            } finally {
                _loading.postValue(false)
            }
        }
    }
}
