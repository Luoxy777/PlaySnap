import com.example.playsnapui.data.Games

// SharedData.kt
object SharedData {
    var detectedObjects: List<String> = emptyList()
    var recommendedGames: List<Games> = emptyList()
    var userProfile: UserProfile? = null
    var gameDetails: Games? = null
}