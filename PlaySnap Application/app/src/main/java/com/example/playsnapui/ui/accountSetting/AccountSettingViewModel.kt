package com.example.playsnapui.ui.accountSetting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    // LiveData to hold account status or any other info you'd like to display
    private val _accountStatus = MutableLiveData<String>()
    val accountStatus: LiveData<String> get() = _accountStatus

    // Simulate sign-out logic
    fun signOut() {
        // Handle sign out logic, e.g., clearing session data
        _accountStatus.value = "Signed Out"
    }

    // Simulate account deletion logic
    fun deleteAccount() {
        // Handle delete account logic, e.g., calling an API or removing data
        _accountStatus.value = "Account Deleted"
    }
}
