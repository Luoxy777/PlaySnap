package com.example.playsnapui.ui.accountSetting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.playsnapui.AuthActivity
import com.example.playsnapui.databinding.PopupDeleteAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import android.widget.Toast

class DeleteDialogFragment : DialogFragment() {

    private lateinit var binding: PopupDeleteAccountBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using ViewBinding
        binding = PopupDeleteAccountBinding.inflate(inflater, container, false)

        // Set up the background darkening effect
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Handle the "No" button click
        binding.btnNo.setOnClickListener {
            dismiss()  // Close the dialog
        }

        // Handle the "Yes" button click
        binding.btnYes.setOnClickListener {
            // Call DeleteUser function to delete the account
            DeleteUser()
        }

        return binding.root
    }

    private fun DeleteUser() {
        val currentUser = auth.currentUser

        // Check if the current user is signed in
        if (currentUser != null) {
            val email = currentUser.email
            val password = "user_password" // Prompt for password if required

            // Re-authenticate the user with their email and password
            val credential = EmailAuthProvider.getCredential(email!!, password)

            currentUser.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // If re-authentication was successful, delete the user account
                    currentUser.delete().addOnCompleteListener { deleteTask ->
                        if (deleteTask.isSuccessful) {
                            // Account deleted successfully, show a success message
                            Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show()

                            // Redirect to the login screen (or anywhere else)
                            val intent = Intent(requireActivity(), AuthActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish() // Close the current activity
                        } else {
                            // Handle failure in deleting the account
                            Toast.makeText(requireContext(), "Failed to delete account: ${deleteTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Handle failure in re-authenticating the user
                    Toast.makeText(requireContext(), "Re-authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Handle case when the user is not signed in
            Toast.makeText(requireContext(), "No user is signed in", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        // Show the dialog fragment
        fun newInstance(): DeleteDialogFragment {
            return DeleteDialogFragment()
        }
    }
}
