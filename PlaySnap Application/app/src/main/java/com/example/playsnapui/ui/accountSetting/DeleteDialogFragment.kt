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
import androidx.navigation.fragment.findNavController
import com.example.playsnapui.R

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
            findNavController().navigate(R.id.action_SettingsAccountFragment_to_deleteAccountFragment)
            dismiss()
        }

        return binding.root
    }

    companion object {
        // Show the dialog fragment
        fun newInstance(): DeleteDialogFragment {
            return DeleteDialogFragment()
        }
    }
}
