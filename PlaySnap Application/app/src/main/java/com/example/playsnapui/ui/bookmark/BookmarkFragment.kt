package com.example.playsnapui.ui.bookmark

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.playsnapui.R
import com.example.playsnapui.data.Games
import com.example.playsnapui.databinding.FragmentBookmarkBinding
import com.example.playsnapui.ui.home.HomeAdapterPopular

class BookmarkFragment : Fragment() {

    private lateinit var binding: FragmentBookmarkBinding
    private val bookmarkViewModel: BookmarkViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        swipeRefreshLayout.setOnRefreshListener {
            // Tambahkan kode refresh di sini, misalnya reload data
            refreshData()

            // Hentikan animasi loading setelah beberapa detik
            Handler(Looper.getMainLooper()).postDelayed({
                swipeRefreshLayout.isRefreshing = false
            }, 2000) // 2 detik
        }

        // Initialize RecyclerView
        val adapter = HomeAdapterPopular(arrayListOf(), childFragmentManager) // Pass an empty list initially
        val layoutManager = GridLayoutManager(requireContext(), 2) // 2 columns in the grid
        binding.recentRecyclerBookmark.layoutManager = layoutManager
        binding.recentRecyclerBookmark.adapter = adapter


        Log.d("Haha", "Cek1")
        bookmarkViewModel.fetchBookmarkedGames()
        Log.d("Haha", "Cek2")

        // Observe the bookmarked games
        bookmarkViewModel.bookmarkedGames.observe(viewLifecycleOwner, Observer { games ->
            games?.let {
                adapter.gameList.clear()
                adapter.gameList.addAll(it)
                adapter.notifyDataSetChanged()
            }
        })


    }

    private fun refreshData() {
        // Contoh: Load ulang data dari server atau update UI
        Toast.makeText(requireContext(), "Refreshing...", Toast.LENGTH_SHORT).show()
        bookmarkViewModel.fetchBookmarkedGames()
    }
}
