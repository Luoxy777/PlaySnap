package com.example.playsnapui.ui.accountSetting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.playsnapui.databinding.FragmentAccountSettingBinding

class AccountSettingFragment : Fragment() {

    // Initialize the view binding
    private var _binding: FragmentAccountSettingBinding? = null
    private val binding get() = _binding!!

    // ViewModel for handling data logic
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using view binding
        _binding = FragmentAccountSettingBinding.inflate(inflater, container, false)

        // Handle button clicks
        binding.btnBack.setOnClickListener {
            // Handle back button click
            // You can pop the fragment from back stack, navigate up, or close the activity
            requireActivity().onBackPressed() // Example of handling back press
        }

        binding.btnSignOut.setOnClickListener {
            // Show SignOutDialogFragment when SignOut button is clicked
            val dialog = SignOutDialogFragment.newInstance()
            dialog.show(parentFragmentManager, "SignOutDialog")
        }

        binding.btnDelete.setOnClickListener {
            // Call ViewModel method to handle delete account logic
            val dialog = DeleteDialogFragment.newInstance()
            dialog.show(parentFragmentManager, "DeleteDialog")
        }

        // Observe LiveData from ViewModel to handle UI updates based on account status
        viewModel.accountStatus.observe(viewLifecycleOwner) { status ->
            // Update UI based on account status (e.g., show a Snackbar or toast)
            // You can show a Snackbar or Toast here to notify the user
        }

        // Return the root view of the fragment
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up the view binding when the view is destroyed
        _binding = null
    }
}
