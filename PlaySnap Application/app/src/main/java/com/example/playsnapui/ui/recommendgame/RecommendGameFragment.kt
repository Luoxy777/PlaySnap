package com.example.playsnapui.ui.recommendgame

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playsnapui.databinding.FragmentRecommendGameBinding
import com.example.playsnapui.data.Games

class RecommendGameFragment : Fragment() {

    private var _binding: FragmentRecommendGameBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecommendGameAdapter
    private val recommendedGames = SharedData.recommendedGames

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecommendGameBinding.inflate(inflater, container, false)
        binding.gameFoundText.text = "${recommendedGames.size} permainan ditemukan"

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Display the recommended games
        if (recommendedGames.isNotEmpty()) {
            adapter = RecommendGameAdapter(recommendedGames as ArrayList<Games>)
            binding.recyclerRecommendGames.layoutManager = LinearLayoutManager(requireContext())  // Make sure it's set
            binding.recyclerRecommendGames.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
