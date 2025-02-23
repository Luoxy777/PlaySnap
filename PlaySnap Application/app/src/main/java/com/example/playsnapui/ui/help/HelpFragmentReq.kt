package com.example.playsnapui.ui.help

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentHelpFragmentReqBinding

class HelpFragmentReq : Fragment() {

    private var _binding: FragmentHelpFragmentReqBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HelpFragmentReqViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpFragmentReqBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi ViewModel dengan cara yang lebih direkomendasikan
        viewModel = ViewModelProvider(this)[HelpFragmentReqViewModel::class.java]

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()  // Kembali ke fragment sebelumnya
        }

        binding.btnSend.setOnClickListener {
            val questionsText = binding.etHelpreq.text.toString().trim()

            if (questionsText.isNotEmpty()) {
                viewModel.saveQuestionsToFirestore(
                    questionsText,
                    onSuccess = {
                        // Hanya pindah ke ReportSuccessFragment jika laporan berhasil disimpan
                        Toast.makeText(requireContext(), "Pertanyaan berhasil dikirim!", Toast.LENGTH_SHORT).show()
                        binding.etHelpreq.text?.clear() // Hapus teks setelah dikirim
                        findNavController().navigate(R.id.action_HelpFragmentReq_to_HelpFragmentReqSuccess)
                    },
                    onFailure = { exception ->
                        Toast.makeText(requireContext(), "Gagal mengirim: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(requireContext(), "Teks tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Mencegah memory leak
    }
}
