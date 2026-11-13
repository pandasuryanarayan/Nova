package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

const val NOVA_HOME_URL = "nova://home"
const val NOVA_VIDEO_LAB_URL = "nova://video-lab"

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val domain: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val domain: String,
    val visitedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shortcuts")
data class QuickShortcutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val subtitle: String,
    val badgeText: String,
    val accentColorHex: Long,
    val sortOrder: Int = 0
)

enum class DownloadStatus {
    PENDING,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED
}

@Entity(tableName = "downloads")
data class DownloadItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val systemDownloadId: Long = -1L,
    val fileName: String,
    val url: String,
    val pageTitle: String,
    val domain: String,
    val mimeType: String = "video/mp4",
    val filePath: String,
    val totalBytes: Long = -1L,
    val downloadedBytes: Long = 0L,
    val status: String = DownloadStatus.PENDING.name,
    val errorReason: String? = null,
    val userAgent: String = "",
    val cookies: String? = null,
    val referer: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val statusEnum: DownloadStatus
        get() = try {
            DownloadStatus.valueOf(status)
        } catch (_: Exception) {
            DownloadStatus.PENDING
        }

    val progressFraction: Float
        get() = if (totalBytes > 0L) {
            (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
}

enum class DownloadFilterTab {
    ONGOING,
    SUCCESS,
    FAILURE
}

enum class AppLanguage(
    val code: String,
    val labelEn: String,
    val labelHi: String,
    val subLabel: String
) {
    SYSTEM(
        code = "system",
        labelEn = "System Language",
        labelHi = "सिस्टम भाषा",
        subLabel = "Auto"
    ),
    ENGLISH(
        code = "en",
        labelEn = "English",
        labelHi = "English",
        subLabel = "EN"
    ),
    HINDI(
        code = "hi",
        labelEn = "Hindi",
        labelHi = "हिन्दी",
        subLabel = "हिन्दी"
    );

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.find {
                it.code.equals(code, ignoreCase = true) || it.name.equals(code, ignoreCase = true)
            } ?: SYSTEM
        }
    }
}

enum class AdBlockMode(
    val displayName: String,
    val shortBadge: String,
    val subtitle: String
) {
    MINIMAL(
        displayName = "Minimal (Default)",
        shortBadge = "Minimal",
        subtitle = "Essential 58 rules • Fast & lightweight ad shield"
    ),
    PRO(
        displayName = "Pro Mode",
        shortBadge = "Pro",
        subtitle = "Expanded +131 rules • Deep ad networks, analytics & social pixels"
    ),
    MAX(
        displayName = "Max Mode",
        shortBadge = "Max",
        subtitle = "All Pro rules + YouTube video, midroll & overlay ad blocker"
    );

    companion object {
        fun fromName(name: String?): AdBlockMode {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: MINIMAL
        }
    }
}

data class BlockedAdEntry(
    val id: String = UUID.randomUUID().toString(),
    val tabId: String,
    val adUrl: String,
    val adDomain: String,
    val pageDomain: String,
    val category: String, // e.g. "Ad Script", "Tracker / Pixel", "Banner Frame", "DOM Ad Slot"
    val filterRule: String, // e.g. "||doubleclick.net^"
    val modeTier: String = "MINIMAL",
    val timestamp: Long = System.currentTimeMillis()
)

data class SslCertificateDetails(
    val isSecure: Boolean = true,
    val summary: String = "Verified Secure Connection",
    val issuedToCommonName: String = "",
    val issuedToOrganization: String = "",
    val issuedToOrganizationalUnit: String = "",
    val issuedByCommonName: String = "",
    val issuedByOrganization: String = "",
    val issuedByOrganizationalUnit: String = "",
    val validFrom: String = "",
    val validUntil: String = "",
    val sha256Fingerprint: String = "",
    val serialNumber: String = "",
    val protocol: String = "TLS 1.3 / HTTPS"
)

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val url: String = NOVA_HOME_URL,
    val title: String = "Nova Home",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isSecureSsl: Boolean = true,
    val sslSummary: String = "Verified Secure Connection",
    val isIncognito: Boolean = false,
    val isDesktopMode: Boolean = false,
    val textZoom: Int = 100,
    val adsBlockedInTab: Int = 0
) {
    val isHomePage: Boolean
        get() = url == NOVA_HOME_URL
}

