package com.example.playsnapui.ui.home

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playsnapui.data.Games
import com.example.playsnapui.databinding.FragmentHomeBinding
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewPopular: RecyclerView
    private lateinit var recyclerViewForYou: RecyclerView
    private lateinit var homeAdapterPopular: HomeAdapterPopular
    private lateinit var homeAdapterForYou: HomeAdapterForYou
    private lateinit var db: FirebaseFirestore
    private lateinit var gamesListPopular: ArrayList<Games>
    private lateinit var gamesListForYou: ArrayList<Games>
    private lateinit var viewModel: HomeViewModel

    companion object {
        fun newInstance() = HomeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        viewModel.welcomeMessage.observe(viewLifecycleOwner, Observer { message ->
            binding.tvTitleName.text = message
        })

        db = FirebaseFirestore.getInstance()
        gamesListPopular = arrayListOf()
        gamesListForYou = arrayListOf()

        setupRecyclerViews()
        setupListeners()
        loadGamesFromFirestore()
    }

    private fun setupRecyclerViews() {
        homeAdapterPopular = HomeAdapterPopular(gamesListPopular)
        recyclerViewPopular = binding.recentRecyclerPopgame
        recyclerViewPopular.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerViewPopular.setHasFixedSize(true)
        recyclerViewPopular.adapter = homeAdapterPopular

        homeAdapterForYou = HomeAdapterForYou(gamesListForYou)
        recyclerViewForYou = binding.recentRecyclerForyou
        recyclerViewForYou.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewForYou.setHasFixedSize(true)
        recyclerViewForYou.isNestedScrollingEnabled = false
        recyclerViewForYou.adapter = homeAdapterForYou
    }

    private fun setupListeners() {
        binding.btnFilterGame.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_filterFragment)
        }
    }

    private fun loadGamesFromFirestore() {
        db.collection("games").addSnapshotListener { value, error ->
            if (error != null) {
                Log.e("Firestore Error", error.message.toString())
                return@addSnapshotListener
            }

            value?.documentChanges?.forEach { dc ->
                if (dc.type == DocumentChange.Type.ADDED) {
                    val game = dc.document.toObject(Games::class.java)
                    gamesListPopular.add(game)
                    gamesListForYou.add(game)
                    Log.d("Firestore", "Game added: ${game.namaPermainan}")
                }
            }

            homeAdapterPopular.notifyDataSetChanged()
            homeAdapterForYou.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
