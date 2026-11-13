package com.example.browser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Collections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Real Website Favicon & App Icon Cache for Quick Shortcuts.
 * - Keeps up to 48 crisp (96x96) website icons in RAM via LruCache.
 * - Persists fetched icons to internal storage (`filesDir/shortcut_icons/`) so they survive cache clears.
 * - Fetches real website icons from Google S2 Favicon API, DuckDuckGo Icon API, or direct `/favicon.ico`.
 */
object ShortcutIconCache {
    private const val ICON_SIZE_PX = 96

    private val memoryCache = object : LruCache<String, ImageBitmap>(48) {}
    private val inFlightDomains: MutableSet<String> =
        Collections.synchronizedSet(HashSet<String>())

    private fun normalizeDomainKey(rawUrl: String): String {
        val domain = BrowserUrlUtils.extractDomain(rawUrl)
            .lowercase()
            .removePrefix("www.")
            .removePrefix("m.")
            .trim()
        return domain.replace(Regex("[^a-z0-9._-]"), "_")
    }

    private fun extractCleanHost(rawUrl: String): String {
        return BrowserUrlUtils.extractDomain(rawUrl)
            .lowercase()
            .removePrefix("m.")
            .trim()
    }

    private fun getCacheFile(context: Context, domainKey: String): File {
        val dir = File(context.filesDir, "shortcut_icons")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, "$domainKey.png")
    }

    fun getFromMemory(rawUrl: String): ImageBitmap? {
        val key = normalizeDomainKey(rawUrl)
        if (key.isBlank()) return null
        return memoryCache.get(key)
    }

    fun saveVisitedWebsiteIcon(context: Context, rawUrl: String, bitmap: Bitmap?) {
        if (bitmap == null || bitmap.width <= 0 || bitmap.height <= 0) return
        val key = normalizeDomainKey(rawUrl)
        if (key.isBlank()) return
        try {
            val scaled = if (bitmap.width == ICON_SIZE_PX && bitmap.height == ICON_SIZE_PX) {
                bitmap
            } else {
                Bitmap.createScaledBitmap(bitmap, ICON_SIZE_PX, ICON_SIZE_PX, true)
            }
            val diskFile = getCacheFile(context.applicationContext, key)
            FileOutputStream(diskFile).use { out ->
                scaled.compress(Bitmap.CompressFormat.PNG, 92, out)
            }
            memoryCache.put(key, scaled.asImageBitmap())
        } catch (_: Throwable) {
        }
    }

    suspend fun loadOrFetchOnce(context: Context, rawUrl: String): ImageBitmap? =
        withContext(Dispatchers.IO) {
            val key = normalizeDomainKey(rawUrl)
            val host = extractCleanHost(rawUrl)
            if (key.isBlank() || host.isBlank() || host.startsWith("nova")) return@withContext null

            memoryCache.get(key)?.let { return@withContext it }

            val diskFile = getCacheFile(context, key)
            if (diskFile.exists() && diskFile.length() > 64L) {
                decodeBitmapSafely(diskFile)?.let { bmp ->
                    val img = bmp.asImageBitmap()
                    memoryCache.put(key, img)
                    return@withContext img
                }
            }

            // Also check legacy cacheDir if previously stored there
            val legacyFile = File(File(context.cacheDir, "shortcut_icons"), "$key.png")
            if (legacyFile.exists() && legacyFile.length() > 64L) {
                decodeBitmapSafely(legacyFile)?.let { bmp ->
                    try {
                        legacyFile.copyTo(diskFile, overwrite = true)
                    } catch (_: Exception) {
                    }
                    val img = bmp.asImageBitmap()
                    memoryCache.put(key, img)
                    return@withContext img
                }
            }

            if (!inFlightDomains.add(key)) {
                return@withContext memoryCache.get(key)
            }

            try {
                val candidateUrls = listOf(
                    "https://www.google.com/s2/favicons?domain_url=https://$host&sz=128",
                    "https://www.google.com/s2/favicons?domain=$host&sz=128",
                    "https://icons.duckduckgo.com/ip3/$host.ico",
                    "https://$host/apple-touch-icon.png",
                    "https://$host/favicon.ico"
                )

                for (endpoint in candidateUrls) {
                    val downloaded = downloadAndScaleIcon(endpoint)
                    if (downloaded != null) {
                        try {
                            FileOutputStream(diskFile).use { out ->
                                downloaded.compress(Bitmap.CompressFormat.PNG, 92, out)
                            }
                        } catch (_: Exception) {
                        }
                        val img = downloaded.asImageBitmap()
                        memoryCache.put(key, img)
                        return@withContext img
                    }
                }
            } finally {
                inFlightDomains.remove(key)
            }

            null
        }

    private fun decodeBitmapSafely(file: File): Bitmap? {
        return try {
            val opts = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(file.absolutePath, opts)
        } catch (_: Throwable) {
            null
        }
    }

    private fun downloadAndScaleIcon(endpoint: String): Bitmap? {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 6000
                instanceFollowRedirects = true
                setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                )
            }
            if (connection.responseCode !in 200..299) return null
            val bytes = connection.inputStream.use { it.readBytes() }
            if (bytes.isEmpty() || bytes.size > 768 * 1024) return null

            val opts = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val rawBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts) ?: return null
            if (rawBitmap.width <= 4 || rawBitmap.height <= 4) return null

            if (rawBitmap.width == ICON_SIZE_PX && rawBitmap.height == ICON_SIZE_PX) {
                rawBitmap
            } else {
                val scaled = Bitmap.createScaledBitmap(rawBitmap, ICON_SIZE_PX, ICON_SIZE_PX, true)
                if (scaled != rawBitmap) {
                    rawBitmap.recycle()
                }
                scaled
            }
        } catch (_: Throwable) {
            null
        } finally {
            try {
                connection?.disconnect()
            } catch (_: Exception) {
            }
        }
    }
}

@Composable
fun rememberShortcutIcon(context: Context, url: String): State<ImageBitmap?> {
    val iconState = remember(url) {
        mutableStateOf(ShortcutIconCache.getFromMemory(url))
    }
    LaunchedEffect(url) {
        if (iconState.value == null) {
            iconState.value = ShortcutIconCache.loadOrFetchOnce(context.applicationContext, url)
        }
    }
    return iconState
}
