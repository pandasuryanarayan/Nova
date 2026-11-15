package com.example.browser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.util.LruCache
import android.view.View
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Collections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Real Webpage Screenshot & Preview Cache for Open Tabs (Chrome-style tab previews).
 * - Captures actual rendered WebView viewports into scaled bitmaps.
 * - Keeps crisp previews in RAM via LruCache and persists to internal storage (`filesDir/tab_previews/`).
 * - Falls back to real webpage preview snapshot / OpenGraph image when a tab has not been rendered on screen yet.
 */
object TabPreviewCache {
    private const val PREVIEW_WIDTH_PX = 420
    private const val PREVIEW_HEIGHT_PX = 280

    private val tabMemoryCache = object : LruCache<String, ImageBitmap>(32) {}
    private val urlMemoryCache = object : LruCache<String, ImageBitmap>(48) {}
    private val inFlightKeys: MutableSet<String> = Collections.synchronizedSet(HashSet<String>())
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _versionFlow = MutableStateFlow(0L)
    val versionFlow: StateFlow<Long> = _versionFlow

    private fun normalizeUrlKey(rawUrl: String): String {
        val cleaned = rawUrl.trim().lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .trimEnd('/')
        if (cleaned.isBlank() || cleaned.startsWith("netra:")) return ""
        val safe = cleaned.replace(Regex("[^a-z0-9._-]"), "_").take(90)
        return "${safe}_${cleaned.hashCode().toUInt().toString(16)}"
    }

