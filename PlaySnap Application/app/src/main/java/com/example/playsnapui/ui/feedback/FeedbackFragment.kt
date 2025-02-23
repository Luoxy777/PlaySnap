package com.example.playsnapui.ui.feedback

import SharedData
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentFeedbackBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackFragment : Fragment() {

    private var _binding: FragmentFeedbackBinding? = null
    private val binding get() = _binding!!
    val gameDetails = SharedData.gameDetails

    private val viewModel: FeedbackViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textView2.text = "Have Fun, ${SharedData.userProfile!!.fullName}"
        binding.gameTitleFeedback.text = gameDetails!!.namaPermainan
        binding.gameDetailsFeedback.text = "${gameDetails!!.jenisLokasi}, ${gameDetails.usiaMin}-${gameDetails.usiaMax} tahun"
        binding.playerCountFeedbackNumber.text = "${gameDetails.pemainMin}-${gameDetails.pemainMax}"
        setupListener()
    }

    private fun setupListener(){
        binding.btnBack.setOnClickListener{
            findNavController().navigateUp()
        }

        binding.selesaiButtonFeedback.setOnClickListener {
            findNavController().navigate(R.id.action_FeedbackFragment_to_HomeFragment)

            // perhitungan rating
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
