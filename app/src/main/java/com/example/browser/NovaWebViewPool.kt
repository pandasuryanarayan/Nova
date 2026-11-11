package com.example.browser

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.BlockedAdEntry
import com.example.data.BrowserTab
import com.example.data.DetectedVideoInfo
import com.example.data.DetectedVideoQuality
import com.example.data.NOVA_HOME_URL
import com.example.data.NOVA_VIDEO_LAB_URL
import java.util.concurrent.ConcurrentHashMap
import org.json.JSONObject

class NovaWebViewPool(
    private val context: Context,
    private val onTabStateUpdated: (
        tabId: String,
        url: String?,
        title: String?,
        isLoading: Boolean?,
        progress: Int?,
        canGoBack: Boolean?,
        canGoForward: Boolean?,
        isSecureSsl: Boolean?,
        sslSummary: String?
    ) -> Unit,
    private val onHistoryVisited: (tabId: String, title: String, url: String) -> Unit,
    private val onAdBlocked: (BlockedAdEntry) -> Unit,
    private val onVideoLongPressed: (DetectedVideoInfo) -> Unit,
    private val onVideoSizeProbedInWebView: (url: String, contentLength: Long, contentType: String) -> Unit,
    private val onWebViewDownloadChunk: (downloadId: Long, base64Chunk: String, loadedBytes: Long, totalBytes: Long) -> Unit,
    private val onWebViewDownloadFinished: (downloadId: Long, totalLoadedBytes: Long) -> Unit,
    private val onWebViewDownloadError: (downloadId: Long, errorMessage: String) -> Unit,
    private val onDirectDownloadRequested: (url: String, userAgent: String, contentDisposition: String?, mimeType: String?, contentLength: Long) -> Unit,
    private val onEnterFullscreenVideo: (view: View, callback: WebChromeClient.CustomViewCallback) -> Unit,
    private val onExitFullscreenVideo: () -> Unit,
    private val onFindResultUpdated: (activeMatchOrdinal: Int, numberOfMatches: Int) -> Unit,
    private val onDevToolsElementSelectedJson: (tabId: String, json: String) -> Unit,
    private val onDevToolsConsoleLogJson: (tabId: String, json: String) -> Unit,
    private val onDevToolsNetworkEntryJson: (tabId: String, json: String) -> Unit,
    private val onPageFinishedForDevTools: (tabId: String) -> Unit,
    private val isAdBlockEnabledProvider: () -> Boolean,
    private val isDarkModeEnabledProvider: () -> Boolean
) {
    private val webViews = mutableMapOf<String, WebView>()
    private val tabStates = ConcurrentHashMap<String, BrowserTab>()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val defaultSystemMobileUserAgent: String by lazy {
        try {
            WebSettings.getDefaultUserAgent(context).takeIf { it.isNotBlank() } ?: MOBILE_USER_AGENT
        } catch (_: Exception) {
            MOBILE_USER_AGENT
        }
    }

    private val videoDetectorJs: String by lazy {
        try {
            context.assets.open("video_detector.js").bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            ""
        }
    }

    private val devToolsInspectorJs: String by lazy {
        try {
            context.assets.open("devtools_inspector.js").bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            ""
        }
    }

    companion object {
        const val MOBILE_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
        const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
        private const val ASSET_VIDEO_LAB_URL = "file:///android_asset/video_test_lab.html"

        private val DESKTOP_CLIENT_HINT_HEADERS = mapOf(
            "Sec-CH-UA" to "\"Chromium\";v=\"131\", \"Google Chrome\";v=\"131\", \"Not_A Brand\";v=\"24\"",
            "Sec-CH-UA-Mobile" to "?0",
            "Sec-CH-UA-Platform" to "\"Windows\"",
            "Upgrade-Insecure-Requests" to "1"
        )

        private val MOBILE_CLIENT_HINT_HEADERS = mapOf(
            "Sec-CH-UA-Mobile" to "?1",
            "Sec-CH-UA-Platform" to "\"Android\""
        )

        private const val DESKTOP_MODE_INJECTION_JS = """
            (function() {
              try {
                // 1. Spoof Desktop Navigator & Screen metrics so JS responsive checks render Desktop UI
                Object.defineProperty(navigator, 'platform', { get: function() { return 'Win32'; }, configurable: true });
                Object.defineProperty(navigator, 'maxTouchPoints', { get: function() { return 0; }, configurable: true });
                if (navigator.userAgentData) {
                  Object.defineProperty(navigator, 'userAgentData', {
                    get: function() {
                      return {
                        brands: [{ brand: 'Chromium', version: '131' }, { brand: 'Google Chrome', version: '131' }],
                        mobile: false,
                        platform: 'Windows',
                        getHighEntropyValues: function() {
                          return Promise.resolve({ architecture: 'x86', bitness: '64', mobile: false, model: '', platform: 'Windows', platformVersion: '15.0.0' });
                        }
                      };
                    },
                    configurable: true
                  });
                }
                if (window.screen) {
                  Object.defineProperty(window.screen, 'width', { get: function() { return 1920; }, configurable: true });
                  Object.defineProperty(window.screen, 'height', { get: function() { return 1080; }, configurable: true });
                  Object.defineProperty(window.screen, 'availWidth', { get: function() { return 1920; }, configurable: true });
                  Object.defineProperty(window.screen, 'availHeight', { get: function() { return 1040; }, configurable: true });
                }

                // 2. Force 1280px Desktop Viewport so CSS @media (min-width: 1024px) rules trigger
                var head = document.head || document.getElementsByTagName('head')[0];
                if (head) {
                  var metas = head.querySelectorAll('meta[name="viewport"]');
                  var targetContent = 'width=1280, initial-scale=0.32, minimum-scale=0.1, maximum-scale=5.0, user-scalable=yes';
                  if (metas.length > 0) {
                    for (var i = 0; i < metas.length; i++) {
                      if (metas[i].getAttribute('content') !== targetContent) {
                        metas[i].setAttribute('content', targetContent);
                      }
                    }
                  } else {
                    var meta = document.createElement('meta');
                    meta.name = 'viewport';
                    meta.content = targetContent;
                    head.appendChild(meta);
                  }
                }
              } catch (e) {}
            })();
        """

        private const val MOBILE_MODE_RESTORE_JS = """
            (function() {
              try {
                var head = document.head || document.getElementsByTagName('head')[0];
                if (head) {
                  var metas = head.querySelectorAll('meta[name="viewport"]');
                  for (var i = 0; i < metas.length; i++) {
                    var c = metas[i].getAttribute('content') || '';
                    if (c.indexOf('width=1280') !== -1) {
                      metas[i].setAttribute('content', 'width=device-width, initial-scale=1.0, user-scalable=yes');
                    }
                  }
                }
              } catch (e) {}
            })();
        """
    }

    fun getEffectiveUserAgent(tab: BrowserTab): String {
        tabStates[tab.id] = tab
        val wv = webViews[tab.id]
        if (wv != null) {
            return try {
                wv.settings.userAgentString.takeIf { it.isNotBlank() }
                    ?: if (tab.isDesktopMode) DESKTOP_USER_AGENT else defaultSystemMobileUserAgent
            } catch (_: Exception) {
                if (tab.isDesktopMode) DESKTOP_USER_AGENT else defaultSystemMobileUserAgent
            }
        }
        return if (tab.isDesktopMode) DESKTOP_USER_AGENT else defaultSystemMobileUserAgent
    }

    fun getOrCreateWebView(tab: BrowserTab): WebView {
        tabStates[tab.id] = tab
        val existing = webViews[tab.id]
        if (existing != null) {
            applyTabSettings(existing, tab)
            return existing
        }
        val created = createConfiguredWebView(tab)
        webViews[tab.id] = created
        if (!tab.isHomePage) {
            loadUrlInWebView(created, tab, tab.url)
        }
        return created
    }

    fun getWebView(tabId: String): WebView? = webViews[tabId]

    fun loadUrl(tab: BrowserTab, targetUrl: String) {
        tabStates[tab.id] = tab
        val webView = getOrCreateWebView(tab)
        loadUrlInWebView(webView, tab, targetUrl)
    }

    /**
     * Toggles Desktop/Mobile mode on an existing WebView, converts mobile/desktop URLs (`m.` <-> `www.`),
     * updates User-Agent + viewport settings, and reloads with Desktop/Mobile Client-Hint headers.
     */
    fun reloadForDesktopModeChange(tab: BrowserTab) {
        tabStates[tab.id] = tab
        val wv = webViews[tab.id] ?: return
        applyTabSettings(wv, tab)
        if (tab.isHomePage) return
        val currentUrl = mapDisplayUrl(wv.url ?: tab.url)
        val convertedUrl = if (tab.isDesktopMode) {
            BrowserUrlUtils.toDesktopUrl(currentUrl)
        } else {
            BrowserUrlUtils.toMobileUrl(currentUrl)
        }
        loadUrlInWebView(wv, tab, convertedUrl)
    }

    private fun loadUrlInWebView(webView: WebView, tab: BrowserTab, targetUrl: String) {
        tabStates[tab.id] = tab
        applyTabSettings(webView, tab)
        when (targetUrl) {
            NOVA_HOME_URL -> {
                webView.onPause()
            }
            NOVA_VIDEO_LAB_URL -> {
                webView.onResume()
                webView.loadUrl(ASSET_VIDEO_LAB_URL)
            }
            else -> {
                webView.onResume()
                val modeAdjustedUrl = if (tab.isDesktopMode) {
                    BrowserUrlUtils.toDesktopUrl(targetUrl)
                } else {
                    targetUrl
                }
                val headers = if (tab.isDesktopMode) {
                    DESKTOP_CLIENT_HINT_HEADERS
                } else {
                    MOBILE_CLIENT_HINT_HEADERS
                }
                webView.loadUrl(modeAdjustedUrl, headers)
            }
        }
    }

    fun applyTabSettings(webView: WebView, tab: BrowserTab) {
        tabStates[tab.id] = tab
        val targetUa = if (tab.isDesktopMode) DESKTOP_USER_AGENT else defaultSystemMobileUserAgent
        if (webView.settings.userAgentString != targetUa) {
            webView.settings.userAgentString = targetUa
        }
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        val desiredTextZoom = if (tab.isDesktopMode && tab.textZoom == 100) 95 else tab.textZoom
        if (webView.settings.textZoom != desiredTextZoom) {
            webView.settings.textZoom = desiredTextZoom
        }
    }

    fun removeAndDestroyTab(tabId: String) {
        tabStates.remove(tabId)
        val webView = webViews.remove(tabId) ?: return
        try {
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.clearHistory()
            webView.removeAllViews()
            webView.destroy()
        } catch (_: Exception) {
        }
    }

    fun clearAllBrowsingData(clearCache: Boolean, clearCookies: Boolean) {
        webViews.values.forEach { wv ->
            if (clearCache) {
                wv.clearCache(true)
                wv.clearFormData()
            }
            wv.clearHistory()
        }
        if (clearCookies) {
            try {
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
                WebStorage.getInstance().deleteAllData()
            } catch (_: Exception) {
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun createConfiguredWebView(initialTab: BrowserTab): WebView {
        val tabId = initialTab.id
        tabStates[tabId] = initialTab

        val webView = WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isFocusable = true
            isFocusableInTouchMode = true
            isLongClickable = true
            isHapticFeedbackEnabled = true
        }

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = !initialTab.isIncognito
        settings.databaseEnabled = !initialTab.isIncognito
        settings.mediaPlaybackRequiresUserGesture = false
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.loadsImagesAutomatically = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.setSupportZoom(true)
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        settings.cacheMode = if (initialTab.isIncognito) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
        settings.userAgentString = if (initialTab.isDesktopMode) DESKTOP_USER_AGENT else defaultSystemMobileUserAgent
        settings.textZoom = initialTab.textZoom

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(!initialTab.isIncognito)
        cookieManager.setAcceptThirdPartyCookies(webView, !initialTab.isIncognito)

        // Attach JavaScript Bridge for Long-Press Video Detection + Size Prober + Stream Downloader + DOM Ad Shield
        webView.addJavascriptInterface(
            NovaJsBridge(
                onVideoPayloadJson = { jsonStr ->
                    mainHandler.post {
                        parseVideoPayload(jsonStr)?.let { info ->
                            onVideoLongPressed(info)
                        }
                    }
                },
                onVideoSizeProbedJson = { jsonStr ->
                    mainHandler.post {
                        try {
                            val obj = JSONObject(jsonStr)
                            val url = obj.optString("url", "")
                            val len = obj.optLong("contentLength", -1L)
                            val cType = obj.optString("contentType", "video/mp4")
                            if (url.isNotBlank() && len > 0L) {
                                onVideoSizeProbedInWebView(url, len, cType)
                            }
                        } catch (_: Exception) {
                        }
                    }
                },
                onChunk = { downloadId, b64, loaded, total ->
                    onWebViewDownloadChunk(downloadId, b64, loaded, total)
                },
                onFinished = { downloadId, totalLoaded ->
                    mainHandler.post {
                        onWebViewDownloadFinished(downloadId, totalLoaded)
                    }
                },
                onError = { downloadId, err ->
                    mainHandler.post {
                        onWebViewDownloadError(downloadId, err)
                    }
                },
                onDomAdBlockedJson = { jsonStr ->
                    if (!isAdBlockEnabledProvider()) return@NovaJsBridge
                    mainHandler.post {
                        try {
                            val obj = JSONObject(jsonStr)
                            val entry = BlockedAdEntry(
                                tabId = tabId,
                                adUrl = obj.optString("adUrl", "dom-ad"),
                                adDomain = obj.optString("adDomain", "dom-element"),
                                pageDomain = obj.optString("pageDomain", "page"),
                                category = obj.optString("category", "DOM Ad Container"),
                                filterRule = obj.optString("filterRule", "##.adsbygoogle")
                            )
                            onAdBlocked(entry)
                        } catch (_: Exception) {
                        }
                    }
                }
            ),
            "NovaVideoBridge"
        )

        // Attach JavaScript Bridge for Desktop-Grade DevTools Inspector
        webView.addJavascriptInterface(
            NovaDevToolsJsBridge(
                onElementSelected = { jsonStr ->
                    mainHandler.post {
                        onDevToolsElementSelectedJson(tabId, jsonStr)
                    }
                },
                onConsoleLog = { jsonStr ->
                    mainHandler.post {
                        onDevToolsConsoleLogJson(tabId, jsonStr)
                    }
                },
                onNetworkEntry = { jsonStr ->
                    mainHandler.post {
                        onDevToolsNetworkEntryJson(tabId, jsonStr)
                    }
                }
            ),
            "NovaDevToolsBridge"
        )

        // Native Long-Press Listener using WebView.HitTestResult + JS Inspector fallback
        webView.setOnLongClickListener { view ->
            val wv = view as? WebView ?: return@setOnLongClickListener false
            val hitResult = wv.hitTestResult
            val hitType = hitResult.type
            val hitExtra = hitResult.extra ?: ""
            val liveTab = tabStates[tabId] ?: initialTab

            val isMediaHitType = hitType == 10
            val isAnchorOrMediaHit = hitType == WebView.HitTestResult.SRC_ANCHOR_TYPE ||
                hitType == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE ||
                hitType == WebView.HitTestResult.IMAGE_TYPE ||
                isMediaHitType

            if (hitExtra.isNotBlank() && (isMediaHitType ||
                    BrowserUrlUtils.isDirectVideoCandidate(hitExtra) ||
                    BrowserUrlUtils.isProtectedOrStreamingUrl(hitExtra))
            ) {
                val pageUrl = mapDisplayUrl(wv.url ?: liveTab.url)
                val pageTitle = wv.title?.takeIf { it.isNotBlank() } ?: BrowserUrlUtils.extractDomain(pageUrl)
                val isStreaming = BrowserUrlUtils.isProtectedOrStreamingUrl(hitExtra)
                val info = DetectedVideoInfo(
                    pageTitle = pageTitle,
                    pageUrl = pageUrl,
                    domain = BrowserUrlUtils.extractDomain(pageUrl),
                    primaryUrl = hitExtra,
                    posterUrl = "",
                    qualities = listOf(
                        DetectedVideoQuality(
                            url = hitExtra,
                            label = if (isStreaming) "Protected Stream" else "Direct MP4",
                            mimeType = if (isStreaming) "application/x-mpegURL" else "video/mp4",
                            isDirectMp4 = !isStreaming,
                            isStreaming = isStreaming
                        )
                    ),
                    selectedQualityIndex = 0
                )
                onVideoLongPressed(info)
                return@setOnLongClickListener true
            }

            wv.evaluateJavascript(
                "(function(){ return window.__novaInspectVideoAtLastTouch ? window.__novaInspectVideoAtLastTouch() : ''; })();",
                null
            )

            isAnchorOrMediaHit || hitType == WebView.HitTestResult.UNKNOWN_TYPE
        }

        webView.setFindListener { activeMatchOrdinal, numberOfMatches, _ ->
            mainHandler.post {
                onFindResultUpdated(activeMatchOrdinal, numberOfMatches)
            }
        }

        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            mainHandler.post {
                onDirectDownloadRequested(url, userAgent, contentDisposition, mimetype, contentLength)
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val reqUrl = request?.url?.toString() ?: return null
                val liveTab = tabStates[tabId] ?: initialTab

                if (isAdBlockEnabledProvider()) {
                    val matchedAd = AdBlocker.matchAdRequest(
                        tabId = tabId,
                        url = reqUrl,
                        pageUrl = liveTab.url
                    )
                    if (matchedAd != null) {
                        mainHandler.post { onAdBlocked(matchedAd) }
                        return AdBlocker.createEmptyResource()
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val target = request?.url?.toString() ?: return false
                if (target.equals(NOVA_VIDEO_LAB_URL, ignoreCase = true)) {
                    view?.loadUrl(ASSET_VIDEO_LAB_URL)
                    return true
                }
                if (target.startsWith("http://") || target.startsWith("https://")) {
                    val liveTab = tabStates[tabId] ?: initialTab
                    if (liveTab.isDesktopMode && request.isForMainFrame) {
                        val desktopUrl = BrowserUrlUtils.toDesktopUrl(target)
                        if (desktopUrl != target && view != null) {
                            loadUrlInWebView(view, liveTab, desktopUrl)
                            return true
                        }
                    }
                    return false
                }
                if (target.startsWith("file://")) {
                    return false
                }
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                val mappedUrl = mapDisplayUrl(url ?: "")
                val isSsl = mappedUrl.startsWith("https://") || mappedUrl.startsWith("nova://")
                val sslDesc = when {
                    mappedUrl.startsWith("nova://") -> "Nova Internal Verified Page"
                    isSsl -> "Connection is encrypted (HTTPS / TLS)"
                    else -> "Not Secure (Unencrypted HTTP connection)"
                }
                onTabStateUpdated(
                    tabId,
                    mappedUrl,
                    view?.title,
                    true,
                    12,
                    view?.canGoBack() ?: false,
                    view?.canGoForward() ?: false,
                    isSsl,
                    sslDesc
                )
                injectScripts(view, tabId)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                try {
                    CookieManager.getInstance().flush()
                } catch (_: Exception) {
                }
                val liveTab = tabStates[tabId] ?: initialTab
                val mappedUrl = mapDisplayUrl(url ?: "")
                val cert = view?.certificate
                val isSsl = (mappedUrl.startsWith("https://") && cert != null) ||
                    mappedUrl.startsWith("https://") ||
                    mappedUrl.startsWith("nova://")
                val sslDesc = when {
                    mappedUrl.startsWith("nova://") -> "Nova Internal Verified Page"
                    cert != null -> "Verified by ${cert.issuedBy?.oName?.ifBlank { cert.issuedBy?.cName } ?: "Trusted CA"}"
                    isSsl -> "Connection is encrypted (HTTPS)"
                    else -> "Not Secure (HTTP)"
                }
                val title = view?.title?.takeIf { it.isNotBlank() } ?: BrowserUrlUtils.extractDomain(mappedUrl)
                onTabStateUpdated(
                    tabId,
                    mappedUrl,
                    title,
                    false,
                    100,
                    view?.canGoBack() ?: false,
                    view?.canGoForward() ?: false,
                    isSsl,
                    sslDesc
                )
                injectScripts(view, tabId)
                mainHandler.post {
                    onPageFinishedForDevTools(tabId)
                }

                if (!liveTab.isIncognito && !mappedUrl.startsWith("nova://") && mappedUrl.isNotBlank()) {
                    onHistoryVisited(tabId, title, mappedUrl)
                }
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                if (url != null && (url.contains(".mp4") || url.contains("video") || url.contains("embed"))) {
                    injectScripts(view, tabId)
                }
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                onTabStateUpdated(
                    tabId,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    "SSL Certificate Warning (${error?.primaryError ?: "Untrusted"})"
                )
                handler?.cancel()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                onTabStateUpdated(
                    tabId,
                    null,
                    view?.title?.takeIf { it.isNotBlank() },
                    newProgress < 100,
                    newProgress,
                    view?.canGoBack() ?: false,
                    view?.canGoForward() ?: false,
                    null,
                    null
                )
                if (newProgress >= 30) {
                    injectScripts(view, tabId)
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                if (!title.isNullOrBlank()) {
                    onTabStateUpdated(
                        tabId,
                        null,
                        title,
                        null,
                        null,
                        view?.canGoBack() ?: false,
                        view?.canGoForward() ?: false,
                        null,
                        null
                    )
                }
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                if (consoleMessage != null) {
                    val level = when (consoleMessage.messageLevel()) {
                        ConsoleMessage.MessageLevel.ERROR -> "ERROR"
                        ConsoleMessage.MessageLevel.WARNING -> "WARN"
                        ConsoleMessage.MessageLevel.LOG -> "LOG"
                        ConsoleMessage.MessageLevel.TIP,
                        ConsoleMessage.MessageLevel.DEBUG -> "INFO"
                        else -> "LOG"
                    }
                    val srcId = consoleMessage.sourceId()?.substringAfterLast('/')?.takeIf { it.isNotBlank() } ?: "page"
                    val payload = JSONObject().apply {
                        put("level", level)
                        put("message", consoleMessage.message() ?: "")
                        put("source", "$srcId:${consoleMessage.lineNumber()}")
                        put("timestamp", System.currentTimeMillis())
                    }.toString()
                    mainHandler.post {
                        onDevToolsConsoleLogJson(tabId, payload)
                    }
                }
                return super.onConsoleMessage(consoleMessage)
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (view != null && callback != null) {
                    onEnterFullscreenVideo(view, callback)
                } else {
                    super.onShowCustomView(view, callback)
                }
            }

            override fun onHideCustomView() {
                onExitFullscreenVideo()
                super.onHideCustomView()
            }
        }

        return webView
    }

    private fun mapDisplayUrl(rawUrl: String): String {
        return if (rawUrl == ASSET_VIDEO_LAB_URL) {
            NOVA_VIDEO_LAB_URL
        } else {
            rawUrl
        }
    }

    private fun injectScripts(webView: WebView?, tabId: String) {
        if (webView == null) return
        val liveTab = tabStates[tabId]

        // 1. Desktop Mode Viewport & Navigator Override vs Mobile Mode Restore
        if (liveTab?.isDesktopMode == true) {
            webView.evaluateJavascript(DESKTOP_MODE_INJECTION_JS, null)
        } else {
            webView.evaluateJavascript(MOBILE_MODE_RESTORE_JS, null)
        }

        // 2. Cosmetic DOM Ad Shield & Reporter
        if (isAdBlockEnabledProvider()) {
            webView.evaluateJavascript(AdBlocker.COSMETIC_AD_SHIELD_JS, null)
        }

        // 4. Long-Press Video Detector & DevTools Inspector
        if (videoDetectorJs.isNotBlank()) {
            webView.evaluateJavascript(videoDetectorJs, null)
        }
        if (devToolsInspectorJs.isNotBlank()) {
            webView.evaluateJavascript(devToolsInspectorJs, null)
        }

        // 5. Dark Mode Hint
        if (isDarkModeEnabledProvider()) {
            val currentUrl = webView.url ?: ""
            if (!currentUrl.contains("video_test_lab.html")) {
                val darkHintJs = """
                    (function() {
                      if (!document.querySelector('meta[name="color-scheme"]')) {
                        var m = document.createElement('meta');
                        m.name = 'color-scheme';
                        m.content = 'dark light';
                        if (document.head) document.head.appendChild(m);
                      }
                    })();
                """.trimIndent()
                webView.evaluateJavascript(darkHintJs, null)
            }
        }
    }

    private fun parseVideoPayload(jsonStr: String): DetectedVideoInfo? {
        return try {
            val obj = JSONObject(jsonStr)
            val pageTitle = obj.optString("pageTitle", "Video")
            val rawPageUrl = obj.optString("pageUrl", "")
            val pageUrl = mapDisplayUrl(rawPageUrl)
            val primaryUrl = obj.optString("primaryUrl", "")
            if (primaryUrl.isBlank()) return null

            val posterUrl = obj.optString("posterUrl", "")
            val isIframe = obj.optBoolean("isIframe", false)
            val dimensions = obj.optString("dimensions", "")
            val durationSeconds = obj.optInt("durationSeconds", 0)

            val qualitiesList = mutableListOf<DetectedVideoQuality>()
            val qualitiesArr = obj.optJSONArray("qualities")
            if (qualitiesArr != null) {
                for (i in 0 until qualitiesArr.length()) {
                    val qObj = qualitiesArr.optJSONObject(i) ?: continue
                    val qUrl = qObj.optString("url", "")
                    if (qUrl.isBlank()) continue
                    val qLabel = qObj.optString("label", "Quality #${i + 1}")
                    val qMime = qObj.optString("mimeType", "video/mp4")
                    val qStreaming = qObj.optBoolean("isStreaming", false) ||
                        BrowserUrlUtils.isProtectedOrStreamingUrl(qUrl, qMime)
                    val qDirect = !qStreaming
                    val qEstimatedBytes = qObj.optLong("estimatedBytes", -1L)

                    qualitiesList.add(
                        DetectedVideoQuality(
                            url = qUrl,
                            label = qLabel,
                            mimeType = qMime.ifBlank { "video/mp4" },
                            isDirectMp4 = qDirect,
                            isStreaming = qStreaming,
                            estimatedBytes = qEstimatedBytes,
                            isProbing = qEstimatedBytes <= 0L
                        )
                    )
                }
            }

            if (qualitiesList.isEmpty()) {
                val isStream = BrowserUrlUtils.isProtectedOrStreamingUrl(primaryUrl)
                qualitiesList.add(
                    DetectedVideoQuality(
                        url = primaryUrl,
                        label = if (isStream) "Protected Stream" else "Direct MP4",
                        mimeType = if (isStream) "application/x-mpegURL" else "video/mp4",
                        isDirectMp4 = !isStream,
                        isStreaming = isStream
                    )
                )
            }

            DetectedVideoInfo(
                pageTitle = pageTitle,
                pageUrl = pageUrl,
                domain = BrowserUrlUtils.extractDomain(pageUrl.ifBlank { primaryUrl }),
                primaryUrl = qualitiesList.first().url,
                posterUrl = posterUrl,
                qualities = qualitiesList,
                selectedQualityIndex = 0,
                isIframe = isIframe,
                dimensions = dimensions,
                durationSeconds = durationSeconds
            )
        } catch (_: Exception) {
            null
        }
    }

    private class NovaJsBridge(
        private val onVideoPayloadJson: (String) -> Unit,
        private val onVideoSizeProbedJson: (String) -> Unit,
        private val onChunk: (Long, String, Long, Long) -> Unit,
        private val onFinished: (Long, Long) -> Unit,
        private val onError: (Long, String) -> Unit,
        private val onDomAdBlockedJson: (String) -> Unit
    ) {
        @JavascriptInterface
        fun onVideoLongPressed(payloadJson: String) {
            onVideoPayloadJson(payloadJson)
        }

        @JavascriptInterface
        fun onVideoSizeProbed(payloadJson: String) {
            onVideoSizeProbedJson(payloadJson)
        }

        @JavascriptInterface
        fun onWebViewDownloadChunk(downloadId: Long, base64Chunk: String, loadedBytes: Long, totalBytes: Long) {
            onChunk(downloadId, base64Chunk, loadedBytes, totalBytes)
        }

        @JavascriptInterface
        fun onWebViewDownloadFinished(downloadId: Long, totalLoadedBytes: Long) {
            onFinished(downloadId, totalLoadedBytes)
        }

        @JavascriptInterface
        fun onWebViewDownloadError(downloadId: Long, errorMessage: String) {
            onError(downloadId, errorMessage)
        }

        @JavascriptInterface
        fun onDomAdBlocked(payloadJson: String) {
            onDomAdBlockedJson(payloadJson)
        }
    }

    private class NovaDevToolsJsBridge(
        private val onElementSelected: (String) -> Unit,
        private val onConsoleLog: (String) -> Unit,
        private val onNetworkEntry: (String) -> Unit
    ) {
        @JavascriptInterface
        fun onElementSelected(payloadJson: String) {
            onElementSelected.invoke(payloadJson)
        }

        @JavascriptInterface
        fun onConsoleLog(payloadJson: String) {
            onConsoleLog.invoke(payloadJson)
        }

        @JavascriptInterface
        fun onNetworkEntry(payloadJson: String) {
            onNetworkEntry.invoke(payloadJson)
        }
    }
}
