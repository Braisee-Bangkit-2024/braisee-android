package com.braille.braisee.helper

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.graphics.ImageDecoder
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import com.braille.braisee.R
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

@Suppress("DEPRECATION")
class ImageClassifierHelper(
    private var threshold: Float = 0.1f,
    private var maxResults: Int = 3,
    private val modelName: String = "cancer_classification.tflite",
    private val context: Context,
    private val classifierListener: ClassifierListener?
) {
    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResults)

        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(4)
        // Uncomment below to use GPU if available
        //.useGpu()
        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                modelName,
                optionsBuilder.build()
            )
            Log.d(TAG, "Image Classifier Initialized Successfully")
        } catch (e: Exception) {
            val errorMessage = context.getString(R.string.image_classifier_failed) + ": ${e.message}"
            classifierListener?.onError(errorMessage)
            Log.e(TAG, errorMessage, e)
        }
    }

    fun classifyStaticImage(imageUri: Uri) {
        if (imageClassifier == null) {
            setupImageClassifier()
        }

        try {
            val bitmap = uriToBitmap(imageUri)
            val tensorImage = processImage(bitmap)
            val results = classifyImage(tensorImage)
            notifyListener(results)
        } catch (e: Exception) {
            val errorMessage = "Failed to classify image: ${e.message}"
            classifierListener?.onError(errorMessage)
            Log.e(TAG, errorMessage, e)
        }
    }

    private fun uriToBitmap(imageUri: Uri): Bitmap {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }.copy(Bitmap.Config.ARGB_8888, true)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to convert Uri to Bitmap: ${e.message}", e)
        }
    }

    private fun classifyImage(tensorImage: TensorImage): List<Classifications>? {
        val startTime = SystemClock.uptimeMillis()
        val results = imageClassifier?.classify(tensorImage)
        val elapsedTime = SystemClock.uptimeMillis() - startTime
        Log.d(TAG, "Inference Time: ${elapsedTime}ms")
        return results
    }

    private fun processImage(bitmap: Bitmap): TensorImage {
        return ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .add(CastOp(DataType.UINT8))
            .build()
            .process(TensorImage.fromBitmap(bitmap))
    }

    private fun notifyListener(results: List<Classifications>?) {
        classifierListener?.onResults(results, 0)
    }

    companion object {
        private const val TAG = "ImageClassifierHelper"
    }

    interface ClassifierListener {
        fun onError(error: String)
        fun onResults(results: List<Classifications>?, inferenceTime: Long)
    }
}
