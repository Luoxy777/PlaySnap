package com.example.playsnapui.ui.report

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentReportSuccesBinding

class ReportSuccessFragment : Fragment() {

    private var _binding: FragmentReportSuccesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportSuccesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Handler(Looper.getMainLooper()).postDelayed({
            // Navigate to the home screen (adjust the destination ID if needed)
            findNavController().navigate(R.id.action_reportSuccessFragment_to_homeFragment)
        }, 3000)  // 3000 milliseconds = 3 seconds

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Mencegah memory leak
    }
}
