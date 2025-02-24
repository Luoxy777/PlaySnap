package com.example.playsnapui.ui.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playsnapui.data.Games
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val _visitedGames = MutableLiveData<List<Games>>()
    val visitedGames: LiveData<List<Games>> get() = _visitedGames

    fun fetchVisitedGames() {
        Log.d("User", "$userId")
        if (userId != null) {
            db.collection("social_interaction")
                .whereEqualTo("user_ID", userId)
                .get()
                .addOnSuccessListener { documents ->
                    val gamesList = mutableListOf<Games>()
                    for (document in documents) {
                        val gameId = document.getString("game_ID")
                        val dateVisit = document.getString("date_visit")
                        if (gameId != "" && gameId != null && dateVisit != null && dateVisit != "") {
                            Log.d("Fetch", "fetching data $gameId")
                            // You need to fetch game details (like namaPermainan) using gameId
                            fetchGameDetails(gameId, gamesList)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("Firestore", "Error fetching bookmarked games", exception)
                }
        }
    }

    private fun fetchGameDetails(gameId: String, gamesList: MutableList<Games>) {
        db.collection("games").document(gameId).get()
            .addOnSuccessListener { document ->
                val game = document.toObject(Games::class.java)
                game?.let {
                    gamesList.add(it)
                    _visitedGames.postValue(gamesList)
                    Log.d("Haha", "test data ${game.game_id}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error fetching game details", exception)
            }
    }

}
