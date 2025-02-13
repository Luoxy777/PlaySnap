package com.example.playsnapui.ui.filter

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentFilterBinding
import com.example.playsnapui.databinding.FragmentHomeBinding

class FilterFragment : Fragment() {
    private var _binding: FragmentFilterBinding? = null

    companion object {
        fun newInstance() = FilterFragment()
    }

    private val binding get() = _binding!! // Safe call to avoid null reference
    private lateinit var viewModel: FilterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filter, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FilterViewModel::class.java)
        // TODO: Use the ViewModel
    }

}