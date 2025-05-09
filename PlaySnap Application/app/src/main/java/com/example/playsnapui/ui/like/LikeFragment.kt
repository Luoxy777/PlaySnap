package com.example.playsnapui.ui.like

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playsnapui.databinding.FragmentLikeBinding
import com.example.playsnapui.ui.home.HomeAdapterPopular

class LikeFragment : Fragment() {

    private lateinit var binding: FragmentLikeBinding
    private val likeViewModel: LikeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLikeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        val adapter = HomeAdapterPopular(arrayListOf(), childFragmentManager)
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recentRecyclerLike.layoutManager = layoutManager
        binding.recentRecyclerLike.adapter = adapter

        // Load data pertama kali
        loadLikedGames(adapter)

        // Setup SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener {
            Toast.makeText(requireContext(), "Refreshing...", Toast.LENGTH_SHORT).show()
            loadLikedGames(adapter)
        }
    }

    private fun loadLikedGames(adapter: HomeAdapterPopular) {
        binding.swipeRefreshLayout.isRefreshing = true // Tampilkan indikator refresh

        likeViewModel.fetchLikedGames()

        likeViewModel.likedGames.observe(viewLifecycleOwner, Observer { games ->
            games?.let {
                adapter.gameList.clear()
                adapter.gameList.addAll(it)
                adapter.notifyDataSetChanged()
            }
            binding.swipeRefreshLayout.isRefreshing = false // Sembunyikan indikator refresh
        })
    }
}
