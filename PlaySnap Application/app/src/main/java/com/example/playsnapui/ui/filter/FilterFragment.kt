package com.example.playsnapui.ui.filter

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.playsnapui.R
import com.example.playsnapui.databinding.BottomSheetFilterPageBinding
import com.example.playsnapui.databinding.FragmentFilterBinding
import com.example.playsnapui.databinding.FragmentHomeBinding

//class FilterFragment : Fragment() {
//
//    private var _binding: FragmentFilterBinding? = null
//    private var bottomSheetBinding: BottomSheetFilterPageBinding? = null
//
//    private val binding get() = _binding!!
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentFilterBinding.inflate(inflater, container, false)
//        bottomSheetBinding = BottomSheetFilterPageBinding.bind(binding.bottomSheetFilterPage)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Set listener untuk tombol usia
//        bottomSheetBinding?.usiaOpt1Btn?.setOnClickListener {
//            binding.usiaValue.text = "<6 th"
//        }
//        bottomSheetBinding?.usiaOpt2Btn?.setOnClickListener {
//            binding.usiaValue.text = "6 - 10 th"
//        }
//        bottomSheetBinding?.usiaOpt3Btn?.setOnClickListener {
//            binding.usiaValue.text = ">10 th"
//        }
//
//        // Set listener untuk tombol lokasi
//        bottomSheetBinding?.lokasiOpt1Btn?.setOnClickListener {
//            binding.lokasiValue.text = "Indoor"
//        }
//        bottomSheetBinding?.lokasiOpt2Btn?.setOnClickListener {
//            binding.lokasiValue.text = "Outdoor"
//        }
//
//        // Set listener untuk tombol jumlah pemain
//        bottomSheetBinding?.pemainOpt1Btn?.setOnClickListener {
//            binding.pemainValue.text = "<3 org"
//        }
//        bottomSheetBinding?.pemainOpt2Btn?.setOnClickListener {
//            binding.pemainValue.text = "3 - 5 org"
//        }
//        bottomSheetBinding?.pemainOpt3Btn?.setOnClickListener {
//            binding.pemainValue.text = ">5 org"
//        }
//
//        // Set listener untuk tombol properti
//        bottomSheetBinding?.propertiOpt1Btn?.setOnClickListener {
//            binding.propertiValue.text = "Ya"
//        }
//        bottomSheetBinding?.propertiOpt2Btn?.setOnClickListener {
//            binding.propertiValue.text = "Tidak"
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//        bottomSheetBinding = null
//    }
//}

class FilterFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private var bottomSheetBinding: BottomSheetFilterPageBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)

        // INI PERBAIKANNYA: Inflate BottomSheet secara mandiri
        bottomSheetBinding = BottomSheetFilterPageBinding.inflate(inflater, binding.bottomSheetFilterPage, true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_FilterFragment_to_HomeFragment)
        }

        bottomSheetBinding?.let { sheetBinding ->
            // Set listener untuk tombol usia
            sheetBinding.usiaOpt1Btn.setOnClickListener {
                binding.usiaValue.text = "<6 th"
            }
            sheetBinding.usiaOpt2Btn.setOnClickListener {
                binding.usiaValue.text = "6 - 10 th"
            }
            sheetBinding.usiaOpt3Btn.setOnClickListener {
                binding.usiaValue.text = ">10 th"
            }

            // Set listener untuk tombol lokasi
            sheetBinding.lokasiOpt1Btn.setOnClickListener {
                binding.lokasiValue.text = "Indoor"
            }
            sheetBinding.lokasiOpt2Btn.setOnClickListener {
                binding.lokasiValue.text = "Outdoor"
            }

            // Set listener untuk tombol jumlah pemain
            sheetBinding.pemainOpt1Btn.setOnClickListener {
                binding.pemainValue.text = "<3 org"
            }
            sheetBinding.pemainOpt2Btn.setOnClickListener {
                binding.pemainValue.text = "3 - 5 org"
            }
            sheetBinding.pemainOpt3Btn.setOnClickListener {
                binding.pemainValue.text = ">5 org"
            }

            // Set listener untuk tombol properti
            sheetBinding.propertiOpt1Btn.setOnClickListener {
                binding.propertiValue.text = "Ya"
            }
            sheetBinding.propertiOpt2Btn.setOnClickListener {
                binding.propertiValue.text = "Tidak"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        bottomSheetBinding = null
    }
}
