package com.example.playsnapui.ui.gallery.scroll

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playsnapui.R

class ScrollGalleryAdapter(
    private val imagePaths: MutableList<String>,
    private val onItemChecked: (Int, Boolean) -> Unit // Callback to notify selection
) : RecyclerView.Adapter<ScrollGalleryAdapter.GalleryViewHolder>() {

    private val selectedItems = mutableSetOf<Int>() // Store checked item positions

    inner class GalleryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val btnCheck: AppCompatButton = view.findViewById(R.id.btn_check)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_taken_photo, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val imagePath = imagePaths[position]

        // Load image using Glide
        Glide.with(holder.itemView.context).load(imagePath).centerCrop().into(holder.imageView)

        // Update button state based on selection
        val isSelected = selectedItems.contains(position)

        holder.btnCheck.apply {
            setBackgroundResource(R.drawable.btn_check) // Default background

            if (isSelected) {
                setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_check_24, 0, 0) // Show drawableTop
            } else {
                setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0) // Remove drawableTop
            }
        }

        // Handle click event for checking/unchecking
        holder.btnCheck.setOnClickListener {
            if (selectedItems.contains(position)) {
                selectedItems.remove(position) // Uncheck
            } else {
                selectedItems.add(position) // Check
            }
            notifyItemChanged(position) // Update UI
            onItemChecked(position, selectedItems.contains(position))
        }
    }

    override fun getItemCount(): Int = imagePaths.size

    // Function to remove selected items
    fun removeSelectedItems() {
        val newList = imagePaths.filterIndexed { index, _ -> !selectedItems.contains(index) }
        imagePaths.clear()
        imagePaths.addAll(newList)
        selectedItems.clear()
        notifyDataSetChanged()
    }
}
