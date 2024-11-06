package com.braille.braisee.ui.analyze

import android.graphics.BitmapFactory
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

        // Ambil URI gambar dari argumen dan tampilkan di ImageView
        val imageUriString = arguments?.getString("imageUri")
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            displayImage(imageUri)
        }
    }

    private fun displayImage(uri: Uri) {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        binding.imageView2.setImageBitmap(bitmap) // Pastikan ID ImageView di layout sesuai
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}