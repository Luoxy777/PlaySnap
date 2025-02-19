package com.example.playsnapui.ui.recommendgame

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.playsnapui.R
import com.example.playsnapui.data.Games
import com.example.playsnapui.databinding.FragmentRecommendGameBinding

class RecommendGameFragment : Fragment() {

    companion object {
        fun newInstance() = RecommendGameFragment()
    }

    private var _binding : FragmentRecommendGameBinding ?= null
    private val binding get() = _binding!!

    private lateinit var viewModel: RecommendGameViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecommendGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){

        val matchingGames = arguments?.getParcelableArrayList<Games>("MATCHING_GAMES") ?: arrayListOf()


    }

}