package com.braille.braisee.ui.analyze

import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.braille.braisee.databinding.FragmentAnalyzeBinding
import com.braille.braisee.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.util.Locale

class AnalyzeFragment : Fragment(), ImageClassifierHelper.ClassifierListener, TextToSpeech.OnInitListener {
    private var _binding: FragmentAnalyzeBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageClassifierHelper: ImageClassifierHelper
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

        // Menggunakan SafeArgs untuk mengambil imageUri yang diteruskan dari HomeFragment
        val args = AnalyzeFragmentArgs.fromBundle(requireArguments())
        val imageUriString = args.imageUri
        val imageUri = Uri.parse(imageUriString)

        // Menampilkan gambar yang dipilih
        displayImage(imageUri)

        // Inisialisasi ImageClassifierHelper
        imageClassifierHelper = ImageClassifierHelper(
            context = requireContext(),
            classifierListener = this // Mengimplementasikan callback listener
        )

        // Ketika tombol analisis ditekan
        binding.buttonAnalyz.setOnClickListener {
            imageClassifierHelper.classifyStaticImage(imageUri)
        }

        // Ketika tombol Text-to-Speech ditekan
        binding.buttonTts.setOnClickListener {
            speakResult()
        }
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

    // Callback hasil klasifikasi
    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
        if (results.isNullOrEmpty()) {
            binding.tvResult.text = "No results found"
            Log.d(TAG, "No classifications returned.")
            return
        }

        // Mengambil hasil klasifikasi terbaik
        val topResult = results[0].categories.firstOrNull()
        if (topResult != null) {
            val label = topResult.label
            val confidence = String.format("%.2f%%", topResult.score * 100)
            val resultText = "Label: $label\nConfidence: $confidence"
            binding.tvResult.text = resultText
            Log.d(TAG, "Classification Result: $resultText")
        } else {
            binding.tvResult.text = "No top result found"
            Log.d(TAG, "No top result found.")
        }
    }

    override fun onError(error: String) {
        // Menampilkan error di log dan UI
        binding.tvResult.text = "Error: $error"
        Log.e(TAG, error)
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
