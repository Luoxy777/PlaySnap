package com.example.app.ui.gallery.swipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SwipeGalleryViewModel @Inject constructor() : ViewModel() {

    private val _images = MutableLiveData<List<String>>() // Replace with actual image URLs or resource IDs
    val images: LiveData<List<String>> get() = _images

//    init {
//        loadImages()
//    }
//
//    private fun loadImages() {
//        _images.value = listOf(
//            "image_url_1",
//            "image_url_2",
//            "image_url_3"
//        ) // Replace with actual image resources
//    }
}
