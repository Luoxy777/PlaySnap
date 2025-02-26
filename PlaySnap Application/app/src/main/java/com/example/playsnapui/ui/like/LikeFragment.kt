package com.example.playsnapui.ui.like

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playsnapui.R
import com.example.playsnapui.data.Games
import com.example.playsnapui.databinding.FragmentBookmarkBinding
import com.example.playsnapui.databinding.FragmentLikeBinding
import com.example.playsnapui.ui.bookmark.BookmarkViewModel
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
        val adapter = HomeAdapterPopular(arrayListOf(), childFragmentManager) // Pass an empty list initially
        val layoutManager = GridLayoutManager(requireContext(), 2) // 2 columns in the grid
        binding.recentRecyclerLike.layoutManager = layoutManager
        binding.recentRecyclerLike.adapter = adapter


        Log.d("Haha", "Cek1")
        likeViewModel.fetchLikedGames()
        Log.d("Haha", "Cek2")

        // Observe the bookmarked games
        likeViewModel.likedGames.observe(viewLifecycleOwner, Observer { games ->
            games?.let {
                adapter.gameList.clear()
                adapter.gameList.addAll(it)
                adapter.notifyDataSetChanged()
            }
        })


    }
}
