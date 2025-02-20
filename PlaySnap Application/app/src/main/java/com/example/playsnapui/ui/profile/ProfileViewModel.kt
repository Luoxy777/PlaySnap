package com.example.playsnapui.ui.profile

import UserProfile
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    // Fetch user profile data from SharedData or any other data source
    fun getUserProfile(): UserProfile? {
        return SharedData.userProfile
    }
}
