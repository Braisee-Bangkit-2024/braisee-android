package com.braille.braisee.helper

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.braille.braisee.ml.Braille
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ImageClassifierHelper(private val context: Context) {

    private var model: Braille? = null
    private val imageSize = 28 // Adjust based on your model input size

    init {
        // Load the model
        model = Braille.newInstance(context)
    }

    fun classifyImage(image: Bitmap): String {
        // Preprocess the image before classification
        val resizedImage = Bitmap.createScaledBitmap(image, imageSize, imageSize, true)
        val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        for (y in 0 until imageSize) {
            for (x in 0 until imageSize) {
                val pixel = resizedImage.getPixel(x, y)
                val r = ((pixel shr 16) and 0xFF) / 255.0f
                byteBuffer.putFloat(r) // Using red channel for grayscale image
            }
        }

        // Create TensorBuffer for input
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 28, 28, 1), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

        // Perform inference with the model
        val outputs = model?.process(inputFeature0)
        val outputFeature0 = outputs?.outputFeature0AsTensorBuffer
        val confidences = outputFeature0?.floatArray ?: return "Unable to recognize"

        // Interpret the inference results
        return interpretBrailleOutput(confidences)
    }

    private fun interpretBrailleOutput(confidences: FloatArray): String {
        // Find the index with the maximum confidence
        val maxIndex = confidences.indices.maxByOrNull { confidences[it] } ?: -1
        val brailleAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" // For direct mapping to alphabet

        // If the max index is within the range of the alphabet
        return if (maxIndex in brailleAlphabet.indices) {
            "Braille Translation: ${brailleAlphabet[maxIndex]}"
        } else {
            "Unable to recognize"
        }
    }

    fun close() {
        model?.close() // Make sure to close the model to release resources
    }
}