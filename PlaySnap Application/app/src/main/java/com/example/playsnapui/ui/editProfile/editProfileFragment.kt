package com.example.playsnapui.ui.editProfile

import SharedData
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import kotlin.properties.Delegates

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private lateinit var viewModel: EditProfileViewModel
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private var editText: EditText? = null

    private val CAMERA_PERMISSION_REQUEST_CODE = 1001
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>  // Declare the launcher
    private var filename: String = "";

    private var flag: Int = 0


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
//        makeEditableOnClick(binding.tvEdit3EmailFill)
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
        val db = FirebaseFirestore.getInstance()

        binding.btnBack.setOnClickListener {
            FirebaseAuth.getInstance().currentUser?.let { it1 ->
                db.collection("users").document(it1.uid).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            SharedData.userProfile?.profilePicture =
                                document.getString("profilePicture")
                            findNavController().navigate(R.id.action_editProfileFragment_to_ProfileFragment) // Go back to previous screen
                        }
                    }
            }
        }

        binding.btnChecklist.setOnClickListener {
            editText?.clearFocus()
            updateUserProfileInFirestore()
        }

        binding.btnChangeProfile.setOnClickListener {
//            // Let the user pick an image from the gallery
//            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            pickImageLauncher.launch(intent)  // Launch the image picker using the new API
            showImagePickerDialog()
        }

        binding.btnChangePass.setOnClickListener {
            // Navigate to Change Password screen or show a dialog
            findNavController().navigate(R.id.action_editProfileFragment_to_ChangePassFragment)
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Pilih dari Galeri", "Ambil Foto")
        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Gambar Profil")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickImageFromGallery()
                    1 -> captureImageFromCamera()
                }
            }
            .show()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    // Define the request code at the top of your activity/fragment class

    private var imageUri: Uri? = null  // Simpan URI global agar bisa diakses setelah capture

    @SuppressLint("QueryPermissionsNeeded")
    private fun captureImageFromCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val imageCaptureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (imageCaptureIntent.resolveActivity(requireContext().packageManager) != null) {
                filename = "profile_pic_${System.currentTimeMillis()}.jpg"

                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES) // Save to Pictures
                }

                // Simpan URI global untuk digunakan setelah foto diambil
                imageUri = requireContext().contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                if (imageUri != null) {
                    imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    cameraResultLauncher.launch(imageCaptureIntent)
                } else {
                    Log.e("CaptureImage", "Failed to create image URI")
                }
            } else {
                Log.e("CaptureImage", "No camera app available")
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    private val cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (imageUri != null) {
                    Log.d("CaptureImage", "Image captured and saved at: $imageUri")
                    SharedData.userProfile?.profilePicture = imageUri.toString()

                    // **Langsung load gambar dengan Glide**
                    loadImageWithGlide(imageUri.toString())
                } else {
                    Log.e("CaptureImage", "Failed to capture image, URI is null.")
                }
            } else {
                Log.e("CaptureImage", "Failed to capture image, result not OK.")
            }
        }


    private fun loadImageWithGlide(imageUri: String) {
        // Use Glide to load the image into an ImageView
        Glide.with(requireContext())
            .load(imageUri)
            .transform(CircleCrop())
            .into(binding.profilePic)
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

    @SuppressLint("ServiceCast", "ResourceType")
    private fun makeEditableOnClick(textView: TextView) {
        textView.setOnClickListener {
            val context = textView.context
            val parent = textView.parent as? ViewGroup ?: return@setOnClickListener
            val index = parent.indexOfChild(textView)

            // Buat EditText baru
            val editText = EditText(context).apply {
                setText(textView.text)
                hint = "Ketikkan yang baru"
                inputType = android.text.InputType.TYPE_CLASS_TEXT
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setHintTextColor(ContextCompat.getColor(context, R.color.grey))
                typeface = ResourcesCompat.getFont(context, R.font.andikaregular)

                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(10, 5, 10, 5)
                }
            }

            // Ganti TextView dengan EditText
            parent.removeViewAt(index)
            parent.addView(editText, index)

            // Fokuskan EditText dan tampilkan keyboard
            editText.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)

            // Ketika kehilangan fokus, kembalikan ke TextView
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    textView.text = editText.text
                    parent.removeViewAt(index)
                    parent.addView(textView, index)
                    imm.hideSoftInputFromWindow(editText.windowToken, 0)
                }
            }

            // Saat tombol "Done" ditekan
            editText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    textView.text = editText.text
                    parent.removeViewAt(index)
                    parent.addView(textView, index)
                    imm.hideSoftInputFromWindow(editText.windowToken, 0)
                    true
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
                    Toast.makeText(requireContext(), "Ganti profil berhasil!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("EditProfile", "Error updating user profile in Firestore", e)
                    Toast.makeText(requireContext(), "Ganti profil gagal!", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun saveImageToInternalStorage(selectedImageUri: Uri) {
        Log.d("Selected Image", "$selectedImageUri")
        val inputStream: InputStream? = context?.contentResolver?.openInputStream(selectedImageUri)
        inputStream?.let { input ->
            try {
                // Create a file in internal storage
                val randomFileName = "${System.currentTimeMillis()}.jpg"
                val file = File(requireContext().filesDir, randomFileName)
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
