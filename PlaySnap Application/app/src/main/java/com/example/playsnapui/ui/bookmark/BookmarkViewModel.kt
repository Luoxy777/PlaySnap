package com.example.playsnapui.ui.bookmark

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playsnapui.data.Games
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class BookmarkViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val _bookmarkedGames = MutableLiveData<List<Games>>()
    val bookmarkedGames: LiveData<List<Games>> get() = _bookmarkedGames

    fun fetchBookmarkedGames() {
        Log.d("User", "$userId")
        if (userId != null) {
            db.collection("social_interaction")
                .whereEqualTo("user_ID", userId)
                .whereEqualTo("bookmark_status", true)
                .get()
                .addOnSuccessListener { documents ->
                    val gamesList = mutableListOf<Games>()

                    if (documents.isEmpty) {
                        _bookmarkedGames.postValue(emptyList()) // Set UI ke kosong jika tidak ada bookmark
                        return@addOnSuccessListener
                    }

                    var fetchCount = 0
                    val totalGames = documents.size() // Jumlah game yang harus di-fetch

                    for (document in documents) {
                        val gameId = document.getString("game_ID")
                        if (!gameId.isNullOrEmpty()) {
                            Log.d("Fetch", "Fetching data $gameId")
                            fetchGameDetails(gameId, gamesList) {
                                fetchCount++
                                if (fetchCount == totalGames) {
                                    _bookmarkedGames.postValue(gamesList) // Update UI hanya setelah semua selesai
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("Firestore", "Error fetching bookmarked games", exception)
                    _bookmarkedGames.postValue(emptyList()) // Handle error dengan mengosongkan UI
                }
        }
    }

    private fun fetchGameDetails(gameId: String, gamesList: MutableList<Games>, onComplete: () -> Unit) {
        db.collection("games").document(gameId).get()
            .addOnSuccessListener { document ->
                val game = document.toObject(Games::class.java)
                game?.let {
                    gamesList.add(it)
                    Log.d("Haha", "Fetched game data: ${game.game_id}")
                }
                onComplete() // Tandai bahwa fetch satu game selesai
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error fetching game details", exception)
                onComplete()
            }
    }
}
