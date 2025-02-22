package com.example.playsnapui.ui.editProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditProfileViewModel : ViewModel() {

    // Example LiveData for profile data
    private val _profileData = MutableLiveData<Profile>()
    val profileData: LiveData<Profile> get() = _profileData

    // Simulate fetching profile data (replace with your repository call)
    fun loadProfile() {
        // Fetch data from repository and post to LiveData
        _profileData.value = Profile("John Doe", "john_doe", "john@example.com", "Male")
    }

    // Example function to save changes (e.g., after editing profile)
    fun saveProfile(name: String, username: String, email: String, gender: String) {
        // Update profile data (you'd probably save this to a database or API)
        _profileData.value = Profile(name, username, email, gender)
    }
}

// Data class for Profile
data class Profile(
    val name: String,
    val username: String,
    val email: String,
    val gender: String
)
