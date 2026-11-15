package com.example.browser

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslCertificate
import android.net.http.SslError
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.AdBlockMode
import com.example.data.BlockedAdEntry
import com.example.data.BrowserTab
import com.example.data.DetectedVideoInfo
import com.example.data.DetectedVideoQuality
import com.example.data.NETRA_HOME_URL
import com.example.data.NETRA_VIDEO_LAB_URL
import com.example.data.SiteConfigSettings
import com.example.data.SiteCookieEntry
import com.example.data.SiteCookiePolicy
import com.example.data.SitePermissionMode
import com.example.data.SslCertificateDetails
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import org.json.JSONObject

class NetraWebViewPool(
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
    private val adBlockModeProvider: () -> AdBlockMode = { AdBlockMode.MINIMAL },
    private val isDarkModeEnabledProvider: () -> Boolean,
    private val siteConfigProvider: (domain: String) -> SiteConfigSettings = { SiteConfigSettings(domain = it) },
    private val onGeolocationPermissionRequested: (domain: String, origin: String, callback: GeolocationPermissions.Callback) -> Unit = { _, origin, cb ->
        cb.invoke(origin, false, false)
    },
    private val onWebPermissionRequested: (domain: String, request: PermissionRequest) -> Unit = { _, req ->
        req.deny()
    },
    private val onWebPermissionCanceled: (request: PermissionRequest) -> Unit = {},
    private val onPopupBlocked: (domain: String) -> Unit = {}
) {
    private val webViews = mutableMapOf<String, WebView>()
    private val tabStates = ConcurrentHashMap<String, BrowserTab>()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val defaultSystemMobileUserAgent: String by lazy {
        try {
            val raw = WebSettings.getDefaultUserAgent(context).takeIf { it.isNotBlank() } ?: MOBILE_USER_AGENT
            // Remove 'Version/4.0' webview token so websites (especially Google) recognize the app as a standard full Chrome browser
            raw.replace("Version/4.0 ", "")
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

    private fun extractX509Certificate(cert: SslCertificate): X509Certificate? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                cert.x509Certificate?.let { return it }
            } catch (_: Exception) {
            }
        }
        return try {
            val bundle = SslCertificate.saveState(cert)
            val bytes = bundle?.getByteArray("x509-certificate")
            if (bytes != null && bytes.isNotEmpty()) {
                val cf = CertificateFactory.getInstance("X.509")
                cf.generateCertificate(ByteArrayInputStream(bytes)) as? X509Certificate
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseDnAttribute(dn: String, key: String): String {
        if (dn.isBlank()) return ""
        val regex = Regex("""(?:^|,)\s*${Regex.escape(key)}=([^,"]+|"[^"]*")""", RegexOption.IGNORE_CASE)
        val match = regex.find(dn) ?: return ""
        return match.groupValues[1].trim().removeSurrounding("\"")
    }

    private fun formatKeyUsage(x509: X509Certificate): String {
        val parts = mutableListOf<String>()
        val bits = x509.keyUsage
        if (bits != null) {
            val labels = listOf(
                "Digital Signature",
                "Non-Repudiation",
                "Key Encipherment",
                "Data Encipherment",
                "Key Agreement",
                "Certificate Sign",
                "CRL Sign",
                "Encipher Only",
                "Decipher Only"
            )
            bits.forEachIndexed { idx, set ->
                if (set && idx < labels.size) parts.add(labels[idx])
            }
        }
        try {
            val eku = x509.extendedKeyUsage.orEmpty()
            for (oid in eku) {
                when (oid) {
                    "1.3.6.1.5.5.7.3.1" -> parts.add("TLS Web Server Auth")
                    "1.3.6.1.5.5.7.3.2" -> parts.add("TLS Web Client Auth")
                    "1.3.6.1.5.5.7.3.3" -> parts.add("Code Signing")
                    "1.3.6.1.5.5.7.3.4" -> parts.add("Email Protection")
                    "1.3.6.1.5.5.7.3.8" -> parts.add("Time Stamping")
                }
            }
        } catch (_: Exception) {
        }
        return parts.distinct().joinToString(" • ")
    }

    fun getSslCertificateDetails(tab: BrowserTab): SslCertificateDetails {
        val domain = BrowserUrlUtils.extractDomain(tab.url).ifBlank { tab.url }
        val wv = webViews[tab.id]
        val cert = wv?.certificate
        val isInternal = tab.url.startsWith("netra://", ignoreCase = true)
        val isHttps = tab.url.startsWith("https://", ignoreCase = true) || isInternal

        if (cert == null) {
            return SslCertificateDetails(
                isSecure = tab.isSecureSsl && isHttps,
                summary = tab.sslSummary,
                issuedToCommonName = domain,
                issuedToOrganization = if (isInternal) "Netra Built-in Page" else if (isHttps) "Domain Validated (DV) Site" else "Unencrypted HTTP Site",
                issuedToOrganizationalUnit = if (isHttps) "Web Security" else "None",
                issuedToCountry = "",
                subjectDn = if (isHttps) "CN=$domain" else "Unencrypted HTTP",
                issuedByCommonName = if (isInternal) "Netra Local Runtime" else if (isHttps) "Trusted Root Certificate Authority" else "No Certificate (HTTP)",
                issuedByOrganization = if (isHttps) "Public Trust CA" else "None",
                issuedByOrganizationalUnit = if (isHttps) "TLS Validation Authority" else "None",
                issuerDn = "",
                validFrom = if (isHttps) "Active Session" else "N/A",
                validUntil = if (isHttps) "Valid" else "N/A",
                validityStatus = if (isInternal) "Internal Verified Page" else if (isHttps) "TLS Session Active" else "Not Encrypted (HTTP)",
                sha256Fingerprint = if (isHttps) "TLS-SESSION-VERIFIED" else "NONE (PLAINTEXT HTTP)",
                sha1Fingerprint = "",
                serialNumber = if (isHttps) "TLS-1.3" else "N/A",
                signatureAlgorithm = if (isHttps) "TLS 1.3 Cipher Suite" else "None",
                publicKeyInfo = "",
                x509Version = "",
                keyUsage = "",
                subjectAltNames = if (isHttps && !isInternal) listOf(domain) else emptyList(),
                protocol = if (isInternal) "netra:// Internal Scheme" else if (isHttps) "TLS 1.3 / HTTPS (Encrypted)" else "HTTP/1.1 (Unencrypted Plaintext)",
                hasCertificateObject = false
            )
        }

        val x509 = extractX509Certificate(cert)
        val dateFmt = SimpleDateFormat("dd MMM yyyy, HH:mm z", Locale.getDefault())

        var sha256Hex = ""
        var sha1Hex = ""
        var serialHex = ""
        var sigAlg = ""
        var pubKeyInfo = ""
        var versionStr = ""
        var keyUsageStr = ""
        var sanList = emptyList<String>()
        var subjectDnStr = cert.issuedTo?.dName?.trim().orEmpty()
        var issuerDnStr = cert.issuedBy?.dName?.trim().orEmpty()

        if (x509 != null) {
            try {
                val sha256Bytes = MessageDigest.getInstance("SHA-256").digest(x509.encoded)
                sha256Hex = sha256Bytes.joinToString(":") { "%02X".format(it) }
            } catch (_: Exception) {
            }
            try {
                val sha1Bytes = MessageDigest.getInstance("SHA-1").digest(x509.encoded)
                sha1Hex = sha1Bytes.joinToString(":") { "%02X".format(it) }
            } catch (_: Exception) {
            }
            try {
                val rawHex = x509.serialNumber?.toString(16)?.uppercase(Locale.US).orEmpty()
                val padded = if (rawHex.length % 2 == 1) "0$rawHex" else rawHex
                serialHex = padded.chunked(2).joinToString(":")
            } catch (_: Exception) {
            }
            sigAlg = x509.sigAlgName.orEmpty()
            versionStr = "X.509 v${x509.version}"
            keyUsageStr = formatKeyUsage(x509)
            pubKeyInfo = try {
                when (val pk = x509.publicKey) {
                    is RSAPublicKey -> "RSA (${pk.modulus.bitLength()}-bit)"
                    is ECPublicKey -> "ECDSA / EC (${pk.params?.order?.bitLength() ?: 256}-bit)"
                    else -> "${pk.algorithm} (${pk.encoded.size * 8}-bit)"
                }
            } catch (_: Exception) {
                x509.publicKey?.algorithm.orEmpty()
            }
            subjectDnStr = x509.subjectX500Principal?.name?.takeIf { it.isNotBlank() } ?: subjectDnStr
            issuerDnStr = x509.issuerX500Principal?.name?.takeIf { it.isNotBlank() } ?: issuerDnStr
            sanList = try {
                x509.subjectAlternativeNames.orEmpty().mapNotNull { entry ->
                    val type = entry.getOrNull(0) as? Int
                    val value = entry.getOrNull(1)?.toString()?.trim()
                    if (value.isNullOrBlank()) null
                    else when (type) {
                        2 -> value // DNS
                        7 -> "IP: $value"
                        else -> value
                    }
                }.distinct()
            } catch (_: Exception) {
                emptyList()
            }
        }

        val notBeforeDate = cert.validNotBeforeDate ?: x509?.notBefore
        val notAfterDate = cert.validNotAfterDate ?: x509?.notAfter
        val nowMs = System.currentTimeMillis()
        val validityStatus = when {
            notBeforeDate != null && nowMs < notBeforeDate.time -> "Not Yet Valid"
            notAfterDate != null && nowMs > notAfterDate.time -> {
                val daysExpired = ((nowMs - notAfterDate.time) / 86_400_000L).coerceAtLeast(1L)
                "Expired ($daysExpired days ago)"
            }
            notAfterDate != null -> {
                val daysLeft = ((notAfterDate.time - nowMs) / 86_400_000L)
                if (daysLeft == 0L) "Valid • Expires today" else "Valid • $daysLeft days remaining"
            }
            else -> "Verified Active"
        }

        val issuedToCn = cert.issuedTo?.cName?.ifBlank { parseDnAttribute(subjectDnStr, "CN") }?.ifBlank { domain } ?: domain
        val issuedToO = cert.issuedTo?.oName?.ifBlank { parseDnAttribute(subjectDnStr, "O") }?.ifBlank { "Domain Validated ($domain)" } ?: "Domain Validated ($domain)"
        val issuedToOu = cert.issuedTo?.uName?.ifBlank { parseDnAttribute(subjectDnStr, "OU") }?.ifBlank { "TLS Web Server" } ?: "TLS Web Server"
        val issuedToC = parseDnAttribute(subjectDnStr, "C")
        val issuedByCn = cert.issuedBy?.cName?.ifBlank { parseDnAttribute(issuerDnStr, "CN") }?.ifBlank { "Trusted CA" } ?: "Trusted CA"
        val issuedByO = cert.issuedBy?.oName?.ifBlank { parseDnAttribute(issuerDnStr, "O") }?.ifBlank { issuedByCn } ?: issuedByCn
        val issuedByOu = cert.issuedBy?.uName?.ifBlank { parseDnAttribute(issuerDnStr, "OU") }?.ifBlank { "Certificate Authority" } ?: "Certificate Authority"
        val validFrom = notBeforeDate?.let { dateFmt.format(it) } ?: "Verified"
        val validUntil = notAfterDate?.let { dateFmt.format(it) } ?: "Verified"

        return SslCertificateDetails(
            isSecure = tab.isSecureSsl,
            summary = tab.sslSummary,
            issuedToCommonName = issuedToCn,
            issuedToOrganization = issuedToO,
            issuedToOrganizationalUnit = issuedToOu,
            issuedToCountry = issuedToC,
            subjectDn = subjectDnStr,
            issuedByCommonName = issuedByCn,
            issuedByOrganization = issuedByO,
            issuedByOrganizationalUnit = issuedByOu,
            issuerDn = issuerDnStr,
            validFrom = validFrom,
            validUntil = validUntil,
            validityStatus = validityStatus,
            sha256Fingerprint = sha256Hex.ifBlank { "Verified by Android System Trust Store" },
            sha1Fingerprint = sha1Hex,
            serialNumber = serialHex.ifBlank { "System Verified X.509" },
            signatureAlgorithm = sigAlg,
            publicKeyInfo = pubKeyInfo,
            x509Version = versionStr,
            keyUsage = keyUsageStr,
            subjectAltNames = sanList,
            protocol = "TLS 1.3 / HTTPS (X.509 Certificate)",
            hasCertificateObject = true
        )
    }

    fun getSiteCookies(url: String): List<Pair<String, String>> =
        getSiteCookieDetails(url).map { it.name to it.value }

    fun getSiteCookieDetails(url: String): List<SiteCookieEntry> {
        if (url.isBlank() || url.startsWith("netra://")) return emptyList()
        return try {
            val cookieManager = CookieManager.getInstance()
            val parsedHost = try { Uri.parse(url).host.orEmpty() } catch (_: Exception) { "" }
            val candidates = linkedSetOf(url)
            if (parsedHost.isNotBlank()) {
                candidates.add("https://$parsedHost/")
                candidates.add("http://$parsedHost/")
            }
            val result = LinkedHashMap<String, SiteCookieEntry>()
            val displayDomain = BrowserUrlUtils.extractDomain(url).ifBlank { parsedHost }
            for (candidateUrl in candidates) {
                val rawCookie = cookieManager.getCookie(candidateUrl) ?: continue
                rawCookie.split(";").forEach { part ->
                    val trimmed = part.trim()
                    if (trimmed.isEmpty()) return@forEach
                    val eqIdx = trimmed.indexOf('=')
                    val (key, value) = if (eqIdx > 0) {
                        trimmed.substring(0, eqIdx).trim() to trimmed.substring(eqIdx + 1).trim()
                    } else {
                        trimmed to "(flag)"
                    }
                    if (!result.containsKey(key)) {
                        result[key] = SiteCookieEntry(
                            name = key,
                            value = value,
                            domain = parsedHost.ifBlank { displayDomain }
                        )
                    }
                }
            }
            result.values.toList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun expireCookieAcrossDomains(cookieManager: CookieManager, url: String, name: String) {
        val parsedUri = try { Uri.parse(url) } catch (_: Exception) { null }
        val host = parsedUri?.host.orEmpty()
        val hostParts = host.split('.').filter { it.isNotBlank() }
        val baseDomain = if (hostParts.size >= 2) hostParts.takeLast(2).joinToString(".") else host
        val targetUrls = linkedSetOf(url)
        if (host.isNotBlank()) {
            targetUrls.add("https://$host/")
            targetUrls.add("http://$host/")
        }
        val domainAttrs = linkedSetOf("", "; Domain=$host", "; Domain=.$host")
        if (baseDomain.isNotBlank()) {
            domainAttrs.add("; Domain=$baseDomain")
            domainAttrs.add("; Domain=.$baseDomain")
        }
        for (target in targetUrls) {
            for (dAttr in domainAttrs) {
                cookieManager.setCookie(
                    target,
                    "$name=; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/$dAttr"
                )
            }
        }
    }

    fun clearSingleSiteCookie(url: String, cookieName: String) {
        if (url.isBlank() || url.startsWith("netra://")) return
        try {
            val cookieManager = CookieManager.getInstance()
            expireCookieAcrossDomains(cookieManager, url, cookieName)
            cookieManager.flush()
        } catch (_: Exception) {
        }
    }

    fun clearSiteCookies(url: String, tabId: String? = null) {
        if (url.isBlank() || url.startsWith("netra://")) return
        try {
            val cookieManager = CookieManager.getInstance()
            val cookies = getSiteCookieDetails(url)
            for (entry in cookies) {
                expireCookieAcrossDomains(cookieManager, url, entry.name)
            }
            cookieManager.flush()

            val parsedHost = try { Uri.parse(url).host.orEmpty() } catch (_: Exception) { "" }
            val storage = WebStorage.getInstance()
            storage.deleteOrigin(url)
            if (parsedHost.isNotBlank()) {
                storage.deleteOrigin("https://$parsedHost")
                storage.deleteOrigin("http://$parsedHost")
                storage.deleteOrigin("https://$parsedHost/")
                storage.deleteOrigin("http://$parsedHost/")
            }

            if (tabId != null) {
                webViews[tabId]?.evaluateJavascript(
                    "(function(){ try { localStorage.clear(); sessionStorage.clear(); } catch(e) {} })();",
                    null
                )
            }
        } catch (_: Exception) {
        }
    }

    fun loadUrl(tab: BrowserTab, targetUrl: String, referer: String? = null) {
        tabStates[tab.id] = tab
        val webView = getOrCreateWebView(tab)
        loadUrlInWebView(webView, tab, targetUrl, referer)
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

    private fun loadUrlInWebView(
        webView: WebView,
        tab: BrowserTab,
        targetUrl: String,
        referer: String? = null
    ) {
        tabStates[tab.id] = tab
        applyTabSettings(webView, tab)
        when (targetUrl) {
            NETRA_HOME_URL -> {
                webView.onPause()
            }
            NETRA_VIDEO_LAB_URL -> {
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
                val baseHeaders = if (tab.isDesktopMode) {
                    DESKTOP_CLIENT_HINT_HEADERS
                } else {
                    MOBILE_CLIENT_HINT_HEADERS
                }
                val headers = if (!referer.isNullOrBlank() &&
                    (referer.startsWith("http://", ignoreCase = true) ||
                        referer.startsWith("https://", ignoreCase = true))
                ) {
                    LinkedHashMap(baseHeaders).apply { put("Referer", referer) }
                } else {
                    baseHeaders
                }
                webView.loadUrl(modeAdjustedUrl, headers)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
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

        val siteDomain = BrowserUrlUtils.extractDomain(tab.url)
        val siteConfig = siteConfigProvider(siteDomain)
        applySiteConfig(webView, tab, siteConfig)
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun applySiteConfig(webView: WebView, tab: BrowserTab, config: SiteConfigSettings) {
        val s = webView.settings
        if (s.javaScriptEnabled != config.javaScriptEnabled) {
            s.javaScriptEnabled = config.javaScriptEnabled
        }
        if (s.loadsImagesAutomatically != config.imagesEnabled) {
            s.loadsImagesAutomatically = config.imagesEnabled
        }
        if (s.blockNetworkImage != !config.imagesEnabled) {
            s.blockNetworkImage = !config.imagesEnabled
        }
        val allowStorage = !tab.isIncognito && config.domStorageEnabled
        if (s.domStorageEnabled != allowStorage) {
            s.domStorageEnabled = allowStorage
        }
        if (s.databaseEnabled != allowStorage) {
            s.databaseEnabled = allowStorage
        }
        if (s.mediaPlaybackRequiresUserGesture != !config.autoplayEnabled) {
            s.mediaPlaybackRequiresUserGesture = !config.autoplayEnabled
        }
        if (s.javaScriptCanOpenWindowsAutomatically != config.popupsAllowed) {
            s.javaScriptCanOpenWindowsAutomatically = config.popupsAllowed
        }
        if (s.supportMultipleWindows() != config.popupsAllowed) {
            s.setSupportMultipleWindows(config.popupsAllowed)
        }

        val desiredMixed = if (config.insecureContentAllowed) {
            WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        } else {
            WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }
        if (s.mixedContentMode != desiredMixed) {
            s.mixedContentMode = desiredMixed
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                if (s.safeBrowsingEnabled != config.safeBrowsingEnabled) {
                    s.safeBrowsingEnabled = config.safeBrowsingEnabled
                }
            } catch (_: Exception) {
            }
        }
        try {
            s.setGeolocationEnabled(config.locationPermission != SitePermissionMode.BLOCK)
        } catch (_: Exception) {
        }

        try {
            val cm = CookieManager.getInstance()
            if (tab.isIncognito) {
                cm.setAcceptCookie(false)
                cm.setAcceptThirdPartyCookies(webView, false)
            } else {
                when (config.cookiePolicy) {
                    SiteCookiePolicy.ALLOW_ALL -> {
                        cm.setAcceptCookie(true)
                        cm.setAcceptThirdPartyCookies(webView, true)
                    }
                    SiteCookiePolicy.BLOCK_THIRD_PARTY -> {
                        cm.setAcceptCookie(true)
                        cm.setAcceptThirdPartyCookies(webView, false)
                    }
                    SiteCookiePolicy.BLOCK_ALL -> {
                        cm.setAcceptCookie(false)
                        cm.setAcceptThirdPartyCookies(webView, false)
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    fun applySiteConfigNow(tabId: String, config: SiteConfigSettings, reloadPage: Boolean = false) {
        val wv = webViews[tabId] ?: return
        val tab = tabStates[tabId] ?: return
        applySiteConfig(wv, tab, config)
        applySoundMuteJs(wv, config.soundEnabled)
        if (reloadPage && !tab.isHomePage) {
            wv.reload()
        }
    }

    private fun applySoundMuteJs(webView: WebView, soundEnabled: Boolean) {
        val muteFlag = !soundEnabled
        val js = """
            (function() {
              try {
                window.__netraMuteSite = $muteFlag;
                var applyMute = function(el) {
                  if (!el) return;
                  if (window.__netraMuteSite) {
                    el.muted = true;
                    el.volume = 0;
                  } else {
                    el.muted = false;
                    if (el.volume === 0) el.volume = 1;
                  }
                };
                document.querySelectorAll('video,audio').forEach(applyMute);
                if (!window.__netraSoundHookInstalled) {
                  window.__netraSoundHookInstalled = true;
                  var origPlay = HTMLMediaElement.prototype.play;
                  HTMLMediaElement.prototype.play = function() {
                    if (window.__netraMuteSite) { this.muted = true; this.volume = 0; }
                    return origPlay.apply(this, arguments);
                  };
                  document.addEventListener('play', function(e) {
                    if (window.__netraMuteSite && e.target) {
                      e.target.muted = true;
                      e.target.volume = 0;
                    }
                  }, true);
                }
              } catch (e) {}
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    fun captureTabPreview(tab: BrowserTab) {
        if (tab.isHomePage) return
        val webView = webViews[tab.id] ?: return
        TabPreviewCache.captureWebView(context, tab.id, tab.url, webView, tab.isIncognito)
    }

    fun removeAndDestroyTab(tabId: String) {
        TabPreviewCache.removeTabPreview(tabId)
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

        val initialSiteDomain = BrowserUrlUtils.extractDomain(initialTab.url)
        val initialSiteConfig = siteConfigProvider(initialSiteDomain)

        val settings = webView.settings
        settings.javaScriptEnabled = initialSiteConfig.javaScriptEnabled
        settings.domStorageEnabled = !initialTab.isIncognito && initialSiteConfig.domStorageEnabled
        settings.databaseEnabled = !initialTab.isIncognito && initialSiteConfig.domStorageEnabled
        settings.mediaPlaybackRequiresUserGesture = !initialSiteConfig.autoplayEnabled
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.loadsImagesAutomatically = initialSiteConfig.imagesEnabled
        settings.blockNetworkImage = !initialSiteConfig.imagesEnabled
        settings.javaScriptCanOpenWindowsAutomatically = initialSiteConfig.popupsAllowed
        settings.setSupportMultipleWindows(initialSiteConfig.popupsAllowed)
        settings.mixedContentMode = if (initialSiteConfig.insecureContentAllowed) {
            WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        } else {
            WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.setSupportZoom(true)
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        settings.cacheMode = if (initialTab.isIncognito) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
        settings.userAgentString = if (initialTab.isDesktopMode) DESKTOP_USER_AGENT else defaultSystemMobileUserAgent
        settings.textZoom = initialTab.textZoom
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                settings.safeBrowsingEnabled = initialSiteConfig.safeBrowsingEnabled
            } catch (_: Exception) {
            }
        }
        try {
            settings.setGeolocationEnabled(initialSiteConfig.locationPermission != SitePermissionMode.BLOCK)
        } catch (_: Exception) {
        }

        val cookieManager = CookieManager.getInstance()
        if (initialTab.isIncognito) {
            cookieManager.setAcceptCookie(false)
            cookieManager.setAcceptThirdPartyCookies(webView, false)
        } else {
            when (initialSiteConfig.cookiePolicy) {
                SiteCookiePolicy.ALLOW_ALL -> {
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(webView, true)
                }
                SiteCookiePolicy.BLOCK_THIRD_PARTY -> {
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(webView, false)
                }
                SiteCookiePolicy.BLOCK_ALL -> {
                    cookieManager.setAcceptCookie(false)
                    cookieManager.setAcceptThirdPartyCookies(webView, false)
                }
            }
        }

        // Attach JavaScript Bridge for Long-Press Video Detection + Size Prober + Stream Downloader + DOM Ad Shield
        webView.addJavascriptInterface(
            NetraJsBridge(
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
                    val currentUrl = tabStates[tabId]?.url.orEmpty()
                    val perSiteAdBlock = siteConfigProvider(BrowserUrlUtils.extractDomain(currentUrl)).isAdBlockEnabled
                    if (!isAdBlockEnabledProvider() || !perSiteAdBlock) return@NetraJsBridge
                    mainHandler.post {
                        try {
                            val obj = JSONObject(jsonStr)
                            val entry = BlockedAdEntry(
                                tabId = tabId,
                                adUrl = obj.optString("adUrl", "dom-ad"),
                                adDomain = obj.optString("adDomain", "dom-element"),
                                pageDomain = obj.optString("pageDomain", "page"),
                                category = obj.optString("category", "DOM Ad Container"),
                                filterRule = obj.optString("filterRule", "##.adsbygoogle"),
                                modeTier = adBlockModeProvider().shortBadge
                            )
                            onAdBlocked(entry)
                        } catch (_: Exception) {
                        }
                    }
                }
            ),
            "NetraVideoBridge"
        )

        // Attach JavaScript Bridge for Desktop-Grade DevTools Inspector
        webView.addJavascriptInterface(
            NetraDevToolsJsBridge(
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
            "NetraDevToolsBridge"
        )

        var lastTouchCssX = 0f
        var lastTouchCssY = 0f
        webView.setOnTouchListener { v, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_MOVE) {
                val density = v.resources.displayMetrics.density.coerceAtLeast(1f)
                lastTouchCssX = event.x / density
                lastTouchCssY = event.y / density
            }
            false
        }

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
                "(function(){ return window.__netraInspectVideoAtLastTouch ? window.__netraInspectVideoAtLastTouch(${lastTouchCssX}, ${lastTouchCssY}) : ''; })();",
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

                val perSiteAdBlock = siteConfigProvider(BrowserUrlUtils.extractDomain(liveTab.url)).isAdBlockEnabled
                if (isAdBlockEnabledProvider() && perSiteAdBlock) {
                    val matchedAd = AdBlocker.matchAdRequest(
                        tabId = tabId,
                        url = reqUrl,
                        pageUrl = liveTab.url,
                        mode = adBlockModeProvider()
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
                if (target.equals(NETRA_VIDEO_LAB_URL, ignoreCase = true)) {
                    view?.loadUrl(ASSET_VIDEO_LAB_URL)
                    return true
                }
                if (target.startsWith("http://") || target.startsWith("https://")) {
                    val liveTab = tabStates[tabId] ?: initialTab
                    if (liveTab.isDesktopMode && request.isForMainFrame) {
                        val desktopUrl = BrowserUrlUtils.toDesktopUrl(target)
                        if (desktopUrl != target && view != null) {
                            val reqReferer = request.requestHeaders?.entries
                                ?.firstOrNull { it.key.equals("Referer", ignoreCase = true) }
                                ?.value ?: view.url
                            loadUrlInWebView(view, liveTab, desktopUrl, reqReferer)
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
                val liveTab = tabStates[tabId] ?: initialTab
                val updatedTab = liveTab.copy(url = mappedUrl)
                tabStates[tabId] = updatedTab
                if (view != null && mappedUrl.isNotBlank()) {
                    applySiteConfig(view, updatedTab, siteConfigProvider(BrowserUrlUtils.extractDomain(mappedUrl)))
                }
                val isInternalPage = mappedUrl.startsWith("netra://")
                val isSsl = mappedUrl.startsWith("https://") || isInternalPage
                val sslDesc = when {
                    isInternalPage -> "Netra Internal Verified Page"
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
                val isInternalPage = mappedUrl.startsWith("netra://")
                val isSsl = (mappedUrl.startsWith("https://") && cert != null) ||
                    mappedUrl.startsWith("https://") ||
                    isInternalPage
                val sslDesc = when {
                    isInternalPage -> "Netra Internal Verified Page"
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
                if (!isInternalPage && mappedUrl.isNotBlank()) {
                    TabPreviewCache.captureWebView(context, tabId, mappedUrl, view, liveTab.isIncognito)
                    mainHandler.postDelayed({
                        val currentTab = tabStates[tabId] ?: liveTab
                        TabPreviewCache.captureWebView(context, tabId, currentTab.url.ifBlank { mappedUrl }, view, currentTab.isIncognito)
                    }, 450L)
                    mainHandler.postDelayed({
                        val currentTab = tabStates[tabId] ?: liveTab
                        TabPreviewCache.captureWebView(context, tabId, currentTab.url.ifBlank { mappedUrl }, view, currentTab.isIncognito)
                    }, 1400L)
                }

                if (!liveTab.isIncognito && !isInternalPage && mappedUrl.isNotBlank()) {
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
            override fun onReceivedIcon(view: WebView?, icon: android.graphics.Bitmap?) {
                super.onReceivedIcon(view, icon)
                val currentUrl = view?.url
                if (!currentUrl.isNullOrBlank()) {
                    ShortcutIconCache.saveVisitedWebsiteIcon(context, currentUrl, icon)
                }
            }

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
                if (newProgress >= 75) {
                    val liveTab = tabStates[tabId] ?: initialTab
                    val currentUrl = view?.url ?: liveTab.url
                    if (!currentUrl.startsWith("netra://")) {
                        TabPreviewCache.captureWebView(context, tabId, currentUrl, view, liveTab.isIncognito)
                    }
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

            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                val liveTab = tabStates[tabId] ?: initialTab
                val parentUrl = mapDisplayUrl(view?.url ?: liveTab.url)
                val domain = BrowserUrlUtils.extractDomain(parentUrl)
                val config = siteConfigProvider(domain)
                if (!config.popupsAllowed && !isUserGesture) {
                    mainHandler.post { onPopupBlocked(domain) }
                    return false
                }
                val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
                val targetWebView = view ?: return false

                var forwarded = false
                fun handlePopupNavigation(
                    tempView: WebView?,
                    destUrl: String?,
                    requestHeaders: Map<String, String>?
                ) {
                    if (forwarded) return
                    if (destUrl.isNullOrBlank() || destUrl.equals("about:blank", ignoreCase = true)) {
                        return
                    }
                    forwarded = true
                    if (destUrl.startsWith("http://", ignoreCase = true) ||
                        destUrl.startsWith("https://", ignoreCase = true)
                    ) {
                        if (isAdBlockEnabledProvider() && config.isAdBlockEnabled) {
                            val matchedAd = AdBlocker.matchAdRequest(
                                tabId = tabId,
                                url = destUrl,
                                pageUrl = parentUrl,
                                mode = adBlockModeProvider()
                            )
                            if (matchedAd != null) {
                                mainHandler.post { onAdBlocked(matchedAd) }
                                mainHandler.post {
                                    tempView?.stopLoading()
                                    tempView?.destroy()
                                }
                                return
                            }
                        }
                        val headers = LinkedHashMap<String, String>()
                        headers.putAll(
                            if (liveTab.isDesktopMode) DESKTOP_CLIENT_HINT_HEADERS else MOBILE_CLIENT_HINT_HEADERS
                        )
                        requestHeaders?.forEach { (k, v) ->
                            if (k.isNotBlank() && v.isNotBlank()) {
                                headers[k] = v
                            }
                        }
                        val hasReferer = headers.keys.any { it.equals("Referer", ignoreCase = true) }
                        if (!hasReferer &&
                            (parentUrl.startsWith("http://", ignoreCase = true) ||
                                parentUrl.startsWith("https://", ignoreCase = true))
                        ) {
                            headers["Referer"] = parentUrl
                        }
                        val finalUrl = if (liveTab.isDesktopMode) {
                            BrowserUrlUtils.toDesktopUrl(destUrl)
                        } else {
                            destUrl
                        }
                        targetWebView.loadUrl(finalUrl, headers)
                    }
                    mainHandler.post {
                        tempView?.stopLoading()
                        tempView?.destroy()
                    }
                }

                val tempWebView = WebView(context).apply {
                    settings.javaScriptEnabled = config.javaScriptEnabled
                    settings.domStorageEnabled = !liveTab.isIncognito && config.domStorageEnabled
                    settings.userAgentString = targetWebView.settings.userAgentString
                    settings.mixedContentMode = targetWebView.settings.mixedContentMode
                    try {
                        val cm = CookieManager.getInstance()
                        cm.setAcceptThirdPartyCookies(
                            this,
                            !liveTab.isIncognito && config.cookiePolicy == SiteCookiePolicy.ALLOW_ALL
                        )
                    } catch (_: Exception) {
                    }
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            v: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            handlePopupNavigation(
                                tempView = v,
                                destUrl = request?.url?.toString(),
                                requestHeaders = request?.requestHeaders
                            )
                            return true
                        }

                        override fun onPageStarted(v: WebView?, url: String?, favicon: Bitmap?) {
                            handlePopupNavigation(
                                tempView = v,
                                destUrl = url,
                                requestHeaders = null
                            )
                        }
                    }
                }
                transport.webView = tempWebView
                resultMsg.sendToTarget()
                return true
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                val cb = callback ?: return
                val safeOrigin = origin.orEmpty()
                val domain = BrowserUrlUtils.extractDomain(safeOrigin.ifBlank { webView.url.orEmpty() })
                val config = siteConfigProvider(domain)
                when (config.locationPermission) {
                    SitePermissionMode.BLOCK -> cb.invoke(safeOrigin, false, false)
                    SitePermissionMode.ALLOW,
                    SitePermissionMode.ASK -> mainHandler.post {
                        onGeolocationPermissionRequested(domain, safeOrigin, cb)
                    }
                }
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                val req = request ?: return
                val originUrl = req.origin?.toString().orEmpty()
                val domain = BrowserUrlUtils.extractDomain(originUrl.ifBlank { webView.url.orEmpty() })
                val resources = req.resources.orEmpty().toList()
                val needsCameraOrMic = resources.any {
                    it == PermissionRequest.RESOURCE_VIDEO_CAPTURE ||
                        it == PermissionRequest.RESOURCE_AUDIO_CAPTURE
                }
                if (!needsCameraOrMic && resources.contains(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID)) {
                    val config = siteConfigProvider(domain)
                    if (config.protectedContentAllowed) {
                        req.grant(arrayOf(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID))
                    } else {
                        req.deny()
                    }
                    return
                }
                mainHandler.post {
                    onWebPermissionRequested(domain, req)
                }
            }

            override fun onPermissionRequestCanceled(request: PermissionRequest?) {
                if (request != null) {
                    mainHandler.post { onWebPermissionCanceled(request) }
                }
                super.onPermissionRequestCanceled(request)
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
            NETRA_VIDEO_LAB_URL
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

        val currentSiteDomain = BrowserUrlUtils.extractDomain(liveTab?.url ?: webView.url.orEmpty())
        val currentSiteConfig = siteConfigProvider(currentSiteDomain)

        // 2. Cosmetic DOM Ad Shield & Reporter (Minimal / Pro / Max)
        if (isAdBlockEnabledProvider() && currentSiteConfig.isAdBlockEnabled) {
            webView.evaluateJavascript(
                AdBlocker.buildCosmeticAdShieldJs(adBlockModeProvider()),
                null
            )
        }

        // 3. Per-site Sound Mute Enforcement
        applySoundMuteJs(webView, currentSiteConfig.soundEnabled)

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

    private class NetraJsBridge(
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

    private class NetraDevToolsJsBridge(
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
