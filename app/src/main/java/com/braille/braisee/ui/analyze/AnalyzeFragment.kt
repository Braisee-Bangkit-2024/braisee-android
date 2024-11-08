package com.braille.braisee.ui.analyze

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.braille.braisee.databinding.FragmentAnalyzeBinding

class AnalyzeFragment : Fragment() {
    private var _binding: FragmentAnalyzeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyzeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Menggunakan SafeArgs untuk mengambil imageUri yang diteruskan dari HomeFragment
        val args = AnalyzeFragmentArgs.fromBundle(requireArguments())
        val imageUriString = args.imageUri // Ambil imageUri yang diterima
        imageUriString.let {
            val imageUri = Uri.parse(it)
            displayImage(imageUri)
        }
    }

    private fun displayImage(imageUri: Uri) {
        binding.imageView2.setImageURI(imageUri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
