package com.example.playsnapui.ui.gallery.scroll

import android.database.Cursor
import android.graphics.Rect
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentScrollGaleryBinding

class ScrollGalleryFragment : Fragment() {

    private var _binding: FragmentScrollGaleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ScrollGalleryAdapter
    private val imagePaths = mutableListOf<String>()
    private val selectedItems = mutableSetOf<Int>() // Stores checked images' positions

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScrollGaleryBinding.inflate(inflater, container, false)
        val view = binding.root

        setupRecyclerView()
        loadRecentImages()
        setupButtons()

        return view
    }

    private fun setupRecyclerView() {
        binding.recentRecyclerPopgame.layoutManager = GridLayoutManager(requireContext(), 3)

        adapter = ScrollGalleryAdapter(imagePaths) { position, isChecked ->
            if (isChecked) {
                selectedItems.add(position)
            } else {
                selectedItems.remove(position)
            }
        }
        binding.recentRecyclerPopgame.adapter = adapter

        val spacingInPixels = 16
        val columnCount = 3

        binding.recentRecyclerPopgame.setPadding(spacingInPixels, 0, spacingInPixels, 0)
        binding.recentRecyclerPopgame.clipToPadding = false

        binding.recentRecyclerPopgame.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val position = parent.getChildAdapterPosition(view)
                outRect.left = spacingInPixels / 2
                outRect.right = spacingInPixels / 2
                outRect.bottom = spacingInPixels

                if (position < columnCount) {
                    outRect.top = spacingInPixels
                }
            }
        })
    }

    private fun setupButtons() {
        binding.hapusButton.setOnClickListener {
            val sortedSelectedItems = selectedItems.sortedDescending()
            for (index in sortedSelectedItems) {
                imagePaths.removeAt(index)
            }
            selectedItems.clear()
            adapter.notifyDataSetChanged()
        }

        binding.btnSwitchLayout.setOnClickListener {
            val bundle = Bundle().apply {
                putStringArrayList("IMAGE_PATHS", ArrayList(imagePaths))
                putIntegerArrayList("SELECTED_ITEMS", ArrayList(selectedItems.toList())) // 🔥 FIXED
            }
            findNavController().navigate(R.id.action_ScrollGalleryFragment_to_SwipeGalleryFragment, bundle)
        }

    }

    private fun loadRecentImages() {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor: Cursor? = requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, sortOrder
        )

        cursor?.use {
            while (it.moveToNext()) {
                val imagePath = it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                imagePaths.add(imagePath)
                if (imagePaths.size >= 20) break
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
