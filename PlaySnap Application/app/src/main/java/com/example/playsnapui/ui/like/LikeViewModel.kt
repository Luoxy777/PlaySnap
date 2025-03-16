package com.example.playsnapui.ui.like

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playsnapui.data.Games
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LikeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val _likedGames = MutableLiveData<List<Games>>()
    val likedGames: LiveData<List<Games>> get() = _likedGames

//    fun fetchLikedGames() {
//        Log.d("User", "$userId")
//        if (userId != null) {
//            db.collection("social_interaction")
//                .whereEqualTo("user_ID", userId)
//                .whereEqualTo("like_status", true)
//                .get()
//                .addOnSuccessListener { documents ->
//                    val gamesList = mutableListOf<Games>()
//                    for (document in documents) {
//                        val gameId = document.getString("game_ID")
//                        if (gameId != "" && gameId != null) {
//                            Log.d("Fetch", "fetching data $gameId")
//                            // You need to fetch game details (like namaPermainan) using gameId
//                            fetchGameDetails(gameId, gamesList)
//                        }
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    Log.w("Firestore", "Error fetching bookmarked games", exception)
//                }
//        }
//    }
//
//    private fun fetchGameDetails(gameId: String, gamesList: MutableList<Games>) {
//        db.collection("games").document(gameId).get()
//            .addOnSuccessListener { document ->
//                val game = document.toObject(Games::class.java)
//                game?.let {
//                    gamesList.add(it)
//                    _likedGames.postValue(gamesList)
//                    Log.d("Haha", "test data ${game.game_id}")
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.w("Firestore", "Error fetching game details", exception)
//            }
//    }

    fun fetchLikedGames() {
        Log.d("User", "$userId")
        if (userId != null) {
            // Kosongkan daftar sebelum memulai pengambilan data
            _likedGames.postValue(emptyList())

            db.collection("social_interaction")
                .whereEqualTo("user_ID", userId)
                .whereEqualTo("like_status", true)
                .get()
                .addOnSuccessListener { documents ->
                    val gamesList = mutableListOf<Games>()

                    // Jika tidak ada game yang disukai, update LiveData menjadi emptyList
                    if (documents.isEmpty) {
                        Log.d("Fetch", "No liked games found")
                        _likedGames.postValue(emptyList())
                        return@addOnSuccessListener
                    }

                    var counter = documents.size()

                    for (document in documents) {
                        val gameId = document.getString("game_ID")
                        if (!gameId.isNullOrEmpty()) {
                            Log.d("Fetch", "Fetching data $gameId")

                            fetchGameDetails(gameId, gamesList) {
                                counter--
                                if (counter == 0) {
                                    _likedGames.postValue(gamesList)
                                }
                            }
                        } else {
                            counter--
                            if (counter == 0) {
                                _likedGames.postValue(gamesList)
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("Firestore", "Error fetching liked games", exception)
                    _likedGames.postValue(emptyList()) // Jika error, tetap update LiveData
                }
        } else {
            _likedGames.postValue(emptyList()) // Jika user tidak login, kosongkan daftar
        }
    }

    private fun fetchGameDetails(gameId: String, gamesList: MutableList<Games>, onComplete: () -> Unit) {
        db.collection("games").document(gameId).get()
            .addOnSuccessListener { document ->
                val game = document.toObject(Games::class.java)
                game?.let {
                    gamesList.add(it)
                }
                onComplete() // Pastikan counter tetap berkurang meskipun gagal
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error fetching game details", exception)
                onComplete()
            }
    }


}
