package com.example.playsnapui.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()


    fun saveReportToFirestore(text: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val currentTime = System.currentTimeMillis()
        val user = auth.currentUser
        // Format timestamp menjadi string yang lebih mudah dibaca
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val formattedTime = dateFormat.format(Date(currentTime))

        val reportData = hashMapOf(
            "userID" to user!!.uid,
            "reportText" to text,
            "timestamp" to formattedTime  // Simpan waktu dalam format "dd/MM/yyyy HH:mm:ss"
        )

        viewModelScope.launch {

            db.collection("reports")
                .add(reportData)
                .addOnSuccessListener {onSuccess() }
                .addOnFailureListener { exception -> onFailure(exception) }
        }
    }
}