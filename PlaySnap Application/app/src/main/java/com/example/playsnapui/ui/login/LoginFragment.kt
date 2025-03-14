package com.example.playsnapui.ui.login

import SharedData.userProfile
import UserProfile
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.playsnapui.HomeActivity
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setOnTouchListener { _, _ -> hideKeyboard(); false }

        binding.etEmail.addTextChangedListener { viewModel.onEmailChanged(it.toString()) }
        binding.etPassword.addTextChangedListener { viewModel.onPasswordChanged(it.toString()) }

        setUpListeners()
        observeViewModel()
    }

    private fun setUpListeners() {
        binding.masukButton.setOnClickListener { viewModel.login() }
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.daftar.setOnClickListener { findNavController().navigate(R.id.action_loginFragment_to_registerFragment) }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Berhasil masuk!", Toast.LENGTH_SHORT).show()
                navigateToHome()
            } else {
                Toast.makeText(requireContext(), "Gagal masuk. Periksa kembali pengenal Anda.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToHome() {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val document = db.collection("users").document(user.uid).get().await()

                userProfile = UserProfile(
                    document.getString("email"),
                    document.getString("fullName"),
                    document.getString("username"),
                    document.getString("profilePicture"),
                    document.getString("gender")
                )

                startActivity(Intent(requireActivity(), HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                requireActivity().finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view?.windowToken?.let { imm.hideSoftInputFromWindow(it, 0) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
