package com.example.playsnapui.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.playsnapui.data.UserRepository
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    fun register(fullName: String, email: String, username: String, password: String) {
        _registerState.value = RegisterState.Loading
        viewModelScope.launch {
            repository.registerUser(fullName, email, username, password)
                .onSuccess {
                    _registerState.update { RegisterState.Success }
                }
                .onFailure { error ->
                    _registerState.update { RegisterState.Error(error.message ?: "Registration failed") }
                }
        }
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}


