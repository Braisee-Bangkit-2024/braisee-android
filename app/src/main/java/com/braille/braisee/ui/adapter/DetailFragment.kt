package com.braille.braisee.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.content.pm.ActivityInfo
import com.braille.braisee.data.learn.Module
import com.braille.braisee.databinding.FragmentDetailBinding

class DetailFragment : Fragment() {

    private var fullscreenView: View? = null
    private var fullscreenCallback: WebChromeClient.CustomViewCallback? = null
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using View Binding
        _binding = FragmentDetailBinding.inflate(inflater, container, false)

        // Get the module passed through arguments
        val module = arguments?.getParcelable<Module>("module")

        module?.let {
            // Set title and description
            binding.tvJudulDetail.text = it.title
            binding.tvDeskripsiDetail.text = it.description

            // Configure WebView
            with(binding.wvDetail) {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                // Fullscreen WebView configuration
                webChromeClient = object : WebChromeClient() {
                    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                        val activity = activity as? AppCompatActivity ?: return
                        if (fullscreenView != null) {
                            callback?.onCustomViewHidden()
                            return
                        }
                        fullscreenView = view
                        fullscreenCallback = callback

                        // Enter fullscreen mode
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        activity.supportActionBar?.hide()
                        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

                        activity.findViewById<ViewGroup>(android.R.id.content).addView(view)
                    }

                    override fun onHideCustomView() {
                        val activity = activity as? AppCompatActivity ?: return
                        if (fullscreenView == null) return

                        // Exit fullscreen mode
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        activity.supportActionBar?.show()
                        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

                        activity.findViewById<ViewGroup>(android.R.id.content).removeView(fullscreenView)
                        fullscreenView = null
                        fullscreenCallback?.onCustomViewHidden()
                        fullscreenCallback = null
                    }
                }

                // Load YouTube video
                val videoUrl =
                    "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/${extractYouTubeId(it.ytLink)}\" frameborder=\"0\" allowfullscreen></iframe>"
                loadData(videoUrl, "text/html", "utf-8")
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding reference to avoid memory leaks
    }

    // Extract YouTube video ID from URL
    private fun extractYouTubeId(url: String): String {
        val regex = "v=([^&]*)".toRegex()
        val match = regex.find(url)
        return match?.groupValues?.get(1) ?: ""
    }
}
