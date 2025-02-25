package com.example.playsnapui.ui.login

import SharedData.userProfile
import UserProfile
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.playsnapui.HomeActivity
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentLoginBinding
import com.example.playsnapui.ui.login.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class  LoginFragment : Fragment() {

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

        // Observe email and password fields
        binding.etEmail.addTextChangedListener { viewModel.onEmailChanged(it.toString()) }
        binding.etPassword.addTextChangedListener { viewModel.onPasswordChanged(it.toString()) }

        // Handle login button click
        binding.masukButton.setOnClickListener {
            viewModel.login()

        }


        // Observe login state
        viewModel.loginState.observe(viewLifecycleOwner, Observer { success ->
            if (success) {
                Toast.makeText(requireContext(), "Berhasil masuk!", Toast.LENGTH_SHORT).show()
                // Navigate to homeFragment
                val auth = FirebaseAuth.getInstance()
                if (auth.currentUser != null) {
                    val user = auth.currentUser
                    user?.let {
                        val userId = user.uid // Get UID of authenticated user
                        val db = FirebaseFirestore.getInstance()
                        // Fetch user data
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { document ->
                                if (document != null) {
                                    val email = document.getString("email")
                                    val fullName = document.getString("fullName")
                                    val username = document.getString("username")
                                    val profilePicture = document.getString("profilePicture")
                                    val gender = document.getString("gender")
                                    // Store user profile globally
                                    userProfile = UserProfile(
                                        email,
                                        fullName,
                                        username,
                                        profilePicture,
                                        gender
                                    )
                                    Log.d(
                                        "Splash",
                                        "User Profile Loaded: ${userProfile?.fullName ?: "N/A"}"
                                    )

                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.w("Error", "Error getting document: ", exception)

                            }
                    }
                }
                val intent = Intent(requireActivity(), HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Gagal masuk. Periksa lagi pengenal Anda.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
