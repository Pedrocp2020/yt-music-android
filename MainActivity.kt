package com.pedro.ytmusic

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {

    private lateinit var web: WebView
    private var script: String = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        script = assets.open("inject.js").bufferedReader().use { it.readText() }

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        // El servicio en primer plano mantiene viva la app con la pantalla apagada
        startForegroundService(Intent(this, AudioService::class.java))

        web = WebView(this)
        setContentView(web)

        with(web.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
        }
        CookieManager.getInstance().setAcceptCookie(true)

        web.webChromeClient = WebChromeClient()
        web.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                view.evaluateJavascript(script, null)
            }

            override fun onPageFinished(view: WebView, url: String?) {
                view.evaluateJavascript(script, null)
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val host = request.url.host ?: return false
                val interno = host.endsWith("youtube.com") ||
                        host.endsWith("google.com") ||
                        host.endsWith("gstatic.com") ||
                        host.endsWith("googlevideo.com")
                if (interno) return false
                startActivity(Intent(Intent.ACTION_VIEW, request.url))
                return true
            }
        }

        web.loadUrl("https://music.youtube.com")
    }

    // IMPORTANTE: no llamamos web.onPause() para que el audio siga sonando en segundo plano.

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else moveTaskToBack(true) // minimiza, sigue sonando
    }

    override fun onDestroy() {
        stopService(Intent(this, AudioService::class.java))
        web.destroy()
        super.onDestroy()
    }
}
