package com.example.playsnapui.ui.gallery.swipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.playsnapui.R

class SwipeGalleryAdapter(
    private val images: List<Int>, // List of image resource IDs
    private val onItemChecked: (Int, Boolean) -> Unit // Callback when item is checked/unchecked
) : RecyclerView.Adapter<SwipeGalleryAdapter.GalleryViewHolder>() {

    private val checkedState = BooleanArray(images.size) // Track checked state

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_taken_photo_bigger, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(images[position], position)
    }

    override fun getItemCount(): Int = images.size

    inner class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val checkButton: Button = itemView.findViewById(R.id.btn_check)

        fun bind(imageRes: Int, position: Int) {
            imageView.setImageResource(imageRes) // Load image
            updateCheckButton(position)

            checkButton.setOnClickListener {
                checkedState[position] = !checkedState[position] // Toggle state
                onItemChecked(position, checkedState[position]) // Callback
                updateCheckButton(position)
            }
        }

        private fun updateCheckButton(position: Int) {
            if (checkedState[position]) {
                checkButton.setBackgroundResource(R.drawable.btn_check)
                checkButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_check_24, 0, 0)
            } else {
                checkButton.setBackgroundResource(R.drawable.btn_check)
                checkButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
        }
    }
}
