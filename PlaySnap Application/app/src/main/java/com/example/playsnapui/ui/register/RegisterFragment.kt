package com.example.playsnapui.ui.register

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }

        setUpListener()

        // ✅ Observe registerState
        lifecycleScope.launch {
            viewModel.registerState.collect { state ->
                when (state) {
                    is RegisterState.Loading -> {
                        binding.registerButton.isEnabled = false
                        Toast.makeText(requireContext(), "Mendaftarkan...", Toast.LENGTH_SHORT).show()
                    }
                    is RegisterState.Success -> {
                        binding.registerButton.isEnabled = true
                        Toast.makeText(requireContext(), "Pendaftaran Sukses!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment) // ✅ Navigate to Login
                    }
                    is RegisterState.Error -> {
                        binding.registerButton.isEnabled = true
                        Toast.makeText(requireContext(), "Eror: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }

    fun setUpListener(){
        binding.registerButton.setOnClickListener {
            val fullName = binding.etFullname.text.toString()
            val email = binding.etEmail.text.toString()
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmpass.text.toString()
            val photoProfile = "https://drive.google.com/uc?export=view&id=1zXG6XHvZmcGaX8G7idtDqu_JO2zDwkt7"

            if (password == confirmPassword) {
                viewModel.register(fullName, email, username, password, photoProfile)
            } else {
                Toast.makeText(requireContext(), "Kata sandi tidak cocok dengan konfirmasi", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.masuk.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
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
        _binding = null
    }
}
