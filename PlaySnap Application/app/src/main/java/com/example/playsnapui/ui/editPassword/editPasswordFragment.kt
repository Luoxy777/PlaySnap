package com.example.playsnapui.ui.editPassword

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentEditPasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class editPasswordFragment : Fragment() {

    private lateinit var binding: FragmentEditPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    private lateinit var oldPassword: String
    private lateinit var newPassword: String
    private lateinit var confirmPassword: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditPasswordBinding.inflate(inflater, container, false)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser!!

        //Initialize view
        binding.tvEdit1OldPassFill.text = ""
        binding.tvEdit2NewPassFill.text = ""
        binding.tvEdit3NewPassConfirmFill.text = ""

        // Handle back button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Handle password change submission
        binding.sendButton.setOnClickListener {
            changePassword()
            findNavController().navigateUp()
        }

        makeEditableOnClick(binding.tvEdit1OldPassFill, "Ketikkan Password Lama")
        makeEditableOnClick(binding.tvEdit2NewPassFill, "Ketikkan Password Baru")
        makeEditableOnClick(binding.tvEdit3NewPassConfirmFill, "Konfirmasi Password Baru")

        return binding.root
    }

    private fun makeEditableOnClick(textView: TextView, hintText: String) {
        textView.setOnClickListener {
            // Create an EditText dynamically and set the text from TextView
            val editText = EditText(requireContext())
            editText.setText(textView.text)


            // Optionally set properties like hint or input type
            editText.hint = hintText
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
                    textView.text = getMaskedPassword(editText.text.toString())

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
                    textView.text = getMaskedPassword(editText.text.toString())

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

    private fun getMaskedPassword(password: String): String {
        return "*".repeat(password.length)
    }

    private fun changePassword() {
        val oldPassword = binding.etOldPass.text.toString()
        val newPassword = binding.etNewPass.text.toString()
        val confirmPassword = binding.etNewPassConfirm.text.toString()

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(requireContext(), "New passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Reauthenticate the user to validate the old password
        val credential = EmailAuthProvider.getCredential(currentUser.email!!, oldPassword)
        currentUser.reauthenticate(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update password if old password is correct
                    currentUser.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Failed to change password", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "Old password is incorrect", Toast.LENGTH_SHORT).show()
                }
            }
    }

}