data class DetectedVideoQuality(
    val url: String,
    val label: String,
    val mimeType: String = "video/mp4",
    val isDirectMp4: Boolean = true,
    val isStreaming: Boolean = false,
    val estimatedBytes: Long = -1L,
    val probedContentType: String? = null,
    val isProbing: Boolean = true,
    val probeError: String? = null
)

data class DetectedVideoInfo(
    val pageTitle: String,
    val pageUrl: String,
    val domain: String,
    val primaryUrl: String,
    val posterUrl: String = "",
    val qualities: List<DetectedVideoQuality> = emptyList(),
    val selectedQualityIndex: Int = 0,
    val isIframe: Boolean = false,
    val dimensions: String = "",
    val durationSeconds: Int = 0
) {
    val selectedQuality: DetectedVideoQuality?
        get() = qualities.getOrNull(selectedQualityIndex) ?: qualities.firstOrNull()
}

data class BackgroundMediaState(
    val isActive: Boolean = false,
    val isPlaying: Boolean = false,
    val title: String = "",
    val url: String = "",
    val domain: String = ""
)

// --- Desktop-Style DevTools Inspector Models (Elements, Sources, Console, Network ONLY) ---

enum class DevToolsTab(val title: String) {
    ELEMENTS("Elements"),
    SOURCES("Sources"),
    CONSOLE("Console"),
    NETWORK("Network")
}

data class DomTreeNodeItem(
    val nodeId: String,
    val depth: Int,
    val tagName: String,
    val selector: String,
    val childCount: Int,
    val textPreview: String
)

data class DomSelectedElementDetail(
    val nodeId: String,
    val tagName: String,
    val selector: String,
    val breadcrumb: String,
    val id: String,
    val className: String,
    val inlineStyle: String,
    val innerText: String,
    val outerHtml: String,
    val widthPx: Int,
    val heightPx: Int,
    val topPx: Int,
    val leftPx: Int,
    val attributes: List<Pair<String, String>>,
    val computedStyles: List<Pair<String, String>>
)

data class DevToolsSourceFile(
    val id: String,
    val name: String,
    val url: String,
    val type: String, // HTML, CSS, JS
    val sizeBytes: Int,
    val content: String
)

data class DevToolsConsoleEntry(
    val id: String = UUID.randomUUID().toString(),
    val level: String, // LOG, INFO, WARN, ERROR, RESULT
    val message: String,
    val source: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class DevToolsNetworkEntry(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val method: String,
    val status: Int,
    val initiatorType: String,
    val mimeType: String,
    val sizeBytes: Long,
    val durationMs: Long,
    val responsePreview: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class DevToolsState(
    val isVisible: Boolean = false,
    val selectedTab: DevToolsTab = DevToolsTab.ELEMENTS,
    val isInspectElementPickerActive: Boolean = false,
    val domTree: List<DomTreeNodeItem> = emptyList(),
    val selectedElement: DomSelectedElementDetail? = null,
    val sources: List<DevToolsSourceFile> = emptyList(),
    val selectedSourceId: String = "",
    val consoleLogs: List<DevToolsConsoleEntry> = emptyList(),
    val networkEntries: List<DevToolsNetworkEntry> = emptyList()
)

enum class ActiveSheet {
    NONE,
    TABS,
    BOOKMARKS,
    HISTORY,
    DOWNLOADS,
    MENU,
    BLOCKED_ADS,
    VIDEO_LONG_PRESS,
    CLEAR_DATA_DIALOG,
    ADD_SHORTCUT_DIALOG,
    SSL_INFO_DIALOG,
    QR_SCANNER
}
