package com.example.playsnapui.ui.gallery.swipe
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.Observer
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.app.ui.gallery.SwipeGalleryViewModel
//import com.example.playsnapui.databinding.FragmentSwipeGalleryBinding
//import dagger.hilt.android.AndroidEntryPoint
//
//@AndroidEntryPoint
//class SwipeGalleryFragment : Fragment() {
//
//    private var _binding: FragmentSwipeGalleryBinding? = null
//    private val binding get() = _binding!!
//    private val viewModel: SwipeGalleryViewModel by viewModels()
//    private lateinit var adapter: SwipeGalleryAdapter
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentSwipeGalleryBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        setupRecyclerView()
////        observeViewModel()
//    }
//
//    private fun setupRecyclerView() {
//        adapter = SwipeGalleryAdapter()
//        binding.viewPager.adapter = adapter
//    }
//
////    private fun observeViewModel() {
////        viewModel.images.observe(viewLifecycleOwner, Observer { images ->
//////            adapter.submitList(images)
////        })
////    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}