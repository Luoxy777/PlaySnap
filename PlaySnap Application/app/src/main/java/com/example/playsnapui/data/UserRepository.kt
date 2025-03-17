package com.example.playsnapui.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun registerUser(fullName: String, email: String, username: String, password: String, photoProfile: String): Result<Unit> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return Result.failure(Exception("User ID is null"))

            // Store additional user details in Firestore
            val userMap = hashMapOf(
                "fullName" to fullName,
                "email" to email,
                "username" to username,
                "profilePicture" to photoProfile
            )
            firestore.collection("users").document(userId).set(userMap).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
