package com.brahim.carhome.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Garde un seul WebView en vie pour retrouver la page quand on revient au navigateur. */
@SuppressLint("SetJavaScriptEnabled")
class BrowserController(private val context: Context) {

    var currentUrl by mutableStateOf("")
        private set
    var title by mutableStateOf("")
        private set
    var progress by mutableIntStateOf(100)
        private set
    var canGoBack by mutableStateOf(false)
        private set
    var canGoForward by mutableStateOf(false)
        private set

    val webView: WebView = WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            mediaPlaybackRequiresUserGesture = false
            builtInZoomControls = true
            displayZoomControls = false
        }
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val scheme = request.url.scheme ?: return false
                if (scheme == "http" || scheme == "https") return false
                // Liens spéciaux (intent://, tel:, waze://…) : on les ouvre dans l'appli concernée
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, request.url).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
                return true
            }

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                url?.let { currentUrl = it }
                updateNav()
            }

            override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
                url?.let { currentUrl = it }
                updateNav()
            }

            override fun onPageFinished(view: WebView, url: String?) = updateNav()
        }
        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progress = newProgress
            }

            override fun onReceivedTitle(view: WebView, t: String?) {
                title = t.orEmpty()
            }
        }
    }

    private val defaultUserAgent: String = webView.settings.userAgentString
    private var desktopMode = false

    private fun updateNav() {
        canGoBack = webView.canGoBack()
        canGoForward = webView.canGoForward()
    }

    fun load(url: String) = webView.loadUrl(url)
    fun goBack() = webView.goBack()
    fun goForward() = webView.goForward()
    fun reload() = webView.reload()

    fun setDesktopMode(enabled: Boolean) {
        if (enabled == desktopMode) return
        desktopMode = enabled
        webView.settings.userAgentString = if (enabled) DESKTOP_UA else defaultUserAgent
        if (currentUrl.isNotEmpty()) webView.reload()
    }

    /** Coupe toutes les vidéos et sons de la page (appelé quand la voiture roule). */
    fun pause() {
        webView.evaluateJavascript(
            "document.querySelectorAll('video,audio').forEach(function(m){m.pause();});", null
        )
        webView.onPause()
    }

    fun resume() = webView.onResume()

    fun destroy() {
        (webView.parent as? ViewGroup)?.removeView(webView)
        webView.destroy()
    }

    companion object {
        private const val DESKTOP_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    }
}
