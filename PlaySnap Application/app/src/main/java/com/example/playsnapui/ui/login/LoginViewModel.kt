package com.example.playsnapui.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> get() = _password

    private val _loginState = MutableLiveData<Boolean>()
    val loginState: LiveData<Boolean> get() = _loginState

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun onEmailChanged(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChanged(newPassword: String) {
        _password.value = newPassword
    }

    fun login() {
        val email = _email.value.orEmpty()
        val password = _password.value.orEmpty()

        if (email.isEmpty() || password.isEmpty()) {
            _loginState.value = false
            return
        }

        viewModelScope.launch {
            try {
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                _loginState.value = result.user != null
            } catch (e: Exception) {
                _loginState.value = false
            }
        }
    }
}
