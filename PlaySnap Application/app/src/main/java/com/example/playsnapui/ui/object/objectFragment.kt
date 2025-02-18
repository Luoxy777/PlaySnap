package com.example.playsnapui.ui.`object`

import SharedData
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
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
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Set up RecyclerView
        adapter = ObjectAdapter(mutableListOf()) { position ->
            viewModel.removeObjectAt(position)
        }

        val gridLayoutManager = GridLayoutManager(requireContext(), 2) // Change '2' based on desired columns
        binding.recyclerPopobject.layoutManager = gridLayoutManager

//        binding.recyclerPopobject.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPopobject.adapter = adapter

        // Observe data from ViewModel
        viewModel.objects.observe(viewLifecycleOwner, Observer { newList ->
            println("New data received in Fragment: $newList") // Debugging log
            adapter.updateData(newList)
        })

        binding.addButton.setOnClickListener {
            val newObject = binding.etSearchGame.text.toString().trim()
            if (newObject.isNotEmpty()) {
                viewModel.addObject(newObject) // Add object to ViewModel
                binding.etSearchGame.text?.clear() // Clear input after adding
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}