package com.example.playsnapui.ui.help

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HelpFragmentReqViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()


    fun saveQuestionsToFirestore(text: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val currentTime = System.currentTimeMillis()
        val user = auth.currentUser
        // Format timestamp menjadi string yang lebih mudah dibaca
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val formattedTime = dateFormat.format(Date(currentTime))

        val reportData = hashMapOf(
            "userID" to user!!.uid,
            "questionsText" to text,
            "timestamp" to formattedTime  // Simpan waktu dalam format "dd/MM/yyyy HH:mm:ss"
        )

        viewModelScope.launch {
            db.collection("questions")
                .add(reportData)
                .addOnSuccessListener {onSuccess() }
                .addOnFailureListener { exception -> onFailure(exception) }
        }
    }

}