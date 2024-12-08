package com.braille.braisee.ui.analyze

import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.braille.braisee.data.AnalyzeHistory
import com.braille.braisee.data.AnalyzeHistoryDao
import com.braille.braisee.data.AppDatabase
import com.braille.braisee.databinding.FragmentAnalyzeBinding
import com.braille.braisee.helper.ImageClassifierHelper
import kotlinx.coroutines.launch
import com.braille.braisee.api.ApiClient
import com.braille.braisee.databinding.FragmentAnalyzeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

        // Inisialisasi Text-to-Speech
        textToSpeech = TextToSpeech(requireContext(), this)

        // Mendapatkan URI gambar dari SafeArgs
        val args = AnalyzeFragmentArgs.fromBundle(requireArguments())
        val imageUriString = args.imageUri
        val imageUri = Uri.parse(imageUriString)


        // Menampilkan gambar yang dipilih
        displayImage(imageUri)

        //  Inisialisasi DAO untuk mengakses riwayat analisis dari database
        val database = AppDatabase.getDatabase(requireContext())
        val analyzeDao = database.analyzeHistoryDao()

        // Inisialisasi ImageClassifierInterpreter
        imageClassifierHelper = ImageClassifierHelper(requireContext())

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
            outputStream?.close()
        }

        if (uniqueImageUri.toString().isNotEmpty() && resultText.isNotEmpty())  {
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

    private fun createUniqueImageUri(): Uri {
        val uniqueFileName = "cropped_image_${System.currentTimeMillis()}.jpg"
        val file = File(requireContext().cacheDir, uniqueFileName)
        return Uri.fromFile(file)
    }


    private fun displayImage(imageUri: Uri) {
        binding.imageView2.setImageURI(imageUri)
    }

    private fun classifyImage(imageUri: Uri) {
        binding.progressBar.visibility = View.GONE // Tampilkan progress bar
        binding.tvResult.text = ""

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = File(imageUri.path ?: "")

                // Debugging: Cek keberadaan dan detail file
                if (!file.exists()) {
                    Log.e(TAG, "File tidak ditemukan: ${file.absolutePath}")
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.tvResult.text = "Error: File tidak ditemukan."
                    }
                    return@launch
                }
                Log.d(TAG, "File ditemukan: ${file.absolutePath}, ukuran: ${file.length()} bytes")

                val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                val multipartBody = MultipartBody.Part.createFormData("image", file.name, requestBody)

                // Panggil API
                val response = ApiClient.apiService.postImage(multipartBody)

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.tvResult.text = "Karakter: ${response.character}"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.tvResult.text = "Error: ${e.message}"
                    Log.e(TAG, "Upload failed: ${e.message}", e)
                }
            }
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
