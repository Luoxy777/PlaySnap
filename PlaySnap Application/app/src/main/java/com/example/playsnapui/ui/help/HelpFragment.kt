package com.example.playsnapui.ui.help

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentHelpBinding

class HelpFragment : Fragment() {

    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HelpViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }

        // Inisialisasi ViewModel dengan cara yang lebih direkomendasikan
        viewModel = ViewModelProvider(this)[HelpViewModel::class.java]

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()  // Kembali ke fragment sebelumnya
        }
        binding.btnApaItu.setOnClickListener {
            findNavController().navigate(R.id.action_HelpFragment_to_HelpFragmentPageQ1)
        }
        binding.btnKenapaPilih.setOnClickListener {
            findNavController().navigate(R.id.action_HelpFragment_to_HelpFragmentPageQ2)
        }
        binding.btnBagaimanaCaraPindai.setOnClickListener {
            findNavController().navigate(R.id.action_HelpFragment_to_HelpFragmentPageQ3)
        }
        binding.btnBagaimanaCaraCari.setOnClickListener {
            findNavController().navigate(R.id.action_HelpFragment_to_HelpFragmentPageQ4)
        }
        binding.btnBagaimanaCaraMenghapus.setOnClickListener {
            findNavController().navigate(R.id.action_HelpFragment_to_HelpFragmentPageQ5)
        }
        binding.btnKenapaObjek.setOnClickListener {
            findNavController().navigate(R.id.action_HelpFragment_to_HelpFragmentPageQ6)
        }
        binding.btnBagaimanaCaraMelihat.setOnClickListener {
            findNavController().navigate(R.id.action_HelpFragment_to_HelpFragmentPageQ7)
        }

        binding.btnSend.setOnClickListener {
            findNavController().navigate(R.id.action_HelpFragment_to_HelpFragmentReq)
        }
    }

    fun hideKeyboard() {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus
        view?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Mencegah memory leak
    }
}
