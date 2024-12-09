package com.braille.braisee.ui.detail

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.braille.braisee.data.learn.Module
import com.braille.braisee.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var detailViewModel: DetailViewModel

    private var fullscreenView: View? = null
    private var fullscreenCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        detailViewModel = ViewModelProvider(this).get(DetailViewModel::class.java)

        // Observe the module LiveData to update UI
        detailViewModel.module.observe(this) { module ->
            module?.let {
                binding.tvJudulDetail.text = it.title
                binding.tvDeskripsiDetail.text = it.description
                if (!detailViewModel.isWebViewInitialized) {
                    loadYouTubeVideo(it.ytLink)
                }
            }
        }

        // Initialize the WebView
        setupWebView()

        // Get the module data passed from the previous screen
        val module = intent.getParcelableExtra<Module>("module")
        if (module == null) {
            detailViewModel.setModule(null)
        } else {
            detailViewModel.setModule(module)
        }
    }

    private fun setupWebView() {
        binding.wvDetail.settings.javaScriptEnabled = true
        binding.wvDetail.settings.domStorageEnabled = true

        binding.wvDetail.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // Blokir semua navigasi
                return true
            }
        }

        binding.wvDetail.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (fullscreenView != null) {
                    callback?.onCustomViewHidden()
                    return
                }

                fullscreenView = view
                fullscreenCallback = callback

                // Masuk ke mode fullscreen
                supportActionBar?.hide()
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
                (window.decorView as? ViewGroup)?.addView(view)
            }

            override fun onHideCustomView() {
                if (fullscreenView == null) return

                // Keluar dari mode fullscreen
                supportActionBar?.show()
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                (window.decorView as? ViewGroup)?.removeView(fullscreenView)
                fullscreenView = null
                fullscreenCallback?.onCustomViewHidden()
                fullscreenCallback = null
            }
        }
    }

    private fun loadYouTubeVideo(url: String) {
        val videoHtml = """
            <html>
            <head>
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        background-color: black;
                        overflow: hidden;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                    }
                    iframe {
                        position: relative;
                        width: 100%;
                        height: 100%;
                    }
                    .overlay {
                        position: absolute;
                        top: 0;
                        left: 0;
                        width: 100%;
                        height: 100%;
                        z-index: 10;
                        background: transparent;
                    }
                    .overlay.top {
                        height: 20%; /* Blokir bagian atas (logo YouTube) */
                    }
                    .overlay.bottom {
                        bottom: 0;
                        height: 20%; /* Blokir bagian bawah */
                    }
                </style>
            </head>
            <body>
                <iframe 
                    src="https://www.youtube.com/embed/${extractYouTubeId(url)}?autoplay=1&rel=0&modestbranding=1&playsinline=1"
                    frameborder="0"
                    allow="autoplay; fullscreen"
                    allowfullscreen>
                </iframe>
                <div class="overlay top"></div>
                <div class="overlay bottom"></div>
            </body>
            </html>
        """.trimIndent()

        binding.wvDetail.loadDataWithBaseURL(null, videoHtml, "text/html", "UTF-8", null)
        detailViewModel.isWebViewInitialized = true
    }

    override fun onPause() {
        super.onPause()
        // Simpan status WebView
        detailViewModel.webViewState = Bundle().apply {
            binding.wvDetail.saveState(this)
        }
    }

    override fun onResume() {
        super.onResume()
        // Pulihkan status WebView
        detailViewModel.webViewState?.let {
            binding.wvDetail.restoreState(it)
        }
    }

    private fun extractYouTubeId(url: String): String {
        val regex = "((?<=(v=))[^&#]*)|((?<=(be/))[^&#]*)".toRegex()
        val match = regex.find(url)
        return match?.value ?: ""
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Tangani orientasi secara manual jika diperlukan
    }
}
