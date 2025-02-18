package com.example.playsnapui.ui.filter

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.playsnapui.R
import com.example.playsnapui.databinding.BottomSheetFilterPageBinding
import com.example.playsnapui.databinding.FragmentFilterBinding
import com.example.playsnapui.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import androidx.lifecycle.lifecycleScope
import com.example.playsnapui.data.Games
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


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
        var usiaMinContainer : Int ?= null
        var usiaMaxContainer : Int ?= null
        var lokasiContainer : String ?= null
        var pemainMinContainer : Int ?= null
        var pemainMaxContainer : Int ?= null
        var propertiContainer : String ?= null

        binding!!.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_FilterFragment_to_HomeFragment)
        }

        bottomSheetBinding?.let { sheetBinding ->
            // Set listener untuk tombol usia
            sheetBinding.usiaOpt1Btn.setOnClickListener {
                binding.usiaValue.text = "<6 th"
                usiaMinContainer = 0
                usiaMaxContainer = 5
            }
            sheetBinding.usiaOpt2Btn.setOnClickListener {
                binding.usiaValue.text = "6 - 10 th"
                usiaMinContainer = 5
                usiaMaxContainer = 11
            }
            sheetBinding.usiaOpt3Btn.setOnClickListener {
                binding.usiaValue.text = ">10 th"
                usiaMinContainer = 11
                usiaMaxContainer = null
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
                pemainMinContainer = 0
                pemainMaxContainer = 3
            }
            sheetBinding.pemainOpt2Btn.setOnClickListener {
                binding.pemainValue.text = "3 - 5 org"
                pemainMinContainer = 3
                pemainMaxContainer = 5
            }
            sheetBinding.pemainOpt3Btn.setOnClickListener {
                binding.pemainValue.text = ">5 org"
                pemainMinContainer = 6
                pemainMaxContainer = null
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


        binding.mulaiButton.setOnClickListener {
            compareGamesWithDatabase(usiaMinContainer, usiaMaxContainer, lokasiContainer, pemainMinContainer, pemainMaxContainer, propertiContainer)
        }


    }

    private fun compareGamesWithDatabase(usiaMinContainer : Int?, usiaMaxContainer : Int?, lokasiContainer : String?, pemainMinContainer : Int?, pemainMaxContainer : Int?, propertiContainer : String?){
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val gamesSnapshot = db.collection("games").get().await()
                val matchingGames: MutableSet<Games> = mutableSetOf()

                for (document in gamesSnapshot.documents) {
                    val game = document.toObject(Games::class.java)  // 🔥 Konversi langsung ke objek Game

                    if (game != null) {
                        // cek games filter

//                        cekGames(game, matchingGames, usiaContainer, lokasiContainer, pemainContainer, propertiContainer)

                        if (usiaMaxContainer != null && usiaMaxContainer >= game.usiaMin && usiaMaxContainer >= game.usiaMax) continue

                        // Filter berdasarkan lokasi
                        if (lokasiContainer != null && game.jenisLokasi != lokasiContainer) continue

                        // Filter berdasarkan jumlah pemain
                        if (pemainMinContainer != null && game.pemainMax > pemainMinContainer) continue
                        if (pemainMaxContainer != null && game.pemainMin < pemainMaxContainer) continue


                        // Filter berdasarkan properti
                        if (propertiContainer == "Ya" && game.properti == null) continue
                        if (propertiContainer == "Tidak" && game.properti != null) continue


                        matchingGames.add(game)
                    }

                    var index = 0

                    for (game in matchingGames){
                        index++
                        Log.d("Games", "Games : $game")
                    }

                    Log.d("Index", "Total : $index")
                }

                val bundle = Bundle().apply {
                    putParcelableArrayList("MATCHING_GAMES", ArrayList(matchingGames.toList()))
                }

                findNavController().navigate(R.id.action_FilterFragment_to_RecGameFragment, bundle)

            } catch (e: Exception) {
                lifecycleScope.launch(Dispatchers.Main) {

                }
            }
        }
    }

    private fun cekGames(game : Games, matchingGames: MutableSet<Games>, usiaContainer : String, lokasiContainer : String, pemainContainer : String, propertiContainer : String){
        if (pemainContainer.equals("<3") && game.pemainMax < 3) {
            cekUsia(game, matchingGames, usiaContainer, lokasiContainer, propertiContainer)
        } else if (pemainContainer.equals("3-5") && game.pemainMin >= 3 && game.pemainMax <= 5) {
            cekUsia(game, matchingGames, usiaContainer, lokasiContainer, propertiContainer)
        } else if (pemainContainer.equals(">5") && game.pemainMin > 5) {
            cekUsia(game, matchingGames, usiaContainer, lokasiContainer, propertiContainer)
        }else{
            cekUsia(game, matchingGames, usiaContainer, lokasiContainer, propertiContainer)
        }
    }

    private fun cekUsia(game: Games, matchingGames: MutableSet<Games>, usiaContainer : String, lokasiContainer : String, propertiContainer : String){
        if (usiaContainer.equals("<6") && game.usiaMax < 6) {
            cekLokasi(game, matchingGames, lokasiContainer, propertiContainer)
        } else if (usiaContainer.equals("6-10") && game.usiaMin >= 6 && game.usiaMax <= 10) {
            cekLokasi(game, matchingGames, lokasiContainer, propertiContainer)
        } else if (usiaContainer.equals(">10") && game.usiaMin > 10) {
            cekLokasi(game, matchingGames, lokasiContainer, propertiContainer)
        } else{
            cekLokasi(game, matchingGames, lokasiContainer, propertiContainer)
        }
    }

    private fun cekLokasi(game: Games, matchingGames: MutableSet<Games>, lokasiContainer : String, propertiContainer : String){
        if (lokasiContainer.equals("Indoor") && game.jenisLokasi.equals("Indoor")) {
            cekProperti(game, matchingGames, propertiContainer)
        } else if (lokasiContainer.equals("Outdoor") && game.jenisLokasi.equals("Outdoor")) {
            cekProperti(game, matchingGames, propertiContainer)
        } else{
            cekProperti(game, matchingGames, propertiContainer)
        }
    }

    private fun cekProperti(game: Games, matchingGames: MutableSet<Games>, propertiContainer: String){
        if (propertiContainer.equals("Ya") && game.properti != null) {
            matchingGames.add(game)
        } else if (propertiContainer.equals("Tidak") && game.properti == null) {
            matchingGames.add(game)
        } else{
            matchingGames.add(game)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        bottomSheetBinding = null
    }
}
