package com.braille.braisee.ui.home

import android.app.Application
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.braille.braisee.data.AnalyzeHistory
import com.braille.braisee.data.AnalyzeHistoryDao
import com.braille.braisee.data.AnalyzeRepo
import com.braille.braisee.databinding.FragmentHomeBinding
import com.braille.braisee.factory.ViewModelFactory
import com.braille.braisee.ui.adapter.HistoryListAdapter
import com.yalantis.ucrop.UCrop
import java.io.File

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var currentImageUri: Uri? = null
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HistoryListAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory.getInstance(requireContext())
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        // Setup RecyclerView
        setupRecyclerView()

        // Observe LiveData untuk daftar history
        viewModel.allHistory.observe(viewLifecycleOwner) { historyList ->
            adapter.setData(historyList)
        }


        // Listener untuk tombol galeri dan kamera
        binding.scanGallery.setOnClickListener { startGallery() }
        binding.scanCamera.setOnClickListener { requestCameraPermission() }
//        val layoutManager = LinearLayoutManager(context)
//        binding.recyclerView.layoutManager = layoutManager
//        val itemDecoration = DividerItemDecoration(context, layoutManager.orientation)
//        binding.recyclerView.addItemDecoration(itemDecoration)
//
//        val factory = ViewModelFactory.getInstance(requireActivity())
//        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
//
//        val adapter = HistoryListAdapter{
//            analyzeHistory ->
//            viewModel.
//        }
//        binding.recyclerView.adapter = adapter

//        viewModel.listHistory.observe(viewLifecycleOwner) { historyList ->
//            adapter.setData(historyList)
//        }


    }

    private fun addBookmark(historyItem: AnalyzeHistory) {
        historyItem.favorite = true
        viewModel.updateHistory(historyItem)
        showToast("Bookmark ditambahkan.")
    }

    private fun removeBookmark(historyItem: AnalyzeHistory) {
        historyItem.favorite = false
        viewModel.updateHistory(historyItem)
        showToast("Bookmark dihapus.")
    }

    private fun isBookmarked(historyItem: AnalyzeHistory): Boolean {
        return historyItem.favorite
    }


    private fun setupRecyclerView() {
        adapter = HistoryListAdapter { history ->
            if (isBookmarked(history)) {
                removeBookmark(history)
            } else {
                addBookmark(history)
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            startUCrop(uri) // Memulai UCrop setelah memilih gambar dari galeri
        } else {
            Log.d("HomeFragment", "No media selected from gallery")
        }
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }

            else -> {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Izin kamera diperlukan untuk mengambil foto",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun startCamera() {
        val uri = createImageUri()
        uri?.let {
            currentImageUri = it
            cameraLauncher.launch(it)
        }
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success && currentImageUri != null) {
                startUCrop(currentImageUri!!) // Memulai UCrop setelah mengambil gambar dari kamera
            } else {
                Log.d("HomeFragment", "Image capture failed or canceled")
            }
        }

    private fun startUCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "cropped_image.jpg"))
        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1000, 1000)
            .start(requireContext(), this)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP && resultCode == AppCompatActivity.RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            resultUri?.let {
                navigateToAnalyzeFragment(it) // Mengirim URI hasil crop ke AnalyzeFragment
            } ?: showToast("Failed to crop image")
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            showToast("Crop error: ${cropError?.message}")
        }
    }

    private fun navigateToAnalyzeFragment(uri: Uri) {
        val action = HomeFragmentDirections.actionHomeToAnalyze(uri.toString())
        findNavController().navigate(action) // Menggunakan NavController dan SafeArgs untuk navigasi
    }

    private fun createImageUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "captured_image_${System.currentTimeMillis()}.jpg"
            )
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
