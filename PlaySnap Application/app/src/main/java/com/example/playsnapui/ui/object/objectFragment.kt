package com.example.playsnapui.ui.`object`

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.playsnapui.databinding.FragmentObjectBinding

class ObjectFragment : Fragment() {

    private var _binding: FragmentObjectBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ObjectViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentObjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.mulaiButton.setOnClickListener {
            viewModel.startGame()
        }

        binding.hapusButton.setOnClickListener {
            viewModel.clearObjects()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}