package com.example.playsnapui.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<Boolean>()
    val authState: LiveData<Boolean> get() = _authState

    init {
        // Check if user is already logged in
        _authState.value = firebaseAuth.currentUser != null
    }
}
