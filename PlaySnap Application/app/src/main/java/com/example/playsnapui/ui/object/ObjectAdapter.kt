package com.example.playsnapui.ui.`object`

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playsnapui.databinding.LayoutGeneratedObjectBinding

class ObjectAdapter(private val objects: MutableList<String>, private val onRemoveClick: (Int) -> Unit) :
    RecyclerView.Adapter<ObjectAdapter.ObjectViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjectViewHolder {
        val binding = LayoutGeneratedObjectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ObjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ObjectViewHolder, position: Int) {
        holder.bind(objects[position], position)
    }

    override fun getItemCount(): Int = objects.size

    fun updateData(newObjects: List<String>) {
        objects.clear()
        objects.addAll(newObjects)
        notifyDataSetChanged()
    }

    inner class ObjectViewHolder(private val binding: LayoutGeneratedObjectBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(name: String, position: Int) {
            binding.textView.text = name

            // Detect if the touch event happened on the right side (where the icon is)
            binding.textView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val iconStartX = binding.textView.width - binding.textView.compoundPaddingEnd
                    if (event.rawX >= iconStartX) {
                        onRemoveClick(position) // Remove item if clicked on the icon
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }
    }
}
