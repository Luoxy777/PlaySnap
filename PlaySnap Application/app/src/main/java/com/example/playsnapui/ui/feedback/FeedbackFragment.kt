package com.example.playsnapui.ui.feedback

import SharedData
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.playsnapui.HomeActivity
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentFeedbackBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackFragment : Fragment() {

    private var _binding: FragmentFeedbackBinding? = null
    private val binding get() = _binding!!
    val gameDetails = SharedData.gameDetails

    private val viewModel: FeedbackViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textView2.text = "Have Fun, ${SharedData.userProfile!!.fullName}"
        binding.gameTitleFeedback.text = gameDetails!!.namaPermainan
        binding.gameDetailsFeedback.text = "${gameDetails!!.jenisLokasi}, ${gameDetails.usiaMin}-${gameDetails.usiaMax} tahun"
        binding.playerCountFeedbackNumber.text = "${gameDetails.pemainMin}-${gameDetails.pemainMax}"
        setupListener()
    }

    private fun setupListener() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.selesaiButtonFeedback.setOnClickListener {
            val newRating = binding.ratingLike.rating
            Log.d("Rating", "Rating baru: $newRating")

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val gameId = gameDetails!!.game_id

            val db = FirebaseFirestore.getInstance()
            val socialInteractionRef = db.collection("social_interaction")

            Log.d("User ID", "Id : $userId")
            Log.d("Gmae ID", "Id : $gameId")
            // Cari dokumen yang sesuai berdasarkan user_ID dan game_ID
            socialInteractionRef
                .whereEqualTo("user_ID", userId)
                .whereEqualTo("game_ID", gameId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        if (documents.isEmpty) {
                            Log.e("Firestore", "Dokumen tidak ditemukan untuk user_ID: $userId dan game_ID: $gameId")
                            return@addOnSuccessListener
                        }
                        Log.d("tes", "tes")
                        socialInteractionRef.document(document.id)
                            .update("rating", newRating)
                            .addOnSuccessListener {
                                Log.d("tes", "tes")
                                Log.d("Firestore2", "Rating berhasil diperbarui")
                                updateGameRating(gameId, newRating) // Update ke koleksi "games"
                                findNavController().navigate(R.id.action_FeedbackFragment_to_HomeFragment)
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Gagal memperbarui rating", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Gagal mendapatkan dokumen", e)
                }
            findNavController().navigate(R.id.action_FeedbackFragment_to_HomeFragment)
        }
    }


    private fun updateGameRating(gameId: String, newRating: Float) {
        val db = FirebaseFirestore.getInstance()
        val gameRef = db.collection("games").document(gameId)
        val socialInteractionRef = db.collection("social_interaction")

        db.collection("social_interaction")
            .whereEqualTo("game_ID", gameId)
            .whereGreaterThan("rating", 0)
            .get()
            .addOnSuccessListener { documents ->
                val totalCount = documents.size()// Hitung jumlah dokumen
                Log.d("Firestore2", "Total games rated: $totalCount")
                gameRef.get()
                    .addOnSuccessListener { gameSnapshot ->
                        if (gameSnapshot.exists()) {
                            val oldRating = gameSnapshot.getDouble("rating") ?: 0.0
                            Log.d("Firestore", "Old rating: $oldRating")

                            // **Hitung rating baru menggunakan rata-rata rating lama dan rating baru**
                            val newAvgRating = ((oldRating * (totalCount-1)) + newRating) / totalCount
                            Log.d("new rating", "old : $oldRating, totalcount : $totalCount, newrating : $newRating")

                            // **Update nilai rating di koleksi "games"**
                            gameRef.update("rating", newAvgRating)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Game rating updated successfully: $newAvgRating")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error updating game rating", e)
                                }
                        } else {
                            Log.e("Firestore", "Game document does not exist")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error fetching game rating", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting count", e)
            }

    }


    override fun onDestroyView() {
        super.onDestroyView()
//        (activity as? HomeActivity)?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.visibility = View.VISIBLE
        _binding = null
    }
}
