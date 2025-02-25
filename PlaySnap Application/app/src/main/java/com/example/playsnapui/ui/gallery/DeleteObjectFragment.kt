package com.example.playsnapui.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.playsnapui.databinding.LayoutDeleteObjectConfirmationBinding
import com.example.playsnapui.ui.gallery.scroll.OnDeleteConfirmedListener
import com.example.playsnapui.ui.gallery.scroll.ScrollGalleryFragment

class DeleteObjectFragment : DialogFragment() {

    private lateinit var binding: LayoutDeleteObjectConfirmationBinding
    private var listener: OnDeleteConfirmedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutDeleteObjectConfirmationBinding.inflate(inflater, container, false)

        // "No" button click closes the dialog without action
        binding.btnNo.setOnClickListener {
            dismiss()
        }

        // "Yes" button click calls the listener's method
        binding.btnYes.setOnClickListener {
            listener?.onDeleteConfirmed()  // This triggers the deletion in the ScrollGalleryFragment
            dismiss() // Close the dialog after confirming
        }

        return binding.root
    }

    // This is where you set the listener from ScrollGalleryFragment
    fun setDeleteConfirmedListener(listener: OnDeleteConfirmedListener) {
        this.listener = listener
    }

    companion object {
        fun newInstance(): DeleteObjectFragment {
            return DeleteObjectFragment()
        }
    }
}

