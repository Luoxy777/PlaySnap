// UserProfile.kt
data class UserProfile(
    val email: String?,
    val fullName: String?,
    val username: String?,
    var profilePicture: String? = "https://drive.google.com/uc?export=view&id=1nY1KAhHQ4mDa-wFGAkg34dCRW8qM1qYH",
    val gender: String? = "Cute"
)
