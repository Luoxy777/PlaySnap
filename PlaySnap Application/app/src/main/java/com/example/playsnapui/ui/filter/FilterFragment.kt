package com.example.playsnapui.ui.filter

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.playsnapui.R
import com.example.playsnapui.databinding.BottomSheetFilterPageBinding
import com.example.playsnapui.databinding.FragmentFilterBinding
import com.example.playsnapui.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import androidx.lifecycle.lifecycleScope
import com.example.playsnapui.data.Games
import kotlinx.coroutines.launch


class FilterFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private var bottomSheetBinding: BottomSheetFilterPageBinding? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)

        // INI PERBAIKANNYA: Inflate BottomSheet secara mandiri
        bottomSheetBinding = BottomSheetFilterPageBinding.inflate(inflater, binding.bottomSheetFilterPage, true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var usiaMinContainer : Int = 0
        var usiaMaxContainer : Int = 0
        var lokasiContainer : String = ""
        var pemainMinContainer : Int = 0
        var pemainMaxContainer : Int = 0
        var propertiContainer : String = ""

        binding!!.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_FilterFragment_to_HomeFragment)
        }

        bottomSheetBinding?.let { sheetBinding ->
            // Set listener untuk tombol usia
            sheetBinding.usiaOpt1Btn.setOnClickListener {
                binding.usiaValue.text = "<6 th"
                usiaMinContainer = 6
                usiaMaxContainer = 0
            }
            sheetBinding.usiaOpt2Btn.setOnClickListener {
                binding.usiaValue.text = "6 - 10 th"
                usiaMinContainer = 6
                usiaMaxContainer = 10
            }
            sheetBinding.usiaOpt3Btn.setOnClickListener {
                binding.usiaValue.text = ">10 th"
                usiaMinContainer = 0
                usiaMaxContainer = 10
            }

            // Set listener untuk tombol lokasi
            sheetBinding.lokasiOpt1Btn.setOnClickListener {
                binding.lokasiValue.text = "Indoor"
                lokasiContainer = "Indoor"
            }
            sheetBinding.lokasiOpt2Btn.setOnClickListener {
                binding.lokasiValue.text = "Outdoor"
                lokasiContainer = "Outdoor"
            }

            // Set listener untuk tombol jumlah pemain
            sheetBinding.pemainOpt1Btn.setOnClickListener {
                binding.pemainValue.text = "<3 org"
                pemainMaxContainer = 2
            }
            sheetBinding.pemainOpt2Btn.setOnClickListener {
                binding.pemainValue.text = "3 - 5 org"
                pemainMinContainer = 3
                pemainMaxContainer = 5
            }
            sheetBinding.pemainOpt3Btn.setOnClickListener {
                binding.pemainValue.text = ">5 org"
                pemainMinContainer = 6
            }

            // Set listener untuk tombol properti
            sheetBinding.propertiOpt1Btn.setOnClickListener {
                binding.propertiValue.text = "Ya"
                propertiContainer = "Ya"
            }
            sheetBinding.propertiOpt2Btn.setOnClickListener {
                binding.propertiValue.text = "Tidak"
                propertiContainer = "Tidak"
            }
        }

        compareGamesWithDatabase(usiaMinContainer, usiaMaxContainer, lokasiContainer, pemainMinContainer, pemainMaxContainer, propertiContainer)

    }

    private fun compareGamesWithDatabase(usiaMinContainer : Int, usiaMaxContainer : Int, lokasiContainer : String, pemainMinContainer : Int, pemainMaxContainer : Int, propertiContainer : String){
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val gamesSnapshot = db.collection("games").get().await()
                val matchingGames = mutableListOf<Games>()

                for (document in gamesSnapshot.documents) {
                    val game = document.toObject(Games::class.java)  // 🔥 Konversi langsung ke objek Game

                    if (game != null) {
                        // Bandingkan pemainMaxContainer dengan pemainMax dari Firestore
                        if (pemainMaxContainer == 2 && game.pemainMax < 3) {
                            matchingGames.add(game)
                        } else if (pemainMinContainer == 3 && pemainMaxContainer == 5 && game.pemainMin in 3 .. 5 && game.pemainMax in 3..5) {
                            matchingGames.add(game)
                        } else if (pemainMinContainer == 6 && game.pemainMin > 5) {
                            matchingGames.add(game)
                        }
                    }
                }

                // Update UI di Main Thread
                lifecycleScope.launch(Dispatchers.Main) {
                    if (matchingGames.isNotEmpty()) {
                        val gameNames = matchingGames.joinToString(", ") { it.namaPermainan }
                        binding.resultTextView.text = "Game cocok: $gameNames"
                    } else {
                        binding.resultTextView.text = "Tidak ada game yang cocok"
                    }
                }

            } catch (e: Exception) {
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.resultTextView.text = "Error: ${e.message}"
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        bottomSheetBinding = null
    }
}
