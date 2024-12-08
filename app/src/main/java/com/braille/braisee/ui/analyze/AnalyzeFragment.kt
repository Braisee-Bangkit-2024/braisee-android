package com.braille.braisee.ui.analyze

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.braille.braisee.api.ApiClient
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

        // Initialize Text-to-Speech
        textToSpeech = TextToSpeech(requireContext(), this)

        // Get Image URI from SafeArgs
        val args = arguments?.let { AnalyzeFragmentArgs.fromBundle(it) }
        val imageUriString = args?.imageUri ?: ""
        if (imageUriString.isEmpty()) {
            Toast.makeText(requireContext(), "Image URI not found!", Toast.LENGTH_SHORT).show()
            return
        }
        val imageUri = Uri.parse(imageUriString)

        // Display the selected image
        displayImage(imageUri)

        // Initialize DAO for accessing the analysis history database
        val database = AppDatabase.getDatabase(requireContext())
        val analyzeDao = database.analyzeHistoryDao()

        // Set button click listeners
        binding.buttonAnalyz.setOnClickListener {
            classifyImage(imageUri)
        }
        binding.buttonTts.setOnClickListener {
            speakResult()
        }
        binding.btnSave.setOnClickListener {
            saveAnalyzeResult(imageUri, analyzeDao)
        }
    }

    private fun displayImage(imageUri: Uri) {
        binding.imageView2.setImageURI(imageUri)
    }

    private fun classifyImage(imageUri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvResult.text = ""

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = File(imageUri.path ?: "")
                if (!file.exists()) {
                    Log.e(TAG, "File not found: ${file.absolutePath}")
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.tvResult.text = "Error: File not found."
                    }
                    return@launch
                }

                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData("image", file.name, requestBody)

                val response = ApiClient.apiService.postImage(multipartBody)

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.tvResult.text = "Character: ${response.character}"
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
            Toast.makeText(requireContext(), "No text to speak!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveAnalyzeResult(imageUri: Uri, analyzeHistoryDao: AnalyzeHistoryDao) {
        val resultText = binding.tvResult.text.toString()
        val bitmap = getBitmapFromUri(imageUri)

        if (bitmap != null && resultText.isNotEmpty()) {
            val file = createUniqueImageFile()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, file.outputStream())

            val history = AnalyzeHistory(
                imageUri = Uri.fromFile(file).toString(),
                result = resultText,
                favorite = false
            )

            lifecycleScope.launch {
                analyzeHistoryDao.insertHistory(history)
                Toast.makeText(requireContext(), "Result saved successfully.", Toast.LENGTH_SHORT).show()
                binding.btnSave.isEnabled = false
            }
        } else {
            Toast.makeText(requireContext(), "Failed to save result.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding Bitmap: ${e.message}", e)
            null
        }
    }

    private fun createUniqueImageFile(): File {
        val uniqueFileName = "cropped_image_${System.currentTimeMillis()}.jpg"
        return File(requireContext().cacheDir, uniqueFileName)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale("id", "ID"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech.setLanguage(Locale.US) // Fallback to English
            }
        } else {
            Log.e(TAG, "Text-to-Speech initialization failed.")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }
}
