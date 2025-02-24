package com.example.playsnapui.ui.history

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentBookmarkBinding
import com.example.playsnapui.databinding.FragmentHistoryBinding
import com.example.playsnapui.ui.bookmark.BookmarkViewModel
import com.example.playsnapui.ui.home.HomeAdapterPopular

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private val historyViewModel: HistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        val adapter = HomeAdapterPopular(arrayListOf()) // Pass an empty list initially
        val layoutManager = GridLayoutManager(requireContext(), 2) // 2 columns in the grid
        binding.recentRecyclerHistory.layoutManager = layoutManager
        binding.recentRecyclerHistory.adapter = adapter


        Log.d("Haha", "Cek1")
        historyViewModel.fetchVisitedGames()
        Log.d("Haha", "Cek2")

        // Observe the bookmarked games
        historyViewModel.visitedGames.observe(viewLifecycleOwner, Observer { games ->
            games?.let {
                adapter.gameList.clear()
                adapter.gameList.addAll(it)
                adapter.notifyDataSetChanged()
            }
        })


        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_historyFragment_to_profileFragment)
        }
    }
}
