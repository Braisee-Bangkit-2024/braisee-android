package com.braille.braisee.ui.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import android.view.WindowInsets
import android.view.WindowInsetsController
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

        // Tombol kembali ke fragment sebelumnya
        binding.btnBack.setOnClickListener {
            finish() // Menutup DetailActivity
        }

        // Initialize ViewModel
        detailViewModel = ViewModelProvider(this)[DetailViewModel::class.java]

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
        val module = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("module", Module::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("module") as? Module
        }

        if (module == null) {
            detailViewModel.setModule(null)
        } else {
            detailViewModel.setModule(module)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.wvDetail.settings.javaScriptEnabled = true
        binding.wvDetail.settings.domStorageEnabled = true

        binding.wvDetail.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java", ReplaceWith("true"))
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

                enterFullscreen(view)
            }

            override fun onHideCustomView() {
                if (fullscreenView == null) return

                exitFullscreen()
            }
        }
    }

    private fun enterFullscreen(view: View?) {
        supportActionBar?.hide()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // API 30 ke atas
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // API di bawah 30
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
        (window.decorView as? ViewGroup)?.addView(view)
    }

    private fun exitFullscreen() {
        supportActionBar?.show()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // API 30 ke atas
            window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            // API di bawah 30
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
        (window.decorView as? ViewGroup)?.removeView(fullscreenView)
        fullscreenView = null
        fullscreenCallback?.onCustomViewHidden()
        fullscreenCallback = null
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
                </style>
            </head>
            <body>
                <iframe 
                    src="https://www.youtube.com/embed/${extractYouTubeId(url)}?autoplay=1&rel=0&modestbranding=1&playsinline=1"
                    frameborder="0"
                    allow="autoplay; fullscreen"
                    allowfullscreen>
                </iframe>
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
}
