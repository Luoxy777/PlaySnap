package com.example.playsnapui.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playsnapui.data.Games
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private val _gameStatus = MutableLiveData<MutableMap<String, GameStatus>>()
    val gameStatus: LiveData<MutableMap<String, GameStatus>> get() = _gameStatus

    init {
        _gameStatus.value = mutableMapOf()
    }

    data class GameStatus(
        var isBookmarked: Boolean = false,
        var isLiked: Boolean = false,
        var likeCount: Int = 0
    )

    fun getGameStatus(gameId: String): LiveData<GameStatus> {
        val result = MutableLiveData<GameStatus>()

        db.collection("social_interaction")
            .whereEqualTo("user_ID", userId)
            .whereEqualTo("game_ID", gameId)
            .get()
            .addOnSuccessListener { documents ->
                val status = GameStatus()
                if (!documents.isEmpty) {
                    val doc = documents.first()
                    status.isBookmarked = doc.getBoolean("bookmark_status") ?: false
                    status.isLiked = doc.getBoolean("like_status") ?: false
                }
                result.postValue(status)
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error fetching game status", exception)
            }

        return result
    }


    fun toggleBookmark(game: Games) {
        if (userId == null) return
        val currentStatus = _gameStatus.value?.get(game.game_id) ?: GameStatus()
        val newBookmarkStatus = !currentStatus.isBookmarked

        db.collection("social_interaction")
            .whereEqualTo("user_ID", userId)
            .whereEqualTo("game_ID", game.game_id)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val newEntry = hashMapOf(
                        "user_ID" to userId,
                        "game_ID" to game.game_id,
                        "bookmark_status" to newBookmarkStatus,
                        "like_status" to false,
                        "rating" to 0
                    )
                    db.collection("social_interaction").add(newEntry)
                } else {
                    db.collection("social_interaction")
                        .document(documents.first().id)
                        .update("bookmark_status", newBookmarkStatus)
                }
                currentStatus.isBookmarked = newBookmarkStatus
                _gameStatus.value?.set(game.game_id, currentStatus)
                _gameStatus.postValue(_gameStatus.value)
            }
    }

    fun toggleLike(game: Games) {
        if (userId == null) return
        val currentStatus = _gameStatus.value?.get(game.game_id) ?: GameStatus()
        val newLikeStatus = !currentStatus.isLiked
        val newLikeCount = if (newLikeStatus) currentStatus.likeCount + 1 else currentStatus.likeCount - 1

        db.collection("social_interaction")
            .whereEqualTo("user_ID", userId)
            .whereEqualTo("game_ID", game.game_id)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val newEntry = hashMapOf(
                        "user_ID" to userId,
                        "game_ID" to game.game_id,
                        "bookmark_status" to false,
                        "like_status" to newLikeStatus,
                        "rating" to 0
                    )
                    db.collection("social_interaction").add(newEntry)
                } else {
                    db.collection("social_interaction")
                        .document(documents.first().id)
                        .update("like_status", newLikeStatus)
                }
                db.collection("games")
                    .document(game.game_id)
                    .update("totalLike", newLikeCount)
                currentStatus.isLiked = newLikeStatus
                currentStatus.likeCount = newLikeCount
                _gameStatus.value?.set(game.game_id, currentStatus)
                _gameStatus.postValue(_gameStatus.value)
            }
    }
}