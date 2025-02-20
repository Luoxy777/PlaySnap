package com.example.playsnapui.ui.tutorial

import SharedData.gameDetails
import TutorialViewModel
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.playsnapui.databinding.FragmentTutorialBinding

class TutorialFragment : Fragment() {
    private var binding: FragmentTutorialBinding? = null
    private var viewModel: TutorialViewModel? = null
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity())[TutorialViewModel::class.java]

        // Inflate the layout using ViewBinding
        binding = FragmentTutorialBinding.inflate(inflater, container, false)
        binding!!.bottomSheet.titleGameDescHeader.text = gameDetails?.namaPermainan ?: "NA"
        binding!!.bottomSheet.subtitleHeaderDesc.text = "${gameDetails?.jenisLokasi}, Usia ${gameDetails?.usiaMin} - ${gameDetails?.usiaMax} tahun"
        binding!!.bottomSheet.deskripsiContent.text = gameDetails?.deskripsi ?: "NA"
        binding!!.bottomSheet.alatBermainContent.text = gameDetails?.properti ?: "NA"
        binding!!.bottomSheet.langkahBermainContent.text = gameDetails?.tutorial ?: "NA"
        binding!!.bottomSheet.numberPlayer.text = "${gameDetails?.pemainMin}-${gameDetails?.pemainMax}"

        // Set up button listeners
        setUpListeners()
        return binding!!.root
    }

    private fun setUpListeners() {
        // Back button click listener
        binding?.btnBack?.setOnClickListener { v ->
            // Navigate back using NavController
            NavHostFragment.findNavController(this).popBackStack()
        }

        // Bookmark button click listener
        binding?.bookmarkButtonTutorial?.setOnClickListener { v -> viewModel?.toggleBookmark() }

        // Like button click listener
        binding?.likeButtonTutorial?.setOnClickListener { v -> viewModel?.toggleLike() }

        // Share button click listener
        binding?.shareButtonTutorial?.setOnClickListener { v -> viewModel?.toggleShare() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up binding to prevent memory leaks
        binding = null
    }
}