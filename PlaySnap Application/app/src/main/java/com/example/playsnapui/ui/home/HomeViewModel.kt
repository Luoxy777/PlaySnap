package com.example.playsnapui.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    private val _welcomeMessage = MutableLiveData("Totok!")
    val welcomeMessage: LiveData<String> get() = _welcomeMessage

}
