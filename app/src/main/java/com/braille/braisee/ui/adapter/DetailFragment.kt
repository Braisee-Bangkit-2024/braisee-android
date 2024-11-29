package com.braille.braisee.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.content.pm.ActivityInfo
import com.braille.braisee.R
import com.braille.braisee.data.learn.Module

class DetailFragment : Fragment() {

    private var fullscreenView: View? = null
    private var fullscreenCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detail, container, false)

        val title = view.findViewById<TextView>(R.id.tv_judul_detail)
        val description = view.findViewById<TextView>(R.id.tv_deskripsi_detail)
        val webView = view.findViewById<WebView>(R.id.wv_detail)

        val module = arguments?.getParcelable<Module>("module")

        module?.let {
            title.text = it.title
            description.text = it.description

            // Enable JavaScript and DOM Storage
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true

            // Fullscreen Mode
            webView.webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    val activity = activity as? AppCompatActivity ?: return
                    if (fullscreenView != null) {
                        callback?.onCustomViewHidden()
                        return
                    }
                    fullscreenView = view
                    fullscreenCallback = callback

                    // Enter Fullscreen
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    activity.supportActionBar?.hide()
                    activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

                    activity.findViewById<ViewGroup>(android.R.id.content).addView(view)
                }

                override fun onHideCustomView() {
                    val activity = activity as? AppCompatActivity ?: return
                    if (fullscreenView == null) return

                    // Exit Fullscreen
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    activity.supportActionBar?.show()
                    activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

                    activity.findViewById<ViewGroup>(android.R.id.content).removeView(fullscreenView)
                    fullscreenView = null
                    fullscreenCallback?.onCustomViewHidden()
                    fullscreenCallback = null
                }
            }

            // Embed YouTube Video
            val videoUrl = "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/${extractYouTubeId(it.ytLink)}\" frameborder=\"0\" allowfullscreen></iframe>"
            webView.loadData(videoUrl, "text/html", "utf-8")
        }

        return view
    }

    // Extract YouTube video ID from URL
    private fun extractYouTubeId(url: String): String {
        val regex = "v=([^&]*)".toRegex()
        val match = regex.find(url)
        return match?.groupValues?.get(1) ?: ""
    }
}
