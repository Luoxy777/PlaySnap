// UserProfile.kt
data class UserProfile(
    var email: String?,
    var fullName: String?,
    var username: String?,
    var profilePicture: String? = "https://drive.google.com/uc?export=view&id=1nY1KAhHQ4mDa-wFGAkg34dCRW8qM1qYH",
    var gender: String? = "Cute"
)
