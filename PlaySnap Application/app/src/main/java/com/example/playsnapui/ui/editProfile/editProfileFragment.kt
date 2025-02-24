package com.example.playsnapui.ui.editProfile

import SharedData
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.InputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private lateinit var viewModel: EditProfileViewModel
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val PICK_IMAGE_REQUEST = 1001
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>  // Declare the launcher



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewBinding
        _binding = FragmentEditProfileBinding.bind(view)
        viewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]

        // Populate profile data
        val userProfile = SharedData.userProfile
        userProfile?.let {
            binding.tvEdit1NameFill.text = it.fullName ?: "N/A"
            binding.tvEdit2UsernameFill.text = it.username ?: "N/A"
            binding.tvEdit3EmailFill.text = it.email ?: "N/A"
            binding.tvEdit4GenderFill.text = it.gender ?: "N/A"
        }

        val profilePicUri = userProfile?.profilePicture
        Glide.with(this)
            .load(profilePicUri)
            .transform(CircleCrop())
            .into(binding.profilePic)

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val selectedImageUri: Uri? = result.data?.data
                selectedImageUri?.let {
                    saveImageToInternalStorage(it)
                }
            }
        }

        // Set up listeners for buttons
        setupListeners()

        // Observe LiveData
        observeViewModel()

        // Set click listeners for TextViews to make them editable
        makeEditableOnClick(binding.tvEdit1NameFill)
        makeEditableOnClick(binding.tvEdit2UsernameFill)
        makeEditableOnClick(binding.tvEdit3EmailFill)
        binding.tvEdit4GenderFill.tag = false;
        binding.tvEdit4GenderFill.setOnClickListener {
            // Show the gender popup when clicked
            binding.tvEdit4GenderFill.tag = !(binding.tvEdit4GenderFill.tag as Boolean)
            if (binding.tvEdit4GenderFill.tag == false) {
                // Set drawable to the down arrow
                binding.tvEdit4GenderFill.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.back_button_down, 0
                )
                hideGenderPopup()
            } else {
                // Set drawable to the up arrow
                binding.tvEdit4GenderFill.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.back_button_up, 0
                )
                showGenderPopup()
            }

        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_editProfileFragment_to_ProfileFragment) // Go back to previous screen
        }

        binding.btnChecklist.setOnClickListener {
            // When checklist button is clicked, save data to Firestore and SharedData
            updateUserProfileInFirestore()
        }

        binding.btnChangeProfile.setOnClickListener {
            // Let the user pick an image from the gallery
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)  // Launch the image picker using the new API
        }

        binding.btnChangePass.setOnClickListener {
            // Navigate to Change Password screen or show a dialog
            findNavController().navigate(R.id.action_editProfileFragment_to_ChangePassFragment)
        }
    }

    private fun observeViewModel() {
        // Example of observing LiveData
        viewModel.profileData.observe(viewLifecycleOwner) { profile ->
            // Update UI with profile data
            binding.tvEdit1NameFill.text = profile.name
            binding.tvEdit2UsernameFill.text = profile.username
            binding.tvEdit3EmailFill.text = profile.email
            binding.tvEdit4GenderFill.text = profile.gender
        }
    }
    private fun hideGenderPopup() {
        val genderPopup = binding.root.findViewById<View>(R.id.pop_up_gender)
        genderPopup?.visibility = View.GONE  // Hide the popup
    }
    private fun showGenderPopup() {
        // Check if the popup view is already inflated
        var genderPopup = binding.root.findViewById<View>(R.id.pop_up_gender)

        // If it's null, inflate the gender popup and add it to the layout
        if (genderPopup == null) {
            genderPopup = layoutInflater.inflate(R.layout.pop_up_gender, binding.llcEdit4GenderFill, false)
            // Add it to the parent layout dynamically
            binding.llcEdit4GenderFill.addView(genderPopup)
        }

        // Make the gender popup visible
        genderPopup.visibility = View.VISIBLE

        // Set click listeners for each option in the popup
        val opt1Laki = genderPopup.findViewById<View>(R.id.opt1_laki)
        val opt2Perempuan = genderPopup.findViewById<View>(R.id.opt2_perempuan)
        val opt3None = genderPopup.findViewById<View>(R.id.opt3_none)

        // Set listeners for each gender option
        opt1Laki.setOnClickListener {
            binding.tvEdit4GenderFill.text = getString(R.string.option_laki) // Set gender to "Laki"
            genderPopup.visibility = View.GONE // Hide the popup after selection
        }

        opt2Perempuan.setOnClickListener {
            binding.tvEdit4GenderFill.text = getString(R.string.option_perempuan) // Set gender to "Perempuan"
            genderPopup.visibility = View.GONE // Hide the popup after selection
        }

        opt3None.setOnClickListener {
            binding.tvEdit4GenderFill.text = getString(R.string.option_none) // Set gender to "None"
            genderPopup.visibility = View.GONE // Hide the popup after selection
        }
    }

    @SuppressLint("ServiceCast")
    private fun makeEditableOnClick(textView: TextView) {
        textView.setOnClickListener {
            // Create an EditText dynamically and set the text from TextView
            val editText = EditText(requireContext())
            editText.setText(textView.text)

            // Optionally set properties like hint or input type
            editText.hint = "Ketikkan yang baru"
            editText.inputType = android.text.InputType.TYPE_CLASS_TEXT

            // Set text color and hint color
            editText.setTextColor(resources.getColor(R.color.black, null))
            editText.setHintTextColor(resources.getColor(R.color.grey, null)) // You can adjust the hint color too

            // Add padding to the right
            val layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(10, 5, 10, 5) // Set margins (top, left, right, bottom)

            // Set the layoutParams for the EditText
            editText.layoutParams = layoutParams
            val typeface = ResourcesCompat.getFont(requireContext(), R.font.andikaregular)
            editText.typeface = typeface

            // Replace TextView with EditText
            val parent = textView.parent as ViewGroup
            val index = parent.indexOfChild(textView)
            parent.removeViewAt(index)
            parent.addView(editText, index)

            // Request focus to automatically show the keyboard
            editText.requestFocus()

            // Store the editText reference in a field to be used later
            textView.tag = editText

            // Show the keyboard
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)

            // Set an OnFocusChangeListener to revert to TextView when focus is lost
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    // When focus is lost, save the new value and convert back to TextView
                    textView.text = editText.text
                    parent.removeViewAt(index)
                    parent.addView(textView, index)

                    // Hide the keyboard
//                    imm.hideSoftInputFromWindow(editText.windowToken, 0)
                }
            }

            // Handle "done" button press on the keyboard (Enter/Return key)
            editText.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Hide the keyboard
                    imm.hideSoftInputFromWindow(editText.windowToken, 0)

                    // Update the TextView with the new value from EditText
                    textView.text = editText.text

                    // Remove the EditText and add the TextView back
                    parent.removeViewAt(index)
                    parent.addView(textView, index)

                    // Clear the focus from EditText
                    editText.clearFocus()

                    true  // Return true to indicate that the action was handled
                } else {
                    false
                }
            }

        }
    }


    private fun updateUserProfileInFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Get the user ID
        val db = FirebaseFirestore.getInstance()

        if (userId != null) {
            val userDocRef = db.collection("users").document(userId)

            // Create a map of the fields that were updated
            val updatedProfileData = mutableMapOf<String, Any>()
            SharedData.userProfile?.fullName = binding.tvEdit1NameFill.text.toString()
            SharedData.userProfile?.username = binding.tvEdit2UsernameFill.text.toString()
            SharedData.userProfile?.email = binding.tvEdit3EmailFill.text.toString()
            SharedData.userProfile?.gender = binding.tvEdit4GenderFill.text.toString()

            updatedProfileData["fullName"] = SharedData.userProfile?.fullName ?: ""
            updatedProfileData["username"] = SharedData.userProfile?.username ?: ""
            updatedProfileData["email"] = SharedData.userProfile?.email ?: ""
            updatedProfileData["gender"] = SharedData.userProfile?.gender ?: ""
            updatedProfileData["profilePicture"] = SharedData.userProfile?.profilePicture ?: "" // Update with new profile picture path

            // Update the user document in Firestore
            userDocRef.update(updatedProfileData)
                .addOnSuccessListener {
                    Log.d("EditProfile", "User profile updated successfully in Firestore")
                    Toast.makeText(requireContext(), "Update profile successful!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("EditProfile", "Error updating user profile in Firestore", e)
                    Toast.makeText(requireContext(), "Update profile failed!", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun saveImageToInternalStorage(selectedImageUri: Uri) {
        val inputStream: InputStream? = context?.contentResolver?.openInputStream(selectedImageUri)
        inputStream?.let { input ->
            try {
                // Create a file in internal storage
                val file = File(requireContext().filesDir, "profile_pic.jpg")
                val outputStream: OutputStream = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var length: Int
                while (input.read(buffer).also { length = it } != -1) {
                    outputStream.write(buffer, 0, length)
                }
                outputStream.flush()
                outputStream.close()
                input.close()

                // Get the path of the saved image
                val imagePath = file.absolutePath

                // Update SharedData with the local file path
                SharedData.userProfile?.profilePicture = imagePath

                Log.d("Gagal gak?", "${SharedData.userProfile?.profilePicture}")

//                // Update Firestore with the new path
//                updateUserProfileInFirestore()

                // Load the image into the UI
                Glide.with(this)
                    .load(file)
                    .transform(CircleCrop())
                    .into(binding.profilePic)

            } catch (e: IOException) {
                Log.e("EditProfile", "Failed to save image to internal storage", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}
