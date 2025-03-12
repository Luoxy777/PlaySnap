package com.example.playsnapui.ui.editProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditProfileViewModel : ViewModel() {

    // Example LiveData for profile data
    private val _profileData = MutableLiveData<Profile>()
    val profileData: LiveData<Profile> get() = _profileData

}

// Data class for Profile
data class Profile(
    val name: String,
    val username: String,
    val email: String,
    val gender: String
)
