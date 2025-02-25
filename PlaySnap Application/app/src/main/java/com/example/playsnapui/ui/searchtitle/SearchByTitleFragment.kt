package com.example.playsnapui.ui.searchtitle

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playsnapui.data.Games
import com.example.playsnapui.databinding.FragmentSearchByTitleBinding
import com.example.playsnapui.ui.home.HomeAdapterForYou
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import android.view.inputmethod.InputMethodManager


class SearchByTitleFragment : Fragment() {

    private var _binding: FragmentSearchByTitleBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SearchByTitleViewModel

    private lateinit var adapter: HomeAdapterForYou
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchByTitleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val emptyList = ArrayList<Games>()
        adapter = HomeAdapterForYou(emptyList)
        val recyclerView = binding.listGamesSearchtitle
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this).get(SearchByTitleViewModel::class.java)
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.etSearchGame.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {

                binding.addButton.performClick() // Jalankan pencarian

                // Sembunyikan keyboard setelah Enter ditekan
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etSearchGame.windowToken, 0)

                return@setOnEditorActionListener true // Konsumsi event
            }
            false
        }


        binding.addButton.setOnClickListener {
            val queryText = binding.etSearchGame.text.toString().trim() // Ambil teks pencarian

            if (queryText.isNotEmpty()) {
                val db = FirebaseFirestore.getInstance()
                val queryEnd = queryText + "\uf8ff" // Batasan untuk pencarian teks

                db.collection("games")
                    .whereGreaterThanOrEqualTo("namaPermainan", queryText)
                    .whereLessThanOrEqualTo("namaPermainan", queryEnd)
                    .get()
                    .addOnSuccessListener { documents ->
                        val gameList = mutableListOf<Games>()
                        for (document in documents) {
                            val game = document.toObject(Games::class.java)
                            gameList.add(game)
                        }
                        adapter.updateGames(gameList) // Perbarui RecyclerView
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Masukkan nama permainan!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}