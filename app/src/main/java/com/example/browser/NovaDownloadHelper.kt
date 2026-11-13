package com.example.browser

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.webkit.CookieManager
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.data.BrowserRepository
import com.example.data.DownloadItemEntity
import com.example.data.DownloadStatus
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLException
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

data class VideoProbeResult(
    val contentLength: Long = -1L,
    val contentType: String? = null,
    val isDirectMp4: Boolean = true,
    val isStreamingOrProtected: Boolean = false,
    val error: String? = null
)

private enum class HeaderProfile {
    PAGE_REFERER_WITH_RANGE,
    VIDEO_ORIGIN_REFERER_WITH_RANGE,
    NO_REFERER_WITH_RANGE,
    PAGE_REFERER_NO_RANGE,
    NO_REFERER_NO_RANGE
}

class NovaDownloadHelper(
    private val context: Context,
    private val repository: BrowserRepository,
    private val scope: CoroutineScope
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val winningProfileCache = ConcurrentHashMap<String, HeaderProfile>()
    private val activeDownloadJobs = ConcurrentHashMap<Long, Job>()

    private val webViewStreamFiles = ConcurrentHashMap<Long, FileOutputStream>()
    private val webViewStreamItems = ConcurrentHashMap<Long, DownloadItemEntity>()
    private val webViewStreamLastUpdate = ConcurrentHashMap<Long, Long>()
    private val webViewStreamCompletion = ConcurrentHashMap<Long, CompletableDeferred<Boolean>>()

    var webViewStreamTrigger: ((downloadId: Long, videoUrl: String) -> Boolean)? = null

    companion object {
        private const val CHANNEL_ID = "nova_video_downloads"
        const val SUBFOLDER_VIDEOS = "NovaBrowser/Videos"

        private val permissiveSslSocketFactory: SSLSocketFactory? by lazy {
            try {
                val trustAllCerts = arrayOf<TrustManager>(
                    object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    }
                )
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustAllCerts, SecureRandom())
                sslContext.socketFactory
            } catch (_: Exception) {
                null
            }
        }

        private val permissiveHostnameVerifier = HostnameVerifier { _, _ -> true }
    }

    init {
        createNotificationChannel()
    }

    private fun openConfiguredConnection(
        videoUrl: String,
        pageUrl: String,
        userAgent: String,
        cookies: String?,
        profile: HeaderProfile,
        rangeHeaderValue: String?,
        usePermissiveSsl: Boolean
    ): HttpURLConnection {
        val safeUrl = videoUrl.trim().replace(" ", "%20")
        val conn = (URL(safeUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 35_000
            instanceFollowRedirects = true
        }
        if (usePermissiveSsl && conn is HttpsURLConnection) {
            permissiveSslSocketFactory?.let { conn.sslSocketFactory = it }
            conn.hostnameVerifier = permissiveHostnameVerifier
        }

        val videoOrigin = extractOrigin(videoUrl)
        val pageOrigin = extractOrigin(pageUrl)
        val effectiveUa = userAgent.ifBlank { NovaWebViewPool.MOBILE_USER_AGENT }

        conn.setRequestProperty("User-Agent", effectiveUa)
        conn.setRequestProperty("Accept", "video/mp4,video/webm,video/*;q=0.9,*/*;q=0.8")
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9")

        if (!cookies.isNullOrBlank()) {
            conn.setRequestProperty("Cookie", cookies)
        }

        when (profile) {
            HeaderProfile.PAGE_REFERER_WITH_RANGE -> {
                if (pageUrl.startsWith("http")) {
                    conn.setRequestProperty("Referer", pageUrl)
                } else if (videoOrigin != null) {
                    conn.setRequestProperty("Referer", "$videoOrigin/")
                }
                if (pageOrigin != null) {
                    conn.setRequestProperty("Origin", pageOrigin)
                }
                if (!rangeHeaderValue.isNullOrBlank()) {
                    conn.setRequestProperty("Range", rangeHeaderValue)
                }
            }
            HeaderProfile.VIDEO_ORIGIN_REFERER_WITH_RANGE -> {
                if (videoOrigin != null) {
                    conn.setRequestProperty("Referer", "$videoOrigin/")
                    conn.setRequestProperty("Origin", videoOrigin)
                }
                if (!rangeHeaderValue.isNullOrBlank()) {
                    conn.setRequestProperty("Range", rangeHeaderValue)
                }
            }
            HeaderProfile.NO_REFERER_WITH_RANGE -> {
                if (!rangeHeaderValue.isNullOrBlank()) {
                    conn.setRequestProperty("Range", rangeHeaderValue)
                }
            }
            HeaderProfile.PAGE_REFERER_NO_RANGE -> {
                if (pageUrl.startsWith("http")) {
                    conn.setRequestProperty("Referer", pageUrl)
                }
            }
            HeaderProfile.NO_REFERER_NO_RANGE -> Unit
        }
        return conn
    }

    private fun <T> executeWithSslFallback(
        videoUrl: String,
        pageUrl: String,
        userAgent: String,
        cookies: String?,
        profile: HeaderProfile,
        rangeHeaderValue: String?,
        block: (HttpURLConnection) -> T
    ): T {
        val firstConn = openConfiguredConnection(
            videoUrl = videoUrl,
            pageUrl = pageUrl,
            userAgent = userAgent,
            cookies = cookies,
            profile = profile,
            rangeHeaderValue = rangeHeaderValue,
            usePermissiveSsl = false
        )
        return try {
            firstConn.connect()
            block(firstConn)
        } catch (_: SSLException) {
            firstConn.disconnect()
            val fallbackConn = openConfiguredConnection(
                videoUrl = videoUrl,
                pageUrl = pageUrl,
                userAgent = userAgent,
                cookies = cookies,
                profile = profile,
                rangeHeaderValue = rangeHeaderValue,
                usePermissiveSsl = true
            )
            try {
                fallbackConn.connect()
                block(fallbackConn)
            } finally {
                fallbackConn.disconnect()
            }
        } finally {
            firstConn.disconnect()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nova Video Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress for Nova Browser MP4 video downloads"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildVideoFileName(websiteTitle: String): String {
        val sanitizedTitle = websiteTitle
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .take(28)
            .ifBlank { "video" }
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(Date())
        return "${sanitizedTitle}_${timestamp}.mp4"
    }

    private fun getPublicVideoFile(fileName: String): File {
        val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val videoDir = File(publicDownloads, SUBFOLDER_VIDEOS)
        if (!videoDir.exists()) {
            try {
                videoDir.mkdirs()
            } catch (_: Exception) {
            }
        }
        return File(videoDir, fileName)
    }

    private fun getWritableVideoFile(fileName: String): File {
        val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(context.filesDir, "downloads")
        val videoDir = File(baseDir, SUBFOLDER_VIDEOS)
        if (!videoDir.exists()) {
            videoDir.mkdirs()
        }
        return File(videoDir, fileName)
    }

    private fun exportToPublicDownloads(sourceFile: File, fileName: String): Uri? {
        if (!sourceFile.exists() || sourceFile.length() <= 0L) return null
        return try {
            val publicFile = getPublicVideoFile(fileName)
            if (publicFile.exists() && publicFile.absolutePath == sourceFile.absolutePath) {
                return Uri.fromFile(publicFile)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "video/mp4")
                    put(MediaStore.Downloads.SIZE, sourceFile.length())
                    put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$SUBFOLDER_VIDEOS")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val itemUri = resolver.insert(collection, contentValues) ?: return null
                resolver.openOutputStream(itemUri)?.use { outStream ->
                    FileInputStream(sourceFile).use { inStream ->
                        inStream.copyTo(outStream, bufferSize = 64 * 1024)
                    }
                }
                val completedValues = ContentValues().apply {
                    put(MediaStore.Downloads.IS_PENDING, 0)
                }
                resolver.update(itemUri, completedValues, null, null)
                itemUri
            } else {
                val destFile = getPublicVideoFile(fileName)
                sourceFile.copyTo(destFile, overwrite = true)
                Uri.fromFile(destFile)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun getDirectCookieForUrl(url: String): String? {
        return try {
            CookieManager.getInstance().getCookie(url)?.trim()?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    private fun getMergedCookies(videoUrl: String, pageUrl: String): String? {
        return try {
            val cm = CookieManager.getInstance()
            val c1 = cm.getCookie(videoUrl)?.trim().orEmpty()
            val c2 = if (pageUrl.startsWith("http")) cm.getCookie(pageUrl)?.trim().orEmpty() else ""
            when {
                c1.isNotBlank() && c2.isNotBlank() && c1 != c2 -> {
                    val map = linkedMapOf<String, String>()
                    (c2.split(";") + c1.split(";")).forEach { pair ->
                        val trimmed = pair.trim()
                        val eq = trimmed.indexOf('=')
                        if (eq > 0) {
                            map[trimmed.substring(0, eq).trim()] = trimmed.substring(eq + 1).trim()
                        }
                    }
                    map.entries.joinToString("; ") { "${it.key}=${it.value}" }
                }
                c1.isNotBlank() -> c1
                c2.isNotBlank() -> c2
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun extractOrigin(rawUrl: String): String? {
        return try {
            val uri = Uri.parse(rawUrl)
            val scheme = uri.scheme ?: return null
            val host = uri.host ?: return null
            val port = if (uri.port != -1 && uri.port != 80 && uri.port != 443) ":${uri.port}" else ""
            "$scheme://$host$port"
        } catch (_: Exception) {
            null
        }
    }

    private fun extractTotalBytesFromConnection(conn: HttpURLConnection, code: Int, wasSmallRangeProbe: Boolean): Long {
        val contentRange = conn.getHeaderField("Content-Range")
        if (!contentRange.isNullOrBlank() && contentRange.contains('/')) {
            val totalPart = contentRange.substringAfterLast('/').trim()
            val parsedTotal = totalPart.toLongOrNull()
            if (parsedTotal != null && parsedTotal > 0L) {
                return parsedTotal
            }
        }
        if (code == 200 || !wasSmallRangeProbe) {
            val cLen = conn.getHeaderField("Content-Length")?.trim()?.toLongOrNull()
                ?: conn.contentLengthLong
            if (cLen > 100L) {
                return cLen
            }
        }
        return -1L
    }

    suspend fun probeVideoHeaders(
        videoUrl: String,
        pageUrl: String,
        userAgent: String
    ): VideoProbeResult = withContext(Dispatchers.IO) {
        if (BrowserUrlUtils.isProtectedOrStreamingUrl(videoUrl)) {
            return@withContext VideoProbeResult(
                isDirectMp4 = false,
                isStreamingOrProtected = true,
                error = "Streaming / protected protocol"
            )
        }

        val cookies = getMergedCookies(videoUrl, pageUrl)

        val profilesToTry = listOf(
            HeaderProfile.PAGE_REFERER_WITH_RANGE,
            HeaderProfile.VIDEO_ORIGIN_REFERER_WITH_RANGE,
            HeaderProfile.NO_REFERER_WITH_RANGE,
            HeaderProfile.PAGE_REFERER_NO_RANGE,
            HeaderProfile.NO_REFERER_NO_RANGE
        )

        for (profile in profilesToTry) {
            try {
                val isRangeProbe = profile == HeaderProfile.PAGE_REFERER_WITH_RANGE ||
                    profile == HeaderProfile.VIDEO_ORIGIN_REFERER_WITH_RANGE ||
                    profile == HeaderProfile.NO_REFERER_WITH_RANGE

                val probeResult = executeWithSslFallback(
                    videoUrl = videoUrl,
                    pageUrl = pageUrl,
                    userAgent = userAgent,
                    cookies = cookies,
                    profile = profile,
                    rangeHeaderValue = if (isRangeProbe) "bytes=0-1" else null
                ) { conn ->
                    val code = conn.responseCode
                    if (code == 200 || code == 206) {
                        val cType = conn.contentType?.substringBefore(';')?.trim()
                        if (cType != null && cType.equals("text/html", ignoreCase = true)) {
                            null
                        } else {
                            winningProfileCache[videoUrl] = profile
                            val totalBytes = extractTotalBytesFromConnection(
                                conn = conn,
                                code = code,
                                wasSmallRangeProbe = isRangeProbe && code == 206
                            )
                            val isStreaming = BrowserUrlUtils.isProtectedOrStreamingUrl(videoUrl, cType)
                            if (totalBytes > 0L || !isRangeProbe) {
                                VideoProbeResult(
                                    contentLength = totalBytes,
                                    contentType = cType?.ifBlank { "video/mp4" } ?: "video/mp4",
                                    isDirectMp4 = !isStreaming,
                                    isStreamingOrProtected = isStreaming
                                )
                            } else {
                                null
                            }
                        }
                    } else {
                        null
                    }
                }
                if (probeResult != null) return@withContext probeResult
            } catch (_: Exception) {
            }
        }

        val isHttp = videoUrl.startsWith("http://", ignoreCase = true) ||
            videoUrl.startsWith("https://", ignoreCase = true)
        val isStream = BrowserUrlUtils.isProtectedOrStreamingUrl(videoUrl, null)
        VideoProbeResult(
            contentLength = -1L,
            contentType = "video/mp4",
            isDirectMp4 = isHttp && !isStream,
            isStreamingOrProtected = isStream,
            error = null
        )
    }

    suspend fun enqueueDirectVideoDownload(
        videoUrl: String,
        websiteTitle: String,
        pageUrl: String,
        userAgent: String,
        estimatedBytes: Long = -1L
    ): Result<DownloadItemEntity> = withContext(Dispatchers.IO) {
        if (BrowserUrlUtils.isProtectedOrStreamingUrl(videoUrl)) {
            return@withContext Result.failure(
                IllegalArgumentException("This video is streaming / protected and cannot be downloaded")
            )
        }

        val fileName = buildVideoFileName(websiteTitle)
        val domain = BrowserUrlUtils.extractDomain(pageUrl.ifBlank { videoUrl })
        val cookies = getMergedCookies(videoUrl, pageUrl)
        val publicFile = getPublicVideoFile(fileName)

        val initialItem = DownloadItemEntity(
            systemDownloadId = -1L,
            fileName = fileName,
            url = videoUrl.trim(),
            pageTitle = websiteTitle.ifBlank { domain },
            domain = domain,
            mimeType = "video/mp4",
            filePath = publicFile.absolutePath,
            totalBytes = estimatedBytes,
            downloadedBytes = 0L,
            status = DownloadStatus.RUNNING.name,
            userAgent = userAgent,
            cookies = cookies,
            referer = pageUrl
        )
        val rowId = repository.insertDownload(initialItem)
        val savedItem = initialItem.copy(id = rowId)

        startHybridDownloadJob(savedItem)

        Result.success(savedItem)
    }

    fun pauseDownload(item: DownloadItemEntity) {
        scope.launch(Dispatchers.IO) {
            activeDownloadJobs.remove(item.id)?.cancel()
            if (item.systemDownloadId > 0L) {
                try {
                    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    dm.remove(item.systemDownloadId)
                } catch (_: Exception) {
                }
            }
            val targetFile = File(item.filePath)
            val savedBytes = if (targetFile.exists() && targetFile.length() > 0L) {
                targetFile.length()
            } else {
                item.downloadedBytes
            }
            repository.updateDownload(
                item.copy(
                    systemDownloadId = -1L,
                    downloadedBytes = savedBytes,
                    status = DownloadStatus.PAUSED.name,
                    errorReason = null
                )
            )
        }
    }

    fun resumeDownload(item: DownloadItemEntity) {
        scope.launch(Dispatchers.IO) {
            val restarted = item.copy(
                systemDownloadId = -1L,
                status = DownloadStatus.RUNNING.name,
                errorReason = null
            )
            repository.updateDownload(restarted)
            startHybridDownloadJob(restarted)
        }
    }

    private fun startHybridDownloadJob(item: DownloadItemEntity) {
        if (activeDownloadJobs.containsKey(item.id)) return
        val job = scope.launch(Dispatchers.IO) {
            try {
                val winningProfile = winningProfileCache[item.url]
                val preferReferer = when (winningProfile) {
                    HeaderProfile.NO_REFERER_WITH_RANGE,
                    HeaderProfile.NO_REFERER_NO_RANGE -> false
                    else -> true
                }
                val customReferer = when (winningProfile) {
                    HeaderProfile.VIDEO_ORIGIN_REFERER_WITH_RANGE -> extractOrigin(item.url)?.let { "$it/" }
                    HeaderProfile.NO_REFERER_WITH_RANGE,
                    HeaderProfile.NO_REFERER_NO_RANGE -> null
                    else -> item.referer?.takeIf { it.startsWith("http") }
                }

                // --- STAGE 1: ANDROID SYSTEM DOWNLOAD MANAGER ---
                val dmFirstAttempt = runSystemDownloadManagerAttempt(
                    item = item,
                    refererHeader = if (preferReferer) customReferer else null
                )
                if (dmFirstAttempt) {
                    return@launch
                }

                if (preferReferer && !customReferer.isNullOrBlank() && isActive) {
                    val dmSecondAttempt = runSystemDownloadManagerAttempt(
                        item = item,
                        refererHeader = null
                    )
                    if (dmSecondAttempt) {
                        return@launch
                    }
                }

                // --- STAGE 2: MULTI-PROFILE HTTP STREAMING ---
                if (isActive) {
                    val httpSuccess = runHttpStreamingAttempt(item)
                    if (httpSuccess) {
                        return@launch
                    }
                }

                // --- STAGE 3: MAIN-THREAD IN-WEBVIEW CHROMIUM STREAM BRIDGE ---
                if (isActive) {
                    val webViewSuccess = runWebViewStreamBridgeAttempt(item)
                    if (webViewSuccess) {
                        return@launch
                    }
                }

                if (isActive) {
                    val errText = "Video server rejected download connection. Try opening video in a new tab first."
                    repository.updateDownload(
                        item.copy(
                            status = DownloadStatus.FAILED.name,
                            errorReason = errText
                        )
                    )
                    postDownloadNotification(item.id.toInt(), item.fileName, 0, true, errText)
                }
            } finally {
                activeDownloadJobs.remove(item.id)
            }
        }
        activeDownloadJobs[item.id] = job
    }

    private suspend fun runSystemDownloadManagerAttempt(
        item: DownloadItemEntity,
        refererHeader: String?
    ): Boolean {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return false
        var sysId = -1L
        return try {
            val safeUrl = item.url.trim().replace(" ", "%20")
            val uri = Uri.parse(safeUrl)
            val scheme = uri.scheme?.lowercase(Locale.US)
            if (scheme != "http" && scheme != "https") return false

            try {
                val pubFile = getPublicVideoFile(item.fileName)
                if (pubFile.exists() && pubFile.length() == 0L) {
                    pubFile.delete()
                }
                val extFile = getWritableVideoFile(item.fileName)
                if (extFile.exists() && extFile.length() == 0L) {
                    extFile.delete()
                }
            } catch (_: Exception) {
            }

            val directCookie = getDirectCookieForUrl(item.url) ?: item.cookies
            val effectiveUa = item.userAgent.ifBlank { NovaWebViewPool.MOBILE_USER_AGENT }

            val request = DownloadManager.Request(uri).apply {
                setTitle(item.fileName)
                setDescription("Nova Browser Video • ${item.domain}")
                setMimeType("video/mp4")
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
                setVisibleInDownloadsUi(true)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                addRequestHeader("User-Agent", effectiveUa)
                addRequestHeader("Accept", "video/mp4,video/*;q=0.9,*/*;q=0.8")
                if (!directCookie.isNullOrBlank()) {
                    addRequestHeader("Cookie", directCookie)
                }
                if (!refererHeader.isNullOrBlank()) {
                    addRequestHeader("Referer", refererHeader)
                }
            }

            var targetFilePath = getPublicVideoFile(item.fileName).absolutePath
            try {
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "$SUBFOLDER_VIDEOS/${item.fileName}"
                )
            } catch (_: Exception) {
                val fallbackFile = getWritableVideoFile(item.fileName)
                targetFilePath = fallbackFile.absolutePath
                request.setDestinationInExternalFilesDir(
                    context,
                    Environment.DIRECTORY_DOWNLOADS,
                    "$SUBFOLDER_VIDEOS/${item.fileName}"
                )
            }

            sysId = dm.enqueue(request)
            if (sysId <= 0L) return false

            var currentItem = item.copy(
                systemDownloadId = sysId,
                filePath = targetFilePath,
                status = DownloadStatus.RUNNING.name,
                errorReason = null
            )
            repository.updateDownload(currentItem)

            val query = DownloadManager.Query().setFilterById(sysId)
            val startTimeMs = System.currentTimeMillis()
            var lastProgressChangeMs = startTimeMs
            var lastDownloadedBytes = 0L

            while (scope.isActive) {
                delay(350L)
                val cursor: Cursor = dm.query(query) ?: break
                var shouldBreak = false
                var succeeded = false

                cursor.use { c ->
                    if (!c.moveToFirst()) {
                        shouldBreak = true
                        return@use
                    }
                    val statusIdx = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val downIdx = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val totalIdx = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val localUriIdx = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

                    val status = if (statusIdx >= 0) c.getInt(statusIdx) else DownloadManager.STATUS_RUNNING
                    val downloaded = if (downIdx >= 0) c.getLong(downIdx).coerceAtLeast(0L) else 0L
                    val totalFromDm = if (totalIdx >= 0) c.getLong(totalIdx) else -1L
                    val bestTotal = if (totalFromDm > 0L) totalFromDm else currentItem.totalBytes

                    if (downloaded > lastDownloadedBytes) {
                        lastDownloadedBytes = downloaded
                        lastProgressChangeMs = System.currentTimeMillis()
                    }

                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            val localUriStr = if (localUriIdx >= 0) c.getString(localUriIdx) else null
                            val resolvedPath = resolveDownloadedFilePath(localUriStr, targetFilePath, item.fileName)
                            val finalBytes = when {
                                downloaded > 0L -> downloaded
                                bestTotal > 0L -> bestTotal
                                File(resolvedPath).exists() -> File(resolvedPath).length()
                                else -> currentItem.downloadedBytes
                            }
                            currentItem = currentItem.copy(
                                filePath = resolvedPath,
                                status = DownloadStatus.COMPLETED.name,
                                downloadedBytes = finalBytes,
                                totalBytes = if (bestTotal > 0L) bestTotal else finalBytes,
                                errorReason = null
                            )
                            repository.updateDownload(currentItem)
                            succeeded = true
                            shouldBreak = true
                        }

                        DownloadManager.STATUS_FAILED -> {
                            try {
                                dm.remove(sysId)
                            } catch (_: Exception) {
                            }
                            succeeded = false
                            shouldBreak = true
                        }

                        DownloadManager.STATUS_PENDING,
                        DownloadManager.STATUS_PAUSED,
                        DownloadManager.STATUS_RUNNING -> {
                            currentItem = currentItem.copy(
                                status = DownloadStatus.RUNNING.name,
                                downloadedBytes = downloaded,
                                totalBytes = bestTotal,
                                errorReason = null
                            )
                            repository.updateDownload(currentItem)

                            val now = System.currentTimeMillis()
                            if (downloaded == 0L && now - startTimeMs > 8000L) {
                                try {
                                    dm.remove(sysId)
                                } catch (_: Exception) {
                                }
                                succeeded = false
                                shouldBreak = true
                            } else if (downloaded > 0L && now - lastProgressChangeMs > 25000L) {
                                try {
                                    dm.remove(sysId)
                                } catch (_: Exception) {
                                }
                                succeeded = false
                                shouldBreak = true
                            }
                        }
                    }
                }

                if (shouldBreak) {
                    return succeeded
                }
            }
            false
        } catch (_: Exception) {
            if (sysId > 0L) {
                try {
                    dm.remove(sysId)
                } catch (_: Exception) {
                }
            }
            false
        }
    }

    private fun resolveDownloadedFilePath(
        localUriStr: String?,
        defaultPath: String,
        fileName: String
    ): String {
        if (!localUriStr.isNullOrBlank()) {
            try {
                val uri = Uri.parse(localUriStr)
                if (uri.scheme == "file" && !uri.path.isNullOrBlank()) {
                    val f = File(uri.path!!)
                    if (f.exists()) return f.absolutePath
                }
            } catch (_: Exception) {
            }
        }
        val pubFile = getPublicVideoFile(fileName)
        if (pubFile.exists() && pubFile.length() > 0L) return pubFile.absolutePath
        val extFile = getWritableVideoFile(fileName)
        if (extFile.exists() && extFile.length() > 0L) return extFile.absolutePath
        return defaultPath
    }

    private suspend fun runHttpStreamingAttempt(item: DownloadItemEntity): Boolean {
        val file = getWritableVideoFile(item.fileName)
        file.parentFile?.mkdirs()
        val freshCookies = getMergedCookies(item.url, item.referer.orEmpty()).takeIf { !it.isNullOrBlank() }
            ?: item.cookies

        val allProfiles = listOf(
            HeaderProfile.PAGE_REFERER_WITH_RANGE,
            HeaderProfile.VIDEO_ORIGIN_REFERER_WITH_RANGE,
            HeaderProfile.NO_REFERER_WITH_RANGE,
            HeaderProfile.PAGE_REFERER_NO_RANGE,
            HeaderProfile.NO_REFERER_NO_RANGE
        )
        val cachedWinner = winningProfileCache[item.url]
        val orderedProfiles = if (cachedWinner != null) {
            listOf(cachedWinner) + allProfiles.filter { it != cachedWinner }
        } else {
            allProfiles
        }

        for (profile in orderedProfiles) {
            if (!scope.isActive) return false
            try {
                val rangeHeader = when (profile) {
                    HeaderProfile.PAGE_REFERER_WITH_RANGE,
                    HeaderProfile.VIDEO_ORIGIN_REFERER_WITH_RANGE,
                    HeaderProfile.NO_REFERER_WITH_RANGE -> "bytes=0-"
                    else -> null
                }

                val completed = executeWithSslFallback(
                    videoUrl = item.url,
                    pageUrl = item.referer.orEmpty(),
                    userAgent = item.userAgent,
                    cookies = freshCookies,
                    profile = profile,
                    rangeHeaderValue = rangeHeader
                ) { conn ->
                    val code = conn.responseCode
                    if (code !in 200..299 && code != 206) {
                        return@executeWithSslFallback false
                    }
                    val responseMime = conn.contentType?.substringBefore(';')?.trim()
                    if (responseMime != null && responseMime.equals("text/html", ignoreCase = true)) {
                        return@executeWithSslFallback false
                    }
                    if (BrowserUrlUtils.isProtectedOrStreamingUrl(item.url, responseMime)) {
                        return@executeWithSslFallback false
                    }

                    val totalFromHeaders = extractTotalBytesFromConnection(
                        conn = conn,
                        code = code,
                        wasSmallRangeProbe = false
                    )
                    val contentLen = conn.contentLengthLong
                    val totalBytes = when {
                        totalFromHeaders > 0L -> totalFromHeaders
                        contentLen > 0L -> contentLen
                        else -> item.totalBytes
                    }

                    scope.launch(Dispatchers.IO) {
                        repository.updateDownload(
                            item.copy(
                                systemDownloadId = -1L,
                                filePath = file.absolutePath,
                                status = DownloadStatus.RUNNING.name,
                                downloadedBytes = 0L,
                                totalBytes = totalBytes,
                                errorReason = null
                            )
                        )
                    }

                    val raf = RandomAccessFile(file, "rw")
                    raf.setLength(0)
                    raf.seek(0)

                    var currentBytes = 0L
                    val buffer = ByteArray(64 * 1024)
                    var lastUpdateMs = 0L

                    conn.inputStream.use { input ->
                        raf.use { output ->
                            while (scope.isActive) {
                                val read = input.read(buffer)
                                if (read == -1) break
                                output.write(buffer, 0, read)
                                currentBytes += read
                                val now = System.currentTimeMillis()
                                if (now - lastUpdateMs > 240L) {
                                    lastUpdateMs = now
                                    val snapBytes = currentBytes
                                    scope.launch(Dispatchers.IO) {
                                        repository.updateDownload(
                                            item.copy(
                                                systemDownloadId = -1L,
                                                filePath = file.absolutePath,
                                                status = DownloadStatus.RUNNING.name,
                                                downloadedBytes = snapBytes,
                                                totalBytes = totalBytes,
                                                errorReason = null
                                            )
                                        )
                                    }
                                    val pct = if (totalBytes > 0L) {
                                        ((currentBytes * 100L) / totalBytes).toInt().coerceIn(1, 99)
                                    } else {
                                        50
                                    }
                                    postDownloadNotification(item.id.toInt(), item.fileName, pct, false, null)
                                }
                            }
                        }
                    }

                    if (scope.isActive && currentBytes > 0L) {
                        exportToPublicDownloads(file, item.fileName)
                        val pubFile = getPublicVideoFile(item.fileName)
                        val finalPath = if (pubFile.exists() && pubFile.length() > 0L) {
                            pubFile.absolutePath
                        } else {
                            file.absolutePath
                        }
                        val snapFinalBytes = currentBytes
                        scope.launch(Dispatchers.IO) {
                            repository.updateDownload(
                                item.copy(
                                    systemDownloadId = -1L,
                                    filePath = finalPath,
                                    status = DownloadStatus.COMPLETED.name,
                                    downloadedBytes = snapFinalBytes,
                                    totalBytes = if (totalBytes > 0L) totalBytes else snapFinalBytes,
                                    errorReason = null
                                )
                            )
                        }
                        postDownloadNotification(item.id.toInt(), item.fileName, 100, true, null)
                        true
                    } else {
                        false
                    }
                }

                if (completed) return true
            } catch (_: Exception) {
            }
        }
        return false
    }

    private suspend fun runWebViewStreamBridgeAttempt(item: DownloadItemEntity): Boolean {
        val trigger = webViewStreamTrigger ?: return false
        val file = getWritableVideoFile(item.fileName)
        file.parentFile?.mkdirs()
        if (file.exists()) file.delete()

        val deferred = CompletableDeferred<Boolean>()
        val fos = try {
            FileOutputStream(file, true)
        } catch (_: Exception) {
            return false
        }

        webViewStreamFiles[item.id] = fos
        webViewStreamItems[item.id] = item.copy(
            systemDownloadId = -1L,
            filePath = file.absolutePath
        )
        webViewStreamLastUpdate[item.id] = System.currentTimeMillis()
        webViewStreamCompletion[item.id] = deferred

        val launched = withContext(Dispatchers.Main) {
            try {
                trigger.invoke(item.id, item.url)
            } catch (_: Exception) {
                false
            }
        }

        if (!launched) {
            cleanupWebViewStream(item.id)
            return false
        }

        while (scope.isActive && !deferred.isCompleted) {
            val done = withTimeoutOrNull(2000L) { deferred.await() }
            if (done != null) return done
            val lastAct = webViewStreamLastUpdate[item.id] ?: 0L
            if (System.currentTimeMillis() - lastAct > 15000L) {
                cleanupWebViewStream(item.id)
                return false
            }
        }

        return if (deferred.isCompleted) deferred.await() else false
    }

    private fun cleanupWebViewStream(downloadId: Long) {
        val fos = webViewStreamFiles.remove(downloadId)
        val item = webViewStreamItems.remove(downloadId)
        webViewStreamLastUpdate.remove(downloadId)
        webViewStreamCompletion.remove(downloadId)?.complete(false)
        try {
            fos?.close()
        } catch (_: Exception) {
        }
        if (item != null) {
            val f = File(item.filePath)
            if (f.exists() && f.length() == 0L) {
                f.delete()
            }
        }
    }

    fun onWebViewDownloadChunk(downloadId: Long, base64Chunk: String, loadedBytes: Long, totalBytes: Long) {
        val fos = webViewStreamFiles[downloadId] ?: return
        val item = webViewStreamItems[downloadId] ?: return
        try {
            val decoded = Base64.decode(base64Chunk, Base64.DEFAULT)
            fos.write(decoded)
            val now = System.currentTimeMillis()
            val lastUpd = webViewStreamLastUpdate[downloadId] ?: 0L
            webViewStreamLastUpdate[downloadId] = now
            if (now - lastUpd > 240L) {
                val effectiveTotal = if (totalBytes > 0L) totalBytes else item.totalBytes
                scope.launch(Dispatchers.IO) {
                    repository.updateDownload(
                        item.copy(
                            status = DownloadStatus.RUNNING.name,
                            downloadedBytes = loadedBytes,
                            totalBytes = effectiveTotal,
                            errorReason = null
                        )
                    )
                    val pct = if (effectiveTotal > 0L) {
                        ((loadedBytes * 100L) / effectiveTotal).toInt().coerceIn(1, 99)
                    } else {
                        50
                    }
                    postDownloadNotification(item.id.toInt(), item.fileName, pct, false, null)
                }
            }
        } catch (_: Exception) {
        }
    }

    fun onWebViewDownloadFinished(downloadId: Long, totalLoadedBytes: Long) {
        val fos = webViewStreamFiles.remove(downloadId)
        val item = webViewStreamItems.remove(downloadId)
        webViewStreamLastUpdate.remove(downloadId)
        val deferred = webViewStreamCompletion.remove(downloadId)

        scope.launch(Dispatchers.IO) {
            try {
                fos?.flush()
                fos?.close()
            } catch (_: Exception) {
            }
            if (item == null) {
                deferred?.complete(false)
                return@launch
            }
            val file = File(item.filePath)
            val finalSize = if (file.exists() && file.length() > 0L) file.length() else totalLoadedBytes
            if (finalSize > 0L) {
                exportToPublicDownloads(file, item.fileName)
                val pubFile = getPublicVideoFile(item.fileName)
                val finalPath = if (pubFile.exists() && pubFile.length() > 0L) {
                    pubFile.absolutePath
                } else {
                    file.absolutePath
                }
                repository.updateDownload(
                    item.copy(
                        filePath = finalPath,
                        status = DownloadStatus.COMPLETED.name,
                        downloadedBytes = finalSize,
                        totalBytes = finalSize,
                        errorReason = null
                    )
                )
                postDownloadNotification(item.id.toInt(), item.fileName, 100, true, null)
                deferred?.complete(true)
            } else {
                if (file.exists()) file.delete()
                deferred?.complete(false)
            }
        }
    }

    fun onWebViewDownloadError(downloadId: Long, errorMessage: String) {
        cleanupWebViewStream(downloadId)
    }

    fun deleteDownload(item: DownloadItemEntity) {
        scope.launch(Dispatchers.IO) {
            activeDownloadJobs.remove(item.id)?.cancel()
            cleanupWebViewStream(item.id)
            if (item.systemDownloadId > 0L) {
                try {
                    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    dm.remove(item.systemDownloadId)
                } catch (_: Exception) {
                }
            }
            try {
                val f = File(item.filePath)
                if (f.exists()) f.delete()
                val pub = getPublicVideoFile(item.fileName)
                if (pub.exists()) pub.delete()
                val ext = getWritableVideoFile(item.fileName)
                if (ext.exists()) ext.delete()
            } catch (_: Exception) {
            }
            repository.deleteDownloadById(item.id)
        }
    }

    fun openDownloadedFile(item: DownloadItemEntity): Result<Unit> {
        return try {
            var contentUri: Uri? = null

            if (item.systemDownloadId > 0L) {
                try {
                    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    contentUri = dm.getUriForDownloadedFile(item.systemDownloadId)
                } catch (_: Exception) {
                }
            }

            if (contentUri == null) {
                val candidateFiles = listOf(
                    File(item.filePath),
                    getPublicVideoFile(item.fileName),
                    getWritableVideoFile(item.fileName)
                )
                val existingFile = candidateFiles.firstOrNull { it.exists() && it.length() > 0L }
                    ?: return Result.failure(IllegalStateException("Downloaded video file not found"))

                contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    existingFile
                )
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, item.mimeType.ifBlank { "video/mp4" })
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(intent, "Play ${item.fileName}").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun postDownloadNotification(
        notificationId: Int,
        fileName: String,
        progressPct: Int,
        isFinished: Boolean,
        errorReason: String?
    ) {
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(fileName)
                .setOnlyAlertOnce(true)

            if (!isFinished) {
                builder.setSmallIcon(android.R.drawable.stat_sys_download)
                    .setContentText("Downloading MP4 video… $progressPct%")
                    .setProgress(100, progressPct, false)
                    .setOngoing(true)
            } else if (errorReason != null) {
                builder.setSmallIcon(android.R.drawable.stat_notify_error)
                    .setContentText("Failed: $errorReason")
                    .setProgress(0, 0, false)
                    .setOngoing(false)
            } else {
                builder.setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentText("Saved to Download/NovaBrowser/Videos/$fileName")
                    .setProgress(0, 0, false)
                    .setOngoing(false)
            }
            notificationManager.notify(9000 + notificationId, builder.build())
        } catch (_: SecurityException) {
        }
    }
}