    private fun getCacheFile(context: Context, urlKey: String): File {
        val dir = File(context.filesDir, "tab_previews")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "$urlKey.jpg")
    }

    fun getFromMemory(tabId: String, rawUrl: String): ImageBitmap? {
        tabMemoryCache.get(tabId)?.let { return it }
        val urlKey = normalizeUrlKey(rawUrl)
        if (urlKey.isNotBlank()) {
            urlMemoryCache.get(urlKey)?.let { return it }
        }
        return null
    }

    fun removeTabPreview(tabId: String) {
        tabMemoryCache.remove(tabId)
    }

    fun captureWebView(
        context: Context,
        tabId: String,
        rawUrl: String,
        webView: WebView?,
        isIncognito: Boolean = false
    ): ImageBitmap? {
        if (webView == null) return null
        val url = rawUrl.ifBlank { webView.url ?: "" }
        if (url.isBlank() || url.startsWith("netra://") || url == "about:blank") {
            return null
        }
        return try {
            if (webView.width <= 0 || webView.height <= 0) {
                val wSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
                val hSpec = View.MeasureSpec.makeMeasureSpec(1620, View.MeasureSpec.EXACTLY)
                webView.measure(wSpec, hSpec)
                webView.layout(0, 0, 1080, 1620)
            }
            val srcW = webView.width.coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(PREVIEW_WIDTH_PX, PREVIEW_HEIGHT_PX, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            val scale = PREVIEW_WIDTH_PX.toFloat() / srcW.toFloat()
            canvas.save()
            canvas.scale(scale, scale)
            canvas.translate(-webView.scrollX.toFloat(), -webView.scrollY.toFloat())
            webView.draw(canvas)
            canvas.restore()

            if (isBlankFrame(bitmap)) {
                bitmap.recycle()
                return getFromMemory(tabId, url)
            }

            val img = bitmap.asImageBitmap()
            tabMemoryCache.put(tabId, img)
            val urlKey = normalizeUrlKey(url)
            if (urlKey.isNotBlank()) {
                urlMemoryCache.put(urlKey, img)
                if (!isIncognito) {
                    val appCtx = context.applicationContext
                    ioScope.launch {
                        try {
                            val file = getCacheFile(appCtx, urlKey)
                            FileOutputStream(file).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 82, out)
                            }
                        } catch (_: Throwable) {
                        }
                    }
                }
            }
            _versionFlow.value = _versionFlow.value + 1L
            img
        } catch (_: Throwable) {
            null
        }
    }

    private fun isBlankFrame(bmp: Bitmap): Boolean {
        val w = bmp.width
        val h = bmp.height
        if (w <= 4 || h <= 4) return true
        val first = bmp.getPixel(w / 2, h / 2)
        val pts = intArrayOf(
            bmp.getPixel(w / 4, h / 4),
            bmp.getPixel(3 * w / 4, h / 4),
            bmp.getPixel(w / 3, h / 2),
            bmp.getPixel(2 * w / 3, h / 2),
            bmp.getPixel(w / 4, 3 * h / 4),
            bmp.getPixel(3 * w / 4, 3 * h / 4),
            bmp.getPixel(w / 2, h / 5),
            bmp.getPixel(w / 2, 4 * h / 5)
        )
        return pts.all { it == first }
    }

    suspend fun loadOrFetchOnce(
        context: Context,
        tabId: String,
        rawUrl: String,
        isIncognito: Boolean = false
    ): ImageBitmap? = withContext(Dispatchers.IO) {
        getFromMemory(tabId, rawUrl)?.let { return@withContext it }
        val urlKey = normalizeUrlKey(rawUrl)
        if (urlKey.isBlank()) return@withContext null

        val diskFile = getCacheFile(context, urlKey)
        if (diskFile.exists() && diskFile.length() > 128L) {
            decodeBitmapSafely(diskFile)?.let { bmp ->
                val img = bmp.asImageBitmap()
                tabMemoryCache.put(tabId, img)
                urlMemoryCache.put(urlKey, img)
                return@withContext img
            }
        }

        if (!inFlightKeys.add(urlKey)) {
            return@withContext getFromMemory(tabId, rawUrl)
        }

        try {
            val encodedUrl = URLEncoder.encode(rawUrl, "UTF-8")
            val endpoints = listOf(
                "https://s.wordpress.com/mshots/v1/$encodedUrl?w=$PREVIEW_WIDTH_PX&h=$PREVIEW_HEIGHT_PX",
                "https://image.thum.io/get/width/$PREVIEW_WIDTH_PX/crop/$PREVIEW_HEIGHT_PX/noanimate/$rawUrl"
            )
            for (endpoint in endpoints) {
                val downloaded = downloadPreviewBitmap(endpoint)
                if (downloaded != null && !isBlankFrame(downloaded)) {
                    if (!isIncognito) {
                        try {
                            FileOutputStream(diskFile).use { out ->
                                downloaded.compress(Bitmap.CompressFormat.JPEG, 82, out)
                            }
                        } catch (_: Throwable) {
                        }
                    }
                    val img = downloaded.asImageBitmap()
                    tabMemoryCache.put(tabId, img)
                    urlMemoryCache.put(urlKey, img)
                    _versionFlow.value = _versionFlow.value + 1L
                    return@withContext img
                }
            }
        } catch (_: Throwable) {
        } finally {
            inFlightKeys.remove(urlKey)
        }
        null
    }

    private fun decodeBitmapSafely(file: File): Bitmap? = try {
        BitmapFactory.decodeFile(
            file.absolutePath,
            BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
        )
    } catch (_: Throwable) {
        null
    }

    private fun downloadPreviewBitmap(endpoint: String): Bitmap? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 6000
                instanceFollowRedirects = true
                setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                )
            }
            if (conn.responseCode !in 200..299) return null
            val contentType = conn.contentType?.lowercase() ?: ""
            if (contentType.contains("gif")) return null
            val bytes = conn.inputStream.use { it.readBytes() }
            if (bytes.size < 512 || bytes.size > 2 * 1024 * 1024) return null
            val raw = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            if (raw.width <= 16 || raw.height <= 16) return null
            val scaled = Bitmap.createScaledBitmap(raw, PREVIEW_WIDTH_PX, PREVIEW_HEIGHT_PX, true)
            if (scaled != raw) raw.recycle()
            scaled
        } catch (_: Throwable) {
            null
        } finally {
            try {
                conn?.disconnect()
            } catch (_: Exception) {
            }
        }
    }
}

@Composable
fun rememberTabPreview(context: Context, tab: com.example.data.BrowserTab): State<ImageBitmap?> {
    val version by TabPreviewCache.versionFlow.collectAsState()
    val state = remember(tab.id, tab.url) {
        mutableStateOf(TabPreviewCache.getFromMemory(tab.id, tab.url))
    }
    LaunchedEffect(tab.id, tab.url, version) {
        val mem = TabPreviewCache.getFromMemory(tab.id, tab.url)
        if (mem != null) {
            state.value = mem
        } else if (!tab.isHomePage) {
            state.value = TabPreviewCache.loadOrFetchOnce(
                context = context.applicationContext,
                tabId = tab.id,
                rawUrl = tab.url,
                isIncognito = tab.isIncognito
            )
        }
    }
    return state
}
