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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.braille.braisee.data.AnalyzeHistory
import com.braille.braisee.data.AnalyzeHistoryDao
import com.braille.braisee.data.AppDatabase
import com.braille.braisee.databinding.FragmentAnalyzeBinding
import com.braille.braisee.factory.ViewModelFactory
import com.braille.braisee.helper.ImageClassifierHelper
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class AnalyzeFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentAnalyzeBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageClassifierHelper: ImageClassifierHelper
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

        // Menggunakan SafeArgs untuk mengambil imageUri yang diteruskan dari HomeFragment
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

    private fun loadHistoryItem(historyId: Int) {
        viewModel.getHistoryById(historyId).observe(viewLifecycleOwner){ history ->
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


    private fun displayImage(imageUri: Uri) {
        binding.imageView2.setImageURI(imageUri)
    }

    private fun speakResult() {
        val resultText = binding.tvResult.text.toString()
        if (resultText.isNotEmpty()) {
            textToSpeech.speak(resultText, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.d(TAG, "No text to speak.")
        }
    }

    private fun classifyImage(imageUri: Uri) {
        val bitmap = getBitmapFromUri(imageUri)
        bitmap?.let {
            val result = imageClassifierHelper.classifyImage(it)
            binding.tvResult.text = result
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
        imageClassifierHelper.close() // Menutup model saat fragment dihancurkan
    }
}
