// UserProfile.kt
data class UserProfile(
    var email: String?,
    var fullName: String?,
    var username: String?,
    var profilePicture: String? = "https://drive.google.com/uc?export=view&id=1zXG6XHvZmcGaX8G7idtDqu_JO2zDwkt7",
    var gender: String? = "Cute"
)
