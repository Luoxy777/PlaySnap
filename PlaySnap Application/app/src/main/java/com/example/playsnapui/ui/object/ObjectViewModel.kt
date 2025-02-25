package com.example.playsnapui.ui.`object`

import SharedData
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playsnapui.data.Games
import com.google.firebase.firestore.FirebaseFirestore

class ObjectViewModel : ViewModel() {

    private val _objects = MutableLiveData<List<String>>()
    val objects: LiveData<List<String>> get() = _objects
    private val db = FirebaseFirestore.getInstance()
    private val _recommendedGames = MutableLiveData<List<Games>>()
    val recommendedGames: LiveData<List<Games>> get() = _recommendedGames

    fun getRecommendedGames(detectedObjects: List<String>) {
        val recommendedList = mutableListOf<Games>()

        // Fetch games from Firestore
        db.collection("games")  // Assuming 'games' is your collection name in Firestore
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val game = document.toObject(Games::class.java)
                    // Check if the game properties match detected objects
                    println("Game: ${game.properti}")
                    for (detectedObject in detectedObjects) {
                        println("Game properti = ${game.properti} and object = $detectedObject")
                        val alreadyExists = recommendedList.any { existingGame ->
                            existingGame.namaPermainan == game.namaPermainan
                        }
                        if (game.properti.contains(detectedObject, ignoreCase = true) && !alreadyExists) {
                            recommendedList.add(game)
                        }
                    }
                }
                _recommendedGames.value = recommendedList  // Update the LiveData
            }
            .addOnFailureListener { exception ->
                Log.e("ObjectViewModel", "Error getting documents: ", exception)
            }
    }

    fun setDetectedObjects(detectedObjects: List<String>) {
        println(detectedObjects.toString())
        _objects.value = detectedObjects.toList()
    }

    fun addObject(newObject: String) {
        // Get the current list (null check to avoid crashes if _objects is null)
        val currentList = _objects.value.orEmpty()

        // Create a new list with the new object added
        val updatedList = currentList.toMutableList().apply {
            add(newObject)
        }

        // Update the LiveData with the new list
        _objects.value = updatedList
    }

    fun removeObjectAt(position: Int) {
        _objects.value = _objects.value?.toMutableList()?.apply {
            if (position in indices) removeAt(position)
        }
    }
}
