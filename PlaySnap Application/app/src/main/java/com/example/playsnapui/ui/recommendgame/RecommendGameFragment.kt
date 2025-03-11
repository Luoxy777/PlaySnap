package com.example.playsnapui.ui.recommendgame

import SharedData
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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
    private val detectedObjects = SharedData.detectedObjects
    private val viewModel: RecommendGameViewModel by viewModels()

    private lateinit var adapter: HomeAdapterForYou
    private val recommendedGames = SharedData.recommendedGames

    private val db = FirebaseFirestore.getInstance()
    private lateinit var popupWindow : PopupWindow
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            binding.numberOfGamesFound.text = "${recommendedGames.size}"
            handler.postDelayed(this, 1000) // Update setiap 1 detik
        }
    }

    var batasUsiaBawah = SharedData.batasUsiaBawah
    var batasUsiaAtas = SharedData.batasUsiaAtas
    var batasPemain1 = SharedData.batasPemain1
    var batasPemain2Bawah = SharedData.batasPemain2Bawah
    var batasPemain2Atas = SharedData.batasPemain2Atas
    var batasPemain3 = SharedData.batasPemain3
    var lokasiContainer = SharedData.lokasiContainer
    val propertyContainer = SharedData.propertyContainer
    var isNullUsia : Boolean = true
    var isNullPemain : Boolean = true
    var isNullLokasi : Boolean = true


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecommendGameBinding.inflate(inflater, container, false)
        binding.numberOfGamesFound.text = "${recommendedGames.size}"
        buttonUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonUI()

        Log.d("isObject", "object : ${SharedData.isObject}")
        popupWindow = PopupWindow(requireContext())

        // Display the recommended games
        adapter = HomeAdapterForYou(ArrayList(recommendedGames), childFragmentManager)
        binding.recyclerRecommendGames.layoutManager = LinearLayoutManager(requireContext())  // Make sure it's set
        binding.recyclerRecommendGames.adapter = adapter

        binding.recyclerRecommendGames.post {
            setRecyclerViewHeightBasedOnItems(binding.recyclerRecommendGames)
        }

        binding.usiaButtonChild.setOnClickListener {
            showPopupWindowUsia(it)
            buttonUI()
            binding.numberOfGamesFound.text = "${recommendedGames.size}"
        }

        binding.pemainButtonChild.setOnClickListener {
            showPopupWindowPemain(it)
            buttonUI()
            binding.numberOfGamesFound.text = "${recommendedGames.size}"
        }

        binding.lokasiButtonChild.setOnClickListener {
            showPopupWindowLokasi(it)
            buttonUI()
            binding.numberOfGamesFound.text = "${recommendedGames.size}"
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun buttonUI(){
        if(batasUsiaBawah == 0 && batasUsiaAtas == 0){
            binding.usiaButtonChild.text = "Semua"
        }
        else{
            if(batasUsiaBawah == 0 && batasUsiaAtas == 5){
                binding.usiaButtonChild.text = "<6"
            }
            else if(batasUsiaBawah == 6 && batasUsiaAtas == 10){
                binding.usiaButtonChild.text = "6-10"
            }
            else if(batasUsiaBawah == 11 && batasUsiaAtas == 13){
                binding.usiaButtonChild.text = ">10"
            }
        }

        if(batasPemain1 == 0
            && batasPemain2Bawah == 0
            && batasPemain2Atas == 0
            && batasPemain3 == 0){
            binding.pemainButtonChild.text = "Semua"
            Log.d("pemain null", "0")
        }
        else{
            Log.d("pemain ga null", "ga 0")
            if(batasPemain1 == 2
                && batasPemain2Bawah == 0
                && batasPemain2Atas == 0
                && batasPemain3 == 0){
                binding.pemainButtonChild.text = "<3"
            }
            else if(batasPemain1 == 0
                && batasPemain2Bawah == 3
                && batasPemain2Atas == 5
                && batasPemain3 == 0){
                binding.pemainButtonChild.text = "3-5"
            }
            else if(batasPemain1 == 0
                && batasPemain2Bawah == 0
                && batasPemain2Atas == 0
                && batasPemain3 == 6){
                binding.pemainButtonChild.text = ">5"
            }
        }

        if(lokasiContainer == ""){
            binding.lokasiButtonChild.text = "Semua"
        }
        else{
            if(lokasiContainer == "Indoor"){
                binding.lokasiButtonChild.text = "Indoor"
            }
            else if(lokasiContainer == "Outdoor"){
                binding.lokasiButtonChild.text = "Outdoor"
            }
        }
    }

    private fun cekGamesFilterNonObject(){
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val gamesSnapshot = db.collection("games").get().await()
                var matchingGames: MutableSet<Games> = mutableSetOf()

                for (document in gamesSnapshot.documents) {
                    val game = document.toObject(Games::class.java)  // 🔥 Konversi langsung ke objek Game

                    if (game != null) {
                        // cek games filter

                        var batasUsia : Int = 11
                        var flag1 : Boolean = false

                        // Usia
                        if(isNullUsia == false){
                            for (i in game.usiaMin..game.usiaMax) {
                                if (i in batasUsiaBawah..batasUsiaAtas) {
                                    matchingGames.add(game)
                                    if(i > 13){
                                        break
                                    }
                                }
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
                            shouldRemove = shouldRemove || rangePemain.none { it in 0..batasPemain1 }
                        }
                        if (batasPemain2Bawah != 0 && batasPemain2Atas != 0) {
                            shouldRemove = shouldRemove || (rangePemain.none { it in batasPemain2Bawah..batasPemain2Atas })
                        }
                        if (batasPemain3 != 0) {
                            shouldRemove = shouldRemove || rangePemain.none { it in batasPemain3..99 }
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
                lifecycleScope.launch(Dispatchers.Main) { adapter.updateGames(SharedData.recommendedGames) }
            } catch (e: Exception) {
                lifecycleScope.launch(Dispatchers.Main) {
                    adapter.updateGames(SharedData.recommendedGames)
                }
            }
        }
    }

    private fun cekGamesFilterObject() {
        try {
            val recommendedList = mutableListOf<Games>()

            db.collection("games")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val game = document.toObject(Games::class.java)
                        Log.d("cek", "Game: ${game.properti}")
                        Log.d("cek 2", "obj : ${SharedData.detectedObjects}")

                        for (detectedObject in SharedData.detectedObjects) {
                            Log.d("cek", "Game properti = ${game.properti} and object = $detectedObject")

                            val alreadyExists = recommendedList.any { it.namaPermainan == game.namaPermainan }
                            if (game.properti.contains(detectedObject, ignoreCase = true) && !alreadyExists) {
                                recommendedList.add(game)
                            }
                        }
                    }

                    Log.d("recomend", "games : $recommendedList")

                    // Pindahkan kode ini ke dalam addOnSuccessListener
                    var tempGames: MutableSet<Games> = recommendedList.toMutableSet()

                    if (!isNullUsia) {
                        tempGames.removeIf { game ->
                            val rangeUsia = game.usiaMin..game.usiaMax
                            rangeUsia.none { it in batasUsiaBawah..batasUsiaAtas } // Hapus jika tidak ada angka dalam range batas
                        }
                    }

                    if (!isNullLokasi) {
                        tempGames.removeIf { game ->
                            (lokasiContainer == "Indoor" && game.jenisLokasi == "Outdoor") ||
                                    (lokasiContainer == "Outdoor" && game.jenisLokasi == "Indoor")
                        }
                    }

                    if (!isNullPemain) {
                        tempGames.removeIf { game ->
                            val rangePemain = game.pemainMin..game.pemainMax
                            var shouldRemove = false

                            if (batasPemain1 != 0) {
                                shouldRemove = shouldRemove || rangePemain.none { it in 0..batasPemain1 }
                            }
                            if (batasPemain2Bawah != 0 && batasPemain2Atas != 0) {
                                shouldRemove = shouldRemove || (rangePemain.none { it in batasPemain2Bawah..batasPemain2Atas })
                            }
                            if (batasPemain3 != 0) {
                                shouldRemove = shouldRemove || rangePemain.none { it in batasPemain3..99 }
                            }

                            shouldRemove
                        }
                    }

                    SharedData.recommendedGames = tempGames.toList()
                    lifecycleScope.launch(Dispatchers.Main) { adapter.updateGames(SharedData.recommendedGames) }
                }
                .addOnFailureListener { exception ->
                    Log.e("ObjectViewModel", "Error getting documents: ", exception)
                }
        } catch (e: Exception) {
            lifecycleScope.launch(Dispatchers.Main) {
                adapter.updateGames(SharedData.recommendedGames)
            }
        }
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
        val usiaOpt4 = popupView.findViewById<AppCompatButton>(R.id.usia_cat_opt_4_rec)


        usiaOpt1.setOnClickListener {

            batasUsiaBawah = 0
            batasUsiaAtas = 5
            isNullUsia = false
            if(SharedData.isObject == false){
                cekGamesFilterNonObject()
            }
            else if(SharedData.isObject == true){
                cekGamesFilterObject()
            }
            buttonUI()
            binding.numberOfGamesFound.text = "${recommendedGames.size}"
            popupWindow.dismiss()
        }

        usiaOpt2.setOnClickListener {
            batasUsiaBawah = 6
            batasUsiaAtas = 10
            isNullUsia = false
            if(SharedData.isObject == false){
                cekGamesFilterNonObject()
            }
            else if(SharedData.isObject == true){
                cekGamesFilterObject()
            }
            buttonUI()
            binding.numberOfGamesFound.text = "${recommendedGames.size}"
            popupWindow.dismiss()
        }

        usiaOpt3.setOnClickListener {
            batasUsiaBawah = 11
            batasUsiaAtas = 13
            isNullUsia = false
            if(SharedData.isObject == false){
                cekGamesFilterNonObject()
            }
            else if(SharedData.isObject == true){
                cekGamesFilterObject()
            }
            buttonUI()
            binding.numberOfGamesFound.text = "${recommendedGames.size}"
            popupWindow.dismiss()
        }

        usiaOpt4.setOnClickListener {
            batasUsiaBawah = 0
            batasUsiaAtas = 0
            isNullUsia = true
            if(SharedData.isObject == false){
                cekGamesFilterNonObject()
            }
            else if(SharedData.isObject == true){
                cekGamesFilterObject()
            }
            buttonUI()
            binding.numberOfGamesFound.text = "${recommendedGames.size}"
            popupWindow.dismiss()
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
        val pemainOpt4 = popupView.findViewById<AppCompatButton>(R.id.pemain_cat_opt_4_rec)

        pemainOpt1.setOnClickListener {
            batasPemain1 = 2
            batasPemain2Bawah = 0
            batasPemain2Atas = 0
            batasPemain3 = 0
            isNullPemain = false
            if(SharedData.isObject == false){
                cekGamesFilterNonObject()
            }
            else if(SharedData.isObject == true){
                cekGamesFilterObject()
            }
            buttonUI()
            binding.numberOfGamesFound.text = "${recommendedGames.size}"
            popupWindow.dismiss()
        }

        pemainOpt2.setOnClickListener {
            batasPemain1 = 0
            batasPemain2Bawah = 3
            batasPemain2Atas = 5
            batasPemain3 = 0
            isNullPemain = false
            if(SharedData.isObject == false){
                cekGamesFilterNonObject()
            }
            else if(SharedData.isObject == true){
                cekGamesFilterObject()
            }
            buttonUI()
            binding.numberOfGamesFound.text = "${recommendedGames.size}"
            popupWindow.dismiss()
        }

        pemainOpt3.setOnClickListener {
            batasPemain1 = 0
            batasPemain2Bawah = 0
            batasPemain2Atas = 0
            batasPemain3 = 6
            isNullPemain = false
            if(SharedData.isObject == false){
                cekGamesFilterNonObject()
            }
            else if(SharedData.isObject == true){
                cekGamesFilterObject()
            }
            buttonUI()
            binding.numberOfGamesFound.text = "${recommendedGames.size}"
            popupWindow.dismiss()
        }

        pemainOpt4.setOnClickListener {
            batasPemain1 = 0
            batasPemain2Bawah = 0
            batasPemain2Atas = 0
            batasPemain3 = 0
            isNullPemain = true
            if(SharedData.isObject == false){
                cekGamesFilterNonObject()
            }
            else if(SharedData.isObject == true){
                cekGamesFilterObject()
            }
            buttonUI()
            binding.numberOfGamesFound.text = "${recommendedGames.size}"
            popupWindow.dismiss()
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
        val lokasiOpt3 = popupView.findViewById<AppCompatButton>(R.id.lokasi_cat_opt_3_rec)

        lokasiOpt1.setOnClickListener {
            lokasiContainer = "Indoor"
            isNullLokasi = false
            if(SharedData.isObject == false){
                cekGamesFilterNonObject()
            }
            else if(SharedData.isObject == true){
                cekGamesFilterObject()
            }
            buttonUI()
            binding.numberOfGamesFound.text = "${recommendedGames.size}"
            popupWindow.dismiss()
        }

        lokasiOpt2.setOnClickListener {
            lokasiContainer = "Outdoor"
            isNullLokasi = false
            if(SharedData.isObject == false){
                cekGamesFilterNonObject()
            }
            else if(SharedData.isObject == true){
                cekGamesFilterObject()
            }
            buttonUI()
            binding.numberOfGamesFound.text = "${recommendedGames.size}"
            popupWindow.dismiss()
        }

        lokasiOpt3.setOnClickListener {
            lokasiContainer = "-"
            isNullLokasi = true
            if(SharedData.isObject == false){
                cekGamesFilterNonObject()
            }
            else if(SharedData.isObject == true){
                cekGamesFilterObject()
            }
            buttonUI()
            binding.numberOfGamesFound.text = "${recommendedGames.size}"
            popupWindow.dismiss()
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
                    val extraHeight = (itemHeight * 5) - 100// Tambahkan tinggi untuk beberapa item tambahan

                    val currentHeight = recyclerView.layoutParams.height
                    recyclerView.layoutParams.height = currentHeight + extraHeight
                    recyclerView.requestLayout()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        buttonUI()
        handler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateRunnable)
        _binding = null
    }
}