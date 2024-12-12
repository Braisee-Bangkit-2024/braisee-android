package com.braille.braisee.ui.analyze

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.braille.braisee.data.AnalyzeHistory
import com.braille.braisee.data.AnalyzeHistoryDao
import com.braille.braisee.data.AppDatabase
import com.braille.braisee.databinding.FragmentAnalyzeBinding
import com.braille.braisee.factory.ViewModelFactory
import kotlinx.coroutines.launch
import com.braille.braisee.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.Locale

class AnalyzeFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentAnalyzeBinding? = null
    private val binding get() = _binding!!

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var viewModel: AnalyzeViewModel

    companion object {
        private const val TAG = "AnalyzeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyzeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi ViewModelFactory dan AnalyzeViewModel
        val factory = ViewModelFactory.getInstance(requireContext())
        viewModel = ViewModelProvider(this, factory)[AnalyzeViewModel::class.java]

        // Inisialisasi Text-to-Speech
        textToSpeech = TextToSpeech(requireContext(), this)

        // Mendapatkan URI gambar dari SafeArgs
        val args = AnalyzeFragmentArgs.fromBundle(requireArguments())
        val imageUriString = args.imageUri
        val imageUri = Uri.parse(imageUriString)

        val historyId = args.historyId

        if (historyId != 1) {
            loadHistoryItem(historyId)
        } else {
            val imageUri = Uri.parse(args.imageUri)
            displayImage(imageUri)
            binding.tvResult.text = args.result

            binding.btnSave.isEnabled = false
        }


        // Menampilkan gambar yang dipilih
        displayImage(imageUri)

        //  Inisialisasi DAO untuk mengakses riwayat analisis dari database
        val database = AppDatabase.getDatabase(requireContext())
        val analyzeDao = database.analyzeHistoryDao()


        // Ketika tombol analisis ditekan
        binding.buttonAnalyz.setOnClickListener {
            classifyImage(imageUri)
        }

        // Ketika tombol Text-to-Speech ditekan
        binding.buttonTts.setOnClickListener {
            speakResult()
        }

        binding.btnSave.setOnClickListener {
            saveAnalyzeResult(analyzeDao)
        }
    }

    private fun saveAnalyzeResult(analyzeHistoryDao: AnalyzeHistoryDao) {
//        val imageUriString = arguments?.let { AnalyzeFragmentArgs.fromBundle(it).imageUri } ?: ""
        val resultText = binding.tvResult.text.toString()
        val uniqueImageUri = createUniqueImageUri()

        // Pastikan gambar disalin ke file baru
        val bitmap =
            getBitmapFromUri(Uri.parse(arguments?.let { AnalyzeFragmentArgs.fromBundle(it).imageUri }))
        bitmap?.let {
            val outputStream = requireContext().contentResolver.openOutputStream(uniqueImageUri)
            it.compress(Bitmap.CompressFormat.JPEG, 100, outputStream!!)
            outputStream.close()
        }

        if (uniqueImageUri.toString().isNotEmpty() && resultText.isNotEmpty()) {
            val history = AnalyzeHistory(
                imageUri = uniqueImageUri.toString(),
                result = resultText,
                favorite = false
            )

            lifecycleScope.launch {
                analyzeHistoryDao.insertHistory(history)
                Toast.makeText(context, "Saved Analysis Results", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Result Saved: $resultText")

                // Menonaktifkan button Save ketika data sudah tersimpan ke Local
                binding.btnSave.isEnabled = false
            }
        } else {
            Toast.makeText(context, "No result to save.", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "No result to save.")
        }
    }
    private fun compressImageFile(context: Context, originalUri: Uri, maxFileSize: Long = 1_000_000): File? {
        return try {
            val bitmap = getBitmapFromUri(originalUri) ?: return null
            var quality = 100
            val compressedFile: File

            // Buat file sementara
            val tempFile = File(context.cacheDir, "compressed_image_${System.currentTimeMillis()}.jpg")
            do {
                // Tulis bitmap ke file dengan kualitas tertentu
                val outputStream = tempFile.outputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                outputStream.close()

                // Periksa ukuran file
                if (tempFile.length() <= maxFileSize) {
                    break
                }

                // Kurangi kualitas jika file terlalu besar
                quality -= 5
            } while (quality > 0)

            // Jika hasil kompresi memenuhi syarat, kembalikan file tersebut
            compressedFile = tempFile
            compressedFile
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image: ${e.message}")
            null
        }
    }

    private fun loadHistoryItem(historyId: Int) {
        viewModel.getHistoryById(historyId).observe(viewLifecycleOwner) { history ->
            if (history != null) {
                val imageUri = Uri.parse(history.imageUri)
                displayImage(imageUri)
                binding.tvResult.text = history.result

                // Disable tombol Save jika data sudah tersimpan
                binding.btnSave.isEnabled = false
            } else {
                Toast.makeText(requireContext(), "Data not found", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun createUniqueImageUri(): Uri {
        val uniqueFileName = "cropped_image_${System.currentTimeMillis()}.jpg"
        val file = File(requireContext().cacheDir, uniqueFileName)
        return Uri.fromFile(file)
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    private fun displayImage(imageUri: Uri) {
        binding.imageView2.setImageURI(imageUri)
    }

    private fun classifyImage(imageUri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvResult.text = ""

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val context = requireContext()
                val compressedFile = compressImageFile(context, imageUri)

                if (compressedFile == null || !compressedFile.exists()) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.tvResult.text = "Error: Gagal mengompres gambar."
                    }
                    return@launch
                }

                Log.d(TAG, "File dikompresi: ${compressedFile.absolutePath}, ukuran: ${compressedFile.length()} bytes")

                val requestBody = compressedFile.asRequestBody("image/*".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData("file", compressedFile.name, requestBody)

                if (isNetworkAvailable(context)) {
                    try {
                        val response = ApiClient.apiService.postImage(multipartBody)

                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                            binding.tvResult.text = response.character.joinToString("")
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                            binding.tvResult.text = "Maaf Ada masalah pada Server."
                            Log.e(TAG, "API request failed: ${e.message}", e)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.tvResult.text = "Error: Tidak Ada Internet."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.tvResult.text = "Error: ${e.message}"
                    Log.e(TAG, "Error during file handling or API request: ${e.message}", e)
                }
            }
        }
    }

    private fun getBitmapFromUri(imageUri: Uri): Bitmap? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            inputStream?.let { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting bitmap from URI: ${e.message}")
            null
        }
    }

    private fun speakResult() {
        val resultText = binding.tvResult.text.toString()
        if (resultText.isNotEmpty()) {
            textToSpeech.speak(resultText, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.d(TAG, "No text to speak.")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale("id", "ID"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Text-to-Speech: Bahasa Indonesia tidak didukung.")
            } else {
                Log.d(TAG, "Text-to-Speech berhasil diinisialisasi.")
            }
        } else {
            Log.e(TAG, "Text-to-Speech gagal diinisialisasi.")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hentikan dan shutdown Text-to-Speech
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }
}