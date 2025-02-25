package com.example.playsnapui.ui.accountSetting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.playsnapui.AuthActivity
import com.example.playsnapui.databinding.PopupSignOutBinding
import com.google.firebase.auth.FirebaseAuth

class SignOutDialogFragment : DialogFragment() {

    private lateinit var binding: PopupSignOutBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using ViewBinding
        binding = PopupSignOutBinding.inflate(inflater, container, false)

        // Set up the background darkening effect
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Handle the "No" button click
        binding.btnNo.setOnClickListener {
            dismiss()  // Close the dialog
        }

        // Handle the "Yes" button click
        binding.btnYes.setOnClickListener {
            // Handle sign out action, for example, clearing user session
            signOutUser()
        }

        return binding.root
    }

    private fun signOutUser() {
        // Sign out the user from FirebaseAuth
        auth.signOut()

        // Show a Toast confirming sign out
        Toast.makeText(requireContext(), "You have signed out successfully", Toast.LENGTH_SHORT).show()

        // Optionally, dismiss the dialog after sign out
        dismiss()

        // You can redirect the user to the login screen if needed
        // For example:
        startActivity(Intent(requireContext(), AuthActivity::class.java))
        activity?.finish()
    }


    companion object {
        // Show the dialog fragment
        fun newInstance(): SignOutDialogFragment {
            return SignOutDialogFragment()
        }
    }
}
