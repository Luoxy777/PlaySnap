package com.example.playsnapui.ui.like

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentLikeBinding
import com.example.playsnapui.ui.home.HomeAdapterPopular

class LikePageFragment : Fragment() {

    private var _binding: FragmentLikeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LikeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLikeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView
        val recyclerView = binding.recentRecyclerLike
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = HomeAdapterPopular(arrayListOf())
        recyclerView.adapter = adapter

        // Observe LiveData from ViewModel
//        viewModel.recentItems.observe(viewLifecycleOwner, Observer {
//            adapter.submitList(it)
//        })

        // Set up any other views, such as title and subtitle
        binding.tvTitleLike.text = getString(R.string.title_like)
        binding.tvSubtitleLike.text = getString(R.string.subtitle_like)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
