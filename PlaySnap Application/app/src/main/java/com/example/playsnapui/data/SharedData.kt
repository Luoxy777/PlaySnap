import com.example.playsnapui.data.Games

// SharedData.kt
object SharedData {
    var totalGamesCount : Int = 0
    var isObject : Boolean = false
    var detectedObjects: List<String> = emptyList()
    var recommendedGames: List<Games> = emptyList()
    var userProfile: UserProfile? = null
    var gameDetails: Games? = null
    var propertyContainer : String = ""
//    var batasUsia1 : Int = 0
//    var batasUsia2Bawah : Int = 0
//    var batasUsia2Atas : Int = 0
//    var batasUsia3 : Int = 0
    var batasUsiaBawah : Int = 0
    var batasUsiaAtas : Int = 0
    var batasPemain1 : Int = 0
    var batasPemain2Bawah : Int = 0
    var batasPemain2Atas : Int = 0
    var batasPemain3 : Int = 0
    var lokasiContainer : String = ""
    var sharedLinks : String = ""
}