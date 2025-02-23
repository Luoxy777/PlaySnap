package com.example.playsnapui.ui.recommendgame

import SharedData
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentRecommendGameBinding
import com.example.playsnapui.data.Games
import com.example.playsnapui.ui.home.HomeAdapterForYou
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RecommendGameFragment : Fragment() {

    private var _binding: FragmentRecommendGameBinding ?= null
    private val binding get() = _binding!!

    private lateinit var adapter: HomeAdapterForYou
    private val recommendedGames = SharedData.recommendedGames

    private val db = FirebaseFirestore.getInstance()
    private lateinit var popupWindow : PopupWindow

    var batasUsia1 = arguments?.getInt("batasUsia1") ?: 0
    var batasUsia2Bawah = arguments?.getInt("batasUsia2Bawah") ?: 0
    var batasUsia2Atas = arguments?.getInt("batasUsia2Atas") ?: 0
    var batasUsia3 = arguments?.getInt("batasUsia3") ?: 0
    var batasPemain1 = arguments?.getInt("batasPemain1") ?: 0
    var batasPemain2Bawah = arguments?.getInt("batasPemain2Bawah") ?: 0
    var batasPemain2Atas = arguments?.getInt("batasPemain2Atas") ?: 0
    var batasPemain3 = arguments?.getInt("batasPemain3") ?: 0
    var lokasiContainer = arguments?.getString("lokasiContainer") ?: ""
    val propertyContainer = arguments?.getString("propertyContainer") ?: ""
    var isNullUsia : Boolean = true
    var isNullPemain : Boolean = true
    var isNullLokasi : Boolean = true

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecommendGameBinding.inflate(inflater, container, false)
        binding.gameFoundText.text = "${recommendedGames.size} permainan ditemukan"

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("Properti", "Properti : $propertyContainer")

        popupWindow = PopupWindow(requireContext())

        // Display the recommended games
        if (recommendedGames.isNotEmpty()) {
            adapter = HomeAdapterForYou(recommendedGames as ArrayList<Games>)
            binding.recyclerRecommendGames.layoutManager = LinearLayoutManager(requireContext())  // Make sure it's set
            binding.recyclerRecommendGames.adapter = adapter

            binding.recyclerRecommendGames.post {
                setRecyclerViewHeightBasedOnItems(binding.recyclerRecommendGames)
            }
        }

        binding.usiaButtonCat.setOnClickListener {
            showPopupWindowUsia(it)
            binding.gameFoundText.text = "${recommendedGames.size} permainan ditemukan"
        }

        binding.pemainButtonCat.setOnClickListener {
            showPopupWindowPemain(it)
            binding.gameFoundText.text = "${recommendedGames.size} permainan ditemukan"
        }

        binding.lokasiButtonCat.setOnClickListener {
            showPopupWindowLokasi(it)
            binding.gameFoundText.text = "${recommendedGames.size} permainan ditemukan"
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_RecommendGameFragment_to_HomeFragment)
        }
    }

    private fun cekGamesFilter(){
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val gamesSnapshot = db.collection("games").get().await()
                var matchingGames: MutableSet<Games> = mutableSetOf()

                for (document in gamesSnapshot.documents) {
                    val game = document.toObject(Games::class.java)  // 🔥 Konversi langsung ke objek Game

                    if (game != null) {
                        // cek games filter

                        var batasUsia : Int = 11
                        var indexUsia : Int = game.usiaMin
                        var flag1 : Boolean = false


                        // Usia
                        if(isNullUsia == false){
                            for (i in indexUsia..game.usiaMax) {
                                if (indexUsia <= batasUsia1) {
                                    matchingGames.add(game)
                                }
                                if (indexUsia in batasUsia2Bawah..batasUsia2Atas) {
                                    matchingGames.add(game)
                                }
                                if (indexUsia >= batasUsia3) {
                                    flag1 = true
                                    matchingGames.add(game)
                                    if (flag1 == true) {
                                        break
                                    }
                                }
                                indexUsia++
                            }
                            if(game.usiaMin >= batasUsia){
                                matchingGames.add(game)
                            }
                        }
                        else if(isNullUsia == true){
                            matchingGames.add(game)
                        }
                    }
                }

                var tempGames: MutableSet<Games> = mutableSetOf()
                tempGames = matchingGames.toMutableSet()

                Log.d("Lokasi", "Lokasi : $lokasiContainer")
                if (isNullLokasi == false) {
                    tempGames.removeIf { game ->
                        (lokasiContainer == "Indoor" && game.jenisLokasi == "Outdoor") || (lokasiContainer == "Outdoor" && game.jenisLokasi == "Indoor")
                    }
                }

                tempGames.removeIf { game ->
                    (propertyContainer == "Ya" && game.properti == "") || (propertyContainer == "Tidak" && game.properti != "")
                }

                if (!isNullPemain) {
                    tempGames.removeIf { game ->
                        val rangePemain = game.pemainMin..game.pemainMax
                        var shouldRemove = false

                        if (batasPemain1 != 0) {
                            shouldRemove = shouldRemove || rangePemain.all { it > batasPemain1 }
                        }
                        if (batasPemain2Bawah != 0 && batasPemain2Atas != 0) {
                            shouldRemove = shouldRemove || (rangePemain.all { it < batasPemain2Bawah } || rangePemain.all { it > batasPemain2Atas })
                        }
                        if (batasPemain3 != 0) {
                            shouldRemove = shouldRemove || rangePemain.all { it < batasPemain3 }
                        }

                        shouldRemove
                    }
                }
                matchingGames = tempGames.toMutableSet()
                var j : Int = 0
                for(game in matchingGames){
                    Log.d("Matching Games", "Games : $game")
                    j++
                }
                Log.d("Shared Data Index", "Total : $j")

                val emptySet : MutableSet<Games> = mutableSetOf()
                SharedData.recommendedGames = emptySet.toList()
                SharedData.recommendedGames = matchingGames.toList()
                var i : Int = 0
                for(game in SharedData.recommendedGames){
                    Log.d("Shared Data Games", "Games : $game")
                    i++
                }
                Log.d("Shared Data Index", "Total : $i")

            } catch (e: Exception) {
                lifecycleScope.launch(Dispatchers.Main) {

                }
            }
        }
    }


    private fun cekGamesFilterUsia(){
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val gamesSnapshot = db.collection("games").get().await()
                var tempGames: MutableSet<Games> = mutableSetOf()

                for (document in gamesSnapshot.documents) {
                    val game = document.toObject(Games::class.java)  // 🔥 Konversi langsung ke objek Game

                    if (game != null) {
                        // cek games filter

                        var batasUsia : Int = 11
                        var indexUsia : Int = game.usiaMin
                        var flag1 : Boolean = false


                        // Usia
                        if(isNullUsia == false){
                            for (i in indexUsia..game.usiaMax) {
                                if (indexUsia <= batasUsia1) {
                                    tempGames.add(game)
                                }
                                if (indexUsia in batasUsia2Bawah..batasUsia2Atas) {
                                    tempGames.add(game)
                                }
                                if (indexUsia >= batasUsia3) {
                                    flag1 = true
                                    tempGames.add(game)
                                    if (flag1 == true) {
                                        break
                                    }
                                }
                                indexUsia++
                            }
                            if(game.usiaMin >= batasUsia){
                                tempGames.add(game)
                            }
                        }
                        else if(isNullUsia == true){
                            tempGames.add(game)
                        }
                    }
                }
                SharedData.recommendedGames = tempGames.toList()
                Log.d("Debug", "Jumlah game setelah filter: ${SharedData.recommendedGames.size}")
            } catch (e: Exception) {
                lifecycleScope.launch(Dispatchers.Main) {

                }
            }
        }
    }

    private fun cekGamesFilterPemain(){
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val gamesSnapshot = db.collection("games").get().await()
                var tempGames: MutableSet<Games> = mutableSetOf()

                for (document in gamesSnapshot.documents) {
                    val game = document.toObject(Games::class.java)  // 🔥 Konversi langsung ke objek Game

                    if (game != null) {
                        // cek games filter

                        var batasUsia : Int = 11
                        var indexUsia : Int = game.usiaMin
                        var flag1 : Boolean = false


                        // Usia
                        if(isNullUsia == false){
                            for (i in indexUsia..game.usiaMax) {
                                if (indexUsia <= batasUsia1) {
                                    tempGames.add(game)
                                }
                                if (indexUsia in batasUsia2Bawah..batasUsia2Atas) {
                                    tempGames.add(game)
                                }
                                if (indexUsia >= batasUsia3) {
                                    flag1 = true
                                    tempGames.add(game)
                                    if (flag1 == true) {
                                        break
                                    }
                                }
                                indexUsia++
                            }
                            if(game.usiaMin >= batasUsia){
                                tempGames.add(game)
                            }
                        }
                        else if(isNullUsia == true){
                            tempGames.add(game)
                        }
                    }
                }
                SharedData.recommendedGames = tempGames.toList()
                Log.d("Debug", "Jumlah game setelah filter: ${SharedData.recommendedGames.size}")
            } catch (e: Exception) {
                lifecycleScope.launch(Dispatchers.Main) {

                }
            }
        }

        var tempGames: MutableSet<Games> = mutableSetOf()
        tempGames = SharedData.recommendedGames.toMutableSet()
        if(isNullPemain == false){
            Log.d("tes", "jalan bang")
            tempGames.removeIf { game ->
                val rangePemain = game.pemainMin..game.pemainMax
                var shouldRemove = false

                if (batasPemain1 != 0) {
                    shouldRemove = shouldRemove || rangePemain.all { it > batasPemain1 }
                }
                if (batasPemain2Bawah != 0 && batasPemain2Atas != 0) {
                    shouldRemove = shouldRemove || (rangePemain.all { it < batasPemain2Bawah } || rangePemain.all { it > batasPemain2Atas })
                }
                if (batasPemain3 != 0) {
                    shouldRemove = shouldRemove || rangePemain.all { it < batasPemain3 }
                }

                shouldRemove
            }
        }
        SharedData.recommendedGames = tempGames.toList()
        Log.d("Debug", "Jumlah game setelah filter: ${SharedData.recommendedGames.size}")

    }

    private fun cekGamesFilterLokasi(){
        var tempGames: MutableSet<Games> = mutableSetOf()
        tempGames = SharedData.recommendedGames.toMutableSet()
        if(isNullLokasi == false){
            tempGames.removeIf { game ->
                (lokasiContainer == "Indoor" && game.jenisLokasi == "Outdoor") || (lokasiContainer == "Outdoor" && game.jenisLokasi == "Indoor")
            }
        }
        SharedData.recommendedGames = tempGames.toList()
        Log.d("Debug", "Jumlah game setelah filter: ${SharedData.recommendedGames.size}")

    }

    private fun showPopupWindowUsia(anchorView : View){
        val popupView = layoutInflater.inflate(R.layout.pop_up_usia_category, null)

        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Tampilkan pop-up di bawah tombol usia
        popupWindow.showAsDropDown(anchorView, 0, 10)

        // Atur tombol dalam pop-up
        val usiaOpt1 = popupView.findViewById<AppCompatButton>(R.id.usia_cat_opt_1_rec)
        val usiaOpt2 = popupView.findViewById<AppCompatButton>(R.id.usia_cat_opt_2_rec)
        val usiaOpt3 = popupView.findViewById<AppCompatButton>(R.id.usia_cat_opt_3_rec)

        usiaOpt1.setOnClickListener {
            batasUsia1 = 5
            batasUsia2Bawah = 0
            batasUsia2Atas = 0
            batasUsia3 = 0
            isNullUsia = false
            cekGamesFilter()
            popupWindow.dismiss()
            adapter.updateGames(SharedData.recommendedGames)
        }

        usiaOpt2.setOnClickListener {
            batasUsia1 = 0
            batasUsia2Bawah = 6
            batasUsia2Atas = 10
            batasUsia3 = 0
            isNullUsia = false
            cekGamesFilter()
            popupWindow.dismiss()
            adapter.updateGames(SharedData.recommendedGames)
        }

        usiaOpt3.setOnClickListener {
            batasUsia1 = 0
            batasUsia2Bawah = 0
            batasUsia2Atas = 0
            batasUsia3 = 11
            isNullUsia = false
            cekGamesFilter()
            popupWindow.dismiss()
            adapter.updateGames(SharedData.recommendedGames)
        }
    }

    private fun showPopupWindowPemain(anchorView : View){
        val popupView = layoutInflater.inflate(R.layout.pop_up_pemain_category, null)

        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Tampilkan pop-up di bawah tombol usia
        popupWindow.showAsDropDown(anchorView, 0, 10)

        // Atur tombol dalam pop-up
        val pemainOpt1 = popupView.findViewById<AppCompatButton>(R.id.pemain_cat_opt_1_rec)
        val pemainOpt2 = popupView.findViewById<AppCompatButton>(R.id.pemain_cat_opt_2_rec)
        val pemainOpt3 = popupView.findViewById<AppCompatButton>(R.id.pemain_cat_opt_3_rec)

        pemainOpt1.setOnClickListener {
            batasPemain1 = 2
            batasPemain2Bawah = 0
            batasPemain2Atas = 0
            batasPemain3 = 0
            isNullPemain = false
            cekGamesFilter()
            popupWindow.dismiss()
            adapter.updateGames(SharedData.recommendedGames)
        }

        pemainOpt2.setOnClickListener {
            batasPemain1 = 0
            batasPemain2Bawah = 3
            batasPemain2Atas = 5
            batasPemain3 = 0
            isNullPemain = false
            cekGamesFilter()
            popupWindow.dismiss()
            adapter.updateGames(SharedData.recommendedGames)
        }

        pemainOpt3.setOnClickListener {
            batasPemain1 = 0
            batasPemain2Bawah = 0
            batasPemain2Atas = 0
            batasPemain3 = 6
            isNullPemain = false
            cekGamesFilter()
            popupWindow.dismiss()
            adapter.updateGames(SharedData.recommendedGames)
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showPopupWindowLokasi(anchorView : View){
        val popupView = layoutInflater.inflate(R.layout.pop_up_lokasi_category, null)

        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Tampilkan pop-up di bawah tombol usia
        popupWindow.showAsDropDown(anchorView, 0, 10)

        // Atur tombol dalam pop-up
        val lokasiOpt1 = popupView.findViewById<AppCompatButton>(R.id.lokasi_cat_opt_1_rec)
        val lokasiOpt2 = popupView.findViewById<AppCompatButton>(R.id.lokasi_cat_opt_2_rec)

        lokasiOpt1.setOnClickListener {
            lokasiContainer = "Indoor"
            isNullLokasi = false
            cekGamesFilter()
            popupWindow.dismiss()
            adapter.updateGames(SharedData.recommendedGames)
        }

        lokasiOpt2.setOnClickListener {
            lokasiContainer = "Outdoor"
            isNullLokasi = false
            cekGamesFilter()
            popupWindow.dismiss()
            adapter.updateGames(SharedData.recommendedGames)
        }
    }

    private fun setRecyclerViewHeightBasedOnItems(recyclerView: RecyclerView) {
        recyclerView.post {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
            val adapter = recyclerView.adapter

            if (adapter != null && adapter.itemCount > 0 && layoutManager != null) {
                val firstViewHolder = recyclerView.findViewHolderForAdapterPosition(0)?.itemView

                firstViewHolder?.let {
                    val itemHeight = it.measuredHeight
                    val extraHeight = itemHeight * 3  // Tambahkan tinggi untuk beberapa item tambahan

                    val currentHeight = recyclerView.layoutParams.height
                    recyclerView.layoutParams.height = currentHeight + extraHeight
                    recyclerView.requestLayout()
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}