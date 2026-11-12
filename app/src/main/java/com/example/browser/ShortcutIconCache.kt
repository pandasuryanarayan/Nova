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
 * Lightweight, memory-safe Favicon & App Icon Cache for Quick Shortcuts.
 * - Keeps at most 32 small (64x64) icons in RAM via LruCache (~250 KB total).
 * - Persists fetched icons to internal mobile cache (`cacheDir/shortcut_icons/`).
 * - Never polls or fetches continuously: only fetches once if missing from disk cache.
 */
object ShortcutIconCache {
    private const val ICON_SIZE_PX = 64

    private val memoryCache = object : LruCache<String, ImageBitmap>(32) {}
    private val attemptedInSession: MutableSet<String> =
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
        val dir = File(context.cacheDir, "shortcut_icons")
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

    suspend fun loadOrFetchOnce(context: Context, rawUrl: String): ImageBitmap? =
        withContext(Dispatchers.IO) {
            val key = normalizeDomainKey(rawUrl)
            val host = extractCleanHost(rawUrl)
            if (key.isBlank() || host.isBlank()) return@withContext null

            memoryCache.get(key)?.let { return@withContext it }

            val diskFile = getCacheFile(context, key)
            if (diskFile.exists() && diskFile.length() > 64L) {
                decodeBitmapSafely(diskFile)?.let { bmp ->
                    val img = bmp.asImageBitmap()
                    memoryCache.put(key, img)
                    return@withContext img
                }
            }

            // Do not fetch continuously — attempt network fetch at most once per domain per session
            if (!attemptedInSession.add(key)) {
                return@withContext null
            }

            val candidateUrls = listOf(
                "https://www.google.com/s2/favicons?domain=$host&sz=128",
                "https://icons.duckduckgo.com/ip3/$host.ico"
            )

            for (endpoint in candidateUrls) {
                val downloaded = downloadAndScaleIcon(endpoint)
                if (downloaded != null) {
                    try {
                        FileOutputStream(diskFile).use { out ->
                            downloaded.compress(Bitmap.CompressFormat.PNG, 90, out)
                        }
                    } catch (_: Exception) {
                    }
                    val img = downloaded.asImageBitmap()
                    memoryCache.put(key, img)
                    return@withContext img
                }
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
                connectTimeout = 5000
                readTimeout = 5000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
            }
            if (connection.responseCode !in 200..299) return null
            val bytes = connection.inputStream.use { it.readBytes() }
            if (bytes.isEmpty() || bytes.size > 512 * 1024) return null

            val opts = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val rawBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts) ?: return null
            if (rawBitmap.width <= 0 || rawBitmap.height <= 0) return null

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
