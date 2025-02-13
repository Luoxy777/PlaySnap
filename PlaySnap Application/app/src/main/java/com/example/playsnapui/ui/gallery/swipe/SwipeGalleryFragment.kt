package com.example.playsnapui.ui.gallery.swipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.app.ui.gallery.swipe.SwipeGalleryViewModel
import com.example.playsnapui.databinding.FragmentSwipeGalleryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SwipeGalleryFragment : Fragment() {

    private var _binding: FragmentSwipeGalleryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SwipeGalleryViewModel by viewModels()
    private lateinit var adapter: SwipeGalleryAdapter
    private val selectedItems = mutableSetOf<Int>() // Combined selected items
    private val scrollCheckedItems = mutableSetOf<Int>() // Selected items from ScrollGalleryFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSwipeGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize adapter with data from arguments
        arguments?.let {
            val receivedImages = (it.getStringArrayList("IMAGE_PATHS") ?: emptyList()).toMutableList()
            val receivedCheckedItems = it.getIntegerArrayList("SELECTED_ITEMS") ?: emptyList()

            // Store the checked items from ScrollGalleryFragment
            selectedItems.addAll(receivedCheckedItems)

            // Initialize adapter
            adapter = SwipeGalleryAdapter(receivedImages) { position, isChecked ->
                if (isChecked) {
                    selectedItems.add(position)
                } else {
                    selectedItems.remove(position)
                }
            }

            // Restore checked items (both from Scroll and Swipe)
            selectedItems.forEach { pos -> adapter.setChecked(pos, true) }
        }

        // Set up ViewPager and listeners
        binding.viewPager.adapter = adapter
        setupListeners()

        // Observe ViewModel
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnSwitchLayout.setOnClickListener {
            // Combine checked items from Scroll and Swipe
            val combinedCheckedItems = selectedItems

            // Send back the combined checked items
            val resultBundle = Bundle().apply {
                putStringArrayList("IMAGE_PATHS", ArrayList(adapter.getCurrentImagePaths()))
                putIntegerArrayList("SELECTED_ITEMS", ArrayList(combinedCheckedItems.toList()))
            }

            setFragmentResult("SWIPE_TO_SCROLL", resultBundle)
            findNavController().navigateUp()
        }

        binding.swipeLeft.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem > 0) {
                binding.viewPager.currentItem = currentItem - 1
            }
        }

        binding.swipeRight.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < adapter.itemCount - 1) {
                binding.viewPager.currentItem = currentItem + 1
            }
        }

        binding.hapusButton.setOnClickListener {
            val updatedList = adapter.getCurrentImagePaths().toMutableList()
            val sortedSelectedItems = selectedItems.sortedDescending()

            // Delete selected items
            for (index in sortedSelectedItems) {
                updatedList.removeAt(index)
            }

            // Update the selectedItems set
            selectedItems.clear()

            // Reinitialize the adapter with the updated imagePaths and selectedItems
            adapter = SwipeGalleryAdapter(updatedList) { position, isChecked ->
                selectedItems.remove(position)
            }

            // Set the adapter to the ViewPager
            binding.viewPager.adapter = adapter

            // Notify the adapter of changes
            adapter.notifyDataSetChanged()
        }
    }

    private fun observeViewModel() {
        viewModel.images.observe(viewLifecycleOwner, Observer { images ->
            adapter.submitList(images)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}