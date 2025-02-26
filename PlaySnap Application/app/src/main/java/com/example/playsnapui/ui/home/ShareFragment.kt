package com.example.playsnapui.ui.home
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.playsnapui.R

class ShareFragment(private val dynamicLink: String) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.pop_up_share, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val linkTextView: TextView = view.findViewById(R.id.link_text)
        val copyButton: ImageButton = view.findViewById(R.id.copy_button)
        val xButton: ImageButton = view.findViewById(R.id.x_button)
        // Set the dynamic link text
        linkTextView.text = dynamicLink

        // Copy link to clipboard when button is clicked
        copyButton.setOnClickListener {
            copyToClipboard(dynamicLink)
        }

        xButton.setOnClickListener {
            dismiss()
        }
    }

    private fun copyToClipboard(link: String) {
        val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Dynamic Link", link)
        clipboard.setPrimaryClip(clip)

        // Optionally, show a toast message to inform the user
        Toast.makeText(requireActivity(), "Link copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}