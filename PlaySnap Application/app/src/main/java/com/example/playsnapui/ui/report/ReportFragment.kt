package com.example.playsnapui.ui.report

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentReportBinding

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ReportViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi ViewModel
        viewModel = ViewModelProvider(this)[ReportViewModel::class.java]

        binding.btnSend.setOnClickListener {
            val reportText = binding.etHelpreq.text.toString().trim()

            if (reportText.isNotEmpty()) {
                viewModel.saveReportToFirestore(
                    reportText,
                    onSuccess = {
                        // Hanya pindah ke ReportSuccessFragment jika laporan berhasil disimpan
                        Toast.makeText(requireContext(), "Laporan berhasil dikirim!", Toast.LENGTH_SHORT).show()
                        binding.etHelpreq.text?.clear() // Hapus teks setelah dikirim
                        findNavController().navigate(R.id.action_ReportFragment_to_ReportSuccessFragment)
                    },
                    onFailure = { exception ->
                        Toast.makeText(requireContext(), "Gagal mengirim: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(requireContext(), "Teks tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_ReportFragment_to_ProfileFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Mencegah memory leak
    }
}
