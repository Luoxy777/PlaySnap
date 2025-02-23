package com.example.playsnapui.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel

    companion object {
        fun newInstance() = ProfileFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // Fetch the user profile data from SharedData
        val userProfile = SharedData.userProfile

        // Update the UI with user profile data
        userProfile?.let {
            // Set user information into the UI using binding
            binding.tvFullName.text = it.fullName ?: "N/A"
            binding.tvEmail.text = it.email ?: "N/A"
//            binding.tvUsername.text = it.username ?: "N/A"

//             Load the profile picture (if any)
            val profilePicUri = it.profilePicture // Assuming you have a URI for profile picture
            Glide.with(this)
                .load(profilePicUri)
                .transform(CircleCrop())
                .into(binding.profilePic)
        }

        // Handle the edit profile button click (for example, navigate to another screen)
        binding.btnEditProfile.setOnClickListener {
            // Open image picker or navigate to the edit profile screen
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        // Handle other buttons (e.g., History Game, Report, Settings, etc.)
        binding.btnHistoryGame.setOnClickListener {
            // Navigate to history game screen
            findNavController().navigate(R.id.action_profileFragment_to_historyFragment)
        }

        binding.btnReport.setOnClickListener {
            // Navigate to report screen
            findNavController().navigate(R.id.action_profileFragment_to_reportFragment)
        }

        binding.btnSettingAcc.setOnClickListener {
            // Navigate to settings screen
            findNavController().navigate(R.id.action_profileFragment_to_accountSettingFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}
