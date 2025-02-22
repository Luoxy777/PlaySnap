package com.example.playsnapui.ui.editProfile

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentEditProfileBinding

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private lateinit var viewModel: EditProfileViewModel
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewBinding
        _binding = FragmentEditProfileBinding.bind(view)
        viewModel = ViewModelProvider(this).get(EditProfileViewModel::class.java)

        // Set up listeners for buttons, etc.
        setupListeners()

        // Observe LiveData
        observeViewModel()
    }

    private fun setupListeners() {
        // Set onClick listeners for the buttons
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack() // Go back to previous screen
        }

        binding.btnChecklist.setOnClickListener {
            // Handle checklist button click
        }

        binding.btnChangeProfile.setOnClickListener {
            // Open dialog to change profile picture
        }

        binding.btnChangePass.setOnClickListener {
            // Navigate to Change Password screen or show a dialog
        }
    }

    private fun observeViewModel() {
        // Example of observing LiveData
        viewModel.profileData.observe(viewLifecycleOwner, Observer { profile ->
            // Update UI with profile data
            binding.tvEdit1NameFill.text = profile.name
            binding.tvEdit2UsernameFill.text = profile.username
            binding.tvEdit3EmailFill.text = profile.email
            binding.tvEdit4GenderFill.text = profile.gender
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}
