package com.example.playsnapui.ui.deleteAccount

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playsnapui.AuthActivity
import com.example.playsnapui.R
import com.google.firebase.auth.FirebaseAuth
import com.example.playsnapui.databinding.FragmentDeleteAccountBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider

class DeleteAccountFragment : Fragment() {

    private lateinit var binding: FragmentDeleteAccountBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDeleteAccountBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()

        // Set up click listener for the "Delete Account" button
        binding.btnDeleteAcc.setOnClickListener {
            val password = binding.etInputPass.text.toString().trim()

            if (TextUtils.isEmpty(password)) {
                showSnackbar("Tolong masukkan password terlebih dahulu!")
                return@setOnClickListener
            }

            deleteAccount(password)
        }

        // Set up click listener for the "Cancel" button
        binding.btnCancelDelete.setOnClickListener {
            // Navigate back or close the fragment
            findNavController().navigateUp()
        }

        return binding.root
    }

    private fun deleteAccount(password: String) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            // Re-authenticate the user with the provided password
            val firebaseAuth = FirebaseAuth.getInstance()
            val credential = EmailAuthProvider.getCredential(user.email!!, password)
            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // If the re-authentication is successful, delete the user account
                        user.delete()
                            .addOnCompleteListener { deleteTask ->
                                if (deleteTask.isSuccessful) {
                                    Toast.makeText(requireContext(), "Akun berhasil dihapus", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(requireContext(), AuthActivity::class.java)
                                    startActivity(intent)
                                    // Optionally, navigate to login screen or main screen
                                } else {
                                    showSnackbar("Akun gagal dihapus")
                                }
                            }
                    } else {
                        showSnackbar("Password-nya salah!")
                    }
                }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
