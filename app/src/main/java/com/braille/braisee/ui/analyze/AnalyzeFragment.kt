package com.braille.braisee.ui.analyze

import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

        // Menampilkan gambar di ImageView
        displayImage(imageUri)

        // Ketika tombol "Analyze" ditekan
        binding.buttonAnalyz.setOnClickListener {
            classifyImage(imageUri)
        }

        // Ketika tombol Text-to-Speech ditekan
        binding.buttonTts.setOnClickListener {
            speakResult()
        }
    }

    private fun displayImage(imageUri: Uri) {
        binding.imageView2.setImageURI(imageUri)
    }

    private fun classifyImage(imageUri: Uri) {
        binding.progressBar.visibility = View.VISIBLE // Tampilkan progress bar
        binding.tvResult.text = ""

        // Jalankan dalam Coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Konversi URI menjadi file
                val file = File(imageUri.path ?: "")
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData("image", file.name, requestBody)

                // Panggil API
                val response = ApiClient.apiService.postImage(multipartBody)

                // Perbarui UI di thread utama
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.tvResult.text = response.character
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.tvResult.text = "Error: ${e.message}"
                    Log.e(TAG, "Upload failed: ${e.message}")
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
