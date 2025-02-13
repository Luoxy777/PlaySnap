package com.example.playsnapui.ui.gallery.scroll

import android.database.Cursor
import android.graphics.Rect
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playsnapui.R

class ScrollGalleryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScrollGalleryAdapter
    private val imagePaths = mutableListOf<String>()
    private val selectedItems = mutableSetOf<Int>() // Stores checked images' positions

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scroll_galery, container, false)
        recyclerView = view.findViewById(R.id.recent_recycler_popgame)
        val hapusButton: Button = view.findViewById(R.id.hapus_button)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        loadRecentImages()
        adapter = ScrollGalleryAdapter(imagePaths) { position, isChecked ->
            if (isChecked) {
                selectedItems.add(position)
            } else {
                selectedItems.remove(position)
            }
        }
        recyclerView.adapter = adapter

        val spacingInPixels = 16
        val columnCount = 3

        val layoutManager = GridLayoutManager(requireContext(), columnCount)
        recyclerView.layoutManager = layoutManager
        recyclerView.setPadding(spacingInPixels, 0, spacingInPixels, 0)
        recyclerView.clipToPadding = false

        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val position = parent.getChildAdapterPosition(view)
                val itemCount = state.itemCount

                outRect.left = spacingInPixels / 2
                outRect.right = spacingInPixels / 2
                outRect.bottom = spacingInPixels

                if (position < columnCount) {
                    outRect.top = spacingInPixels
                }
            }
        })

        // Handle delete button click
        hapusButton.setOnClickListener {
            val sortedSelectedItems = selectedItems.sortedDescending()
            for (index in sortedSelectedItems) {
                imagePaths.removeAt(index)
            }
            selectedItems.clear()
            adapter.notifyDataSetChanged()
        }

        return view
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
}
