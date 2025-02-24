package com.example.playsnapui.ui.`object`

import SharedData
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentObjectBinding

class ObjectFragment : Fragment() {

    private var _binding: FragmentObjectBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ObjectViewModel by viewModels()

    private lateinit var adapter: ObjectAdapter
    private val detectedObjects = SharedData.detectedObjects

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentObjectBinding.inflate(inflater, container, false)
        println("DetectedObjects in objectFragment: $detectedObjects")
        viewModel.setDetectedObjects(detectedObjects)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_ObjectFragment_to_HomeFragment)
        }

        // Set up RecyclerView
        adapter = ObjectAdapter(mutableListOf()) { position ->
            viewModel.removeObjectAt(position)
        }

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)  // 2 columns in grid
        binding.recyclerPopobject.layoutManager = gridLayoutManager
        binding.recyclerPopobject.adapter = adapter

        // Observe data from ViewModel
        viewModel.objects.observe(viewLifecycleOwner, Observer { newList ->
            println("New data received in Fragment: $newList")  // Debugging log
            adapter.updateData(newList)
        })

        binding.addButton.setOnClickListener {
            val newObject = binding.etSearchGame.text.toString().trim()
            if (newObject.isNotEmpty()) {
                viewModel.addObject(newObject)  // Add object to ViewModel
                binding.etSearchGame.text?.clear()  // Clear input after adding
            }
        }

        binding.mulaiButton.setOnClickListener {
            // Fetch recommended games from ViewModel based on detected objects
            val detectedObjects = viewModel.objects.value.orEmpty()
            viewModel.getRecommendedGames(detectedObjects)

            // Observe recommended games from ViewModel
            viewModel.recommendedGames.observe(viewLifecycleOwner, Observer { recommendedGames ->
                // Save the recommended games (Games objects) to SharedData
                SharedData.recommendedGames = recommendedGames
                Log.d("ObjectFragment", "Recommended games size: ${SharedData.recommendedGames.size}")

                // Navigate to the recommendation page
                findNavController().navigate(R.id.action_ObjectFragment_to_RecommendGameFragment)
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
