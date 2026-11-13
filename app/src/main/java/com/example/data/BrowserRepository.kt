package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BrowserPreferences(
    val isDarkMode: Boolean = false,
    val isAdBlockEnabled: Boolean = true,
    val adBlockMode: AdBlockMode = AdBlockMode.MINIMAL,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val defaultTextZoom: Int = 100,
    val totalAdsBlocked: Long = 0L
)

class BrowserRepository(
    context: Context,
    private val dao: BrowserDao
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nova_browser_prefs", Context.MODE_PRIVATE)

    private object PrefKeys {
        const val DARK_MODE = "dark_mode"
        const val AD_BLOCK_ENABLED = "ad_block_enabled"
        const val AD_BLOCK_MODE = "ad_block_mode"
        const val APP_LANGUAGE = "app_language"
        const val DEFAULT_TEXT_ZOOM = "default_text_zoom"
        const val TOTAL_ADS_BLOCKED = "total_ads_blocked"
        const val DEFAULT_SHORTCUTS_SEEDED = "default_shortcuts_seeded"
    }

    private fun readPreferences(): BrowserPreferences = BrowserPreferences(
        isDarkMode = prefs.getBoolean(PrefKeys.DARK_MODE, false),
        isAdBlockEnabled = prefs.getBoolean(PrefKeys.AD_BLOCK_ENABLED, true),
        adBlockMode = AdBlockMode.fromName(prefs.getString(PrefKeys.AD_BLOCK_MODE, null)),
        appLanguage = AppLanguage.fromCode(prefs.getString(PrefKeys.APP_LANGUAGE, null)),
        defaultTextZoom = prefs.getInt(PrefKeys.DEFAULT_TEXT_ZOOM, 100),
        totalAdsBlocked = prefs.getLong(PrefKeys.TOTAL_ADS_BLOCKED, 0L)
    )

    private val _preferencesFlow = MutableStateFlow(readPreferences())
    val preferencesFlow: Flow<BrowserPreferences> = _preferencesFlow.asStateFlow()

    val bookmarksFlow: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()
    val historyFlow: Flow<List<HistoryEntity>> = dao.getAllHistory()
    val shortcutsFlow: Flow<List<QuickShortcutEntity>> = dao.getAllShortcuts()
    val downloadsFlow: Flow<List<DownloadItemEntity>> = dao.getAllDownloads()

    suspend fun ensureDefaultShortcutsSeeded() {
        val alreadySeeded = prefs.getBoolean(PrefKeys.DEFAULT_SHORTCUTS_SEEDED, false)
        if (!alreadySeeded) {
            if (dao.getShortcutsCount() == 0) {
                dao.insertShortcuts(NovaDatabase.defaultShortcuts())
            }
            prefs.edit().putBoolean(PrefKeys.DEFAULT_SHORTCUTS_SEEDED, true).apply()
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(PrefKeys.DARK_MODE, enabled).apply()
        _preferencesFlow.value = readPreferences()
    }

    suspend fun setAdBlockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PrefKeys.AD_BLOCK_ENABLED, enabled).apply()
        _preferencesFlow.value = readPreferences()
    }

    suspend fun setAdBlockMode(mode: AdBlockMode) {
        prefs.edit()
            .putString(PrefKeys.AD_BLOCK_MODE, mode.name)
            .putBoolean(PrefKeys.AD_BLOCK_ENABLED, true)
            .apply()
        _preferencesFlow.value = readPreferences()
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        prefs.edit().putString(PrefKeys.APP_LANGUAGE, language.code).apply()
        _preferencesFlow.value = readPreferences()
    }

    suspend fun setDefaultTextZoom(zoom: Int) {
        prefs.edit().putInt(PrefKeys.DEFAULT_TEXT_ZOOM, zoom.coerceIn(50, 200)).apply()
        _preferencesFlow.value = readPreferences()
    }

    suspend fun incrementAdsBlocked() {
        val next = prefs.getLong(PrefKeys.TOTAL_ADS_BLOCKED, 0L) + 1L
        prefs.edit().putLong(PrefKeys.TOTAL_ADS_BLOCKED, next).apply()
        _preferencesFlow.value = readPreferences()
    }

    suspend fun addBookmark(title: String, url: String, domain: String) {
        dao.insertBookmark(BookmarkEntity(title = title, url = url, domain = domain))
    }

    suspend fun removeBookmarkById(id: Long) {
        dao.deleteBookmarkById(id)
    }

    suspend fun removeBookmarkByUrl(url: String) {
        dao.deleteBookmarkByUrl(url)
    }

    suspend fun recordHistory(title: String, url: String, domain: String) {
        if (url.startsWith("nova://") || url.isBlank() || url == "about:blank") return
        dao.insertHistory(HistoryEntity(title = title.ifBlank { domain }, url = url, domain = domain))
    }

    suspend fun deleteHistoryItem(id: Long) {
        dao.deleteHistoryById(id)
    }

    suspend fun clearAllHistory() {
        dao.clearAllHistory()
    }

    suspend fun addShortcut(title: String, url: String, subtitle: String, badgeText: String, accentColorHex: Long) {
        dao.insertShortcut(
            QuickShortcutEntity(
                title = title,
                url = url,
                subtitle = subtitle,
                badgeText = badgeText,
                accentColorHex = accentColorHex,
                sortOrder = 99
            )
        )
    }

    suspend fun updateShortcut(shortcut: QuickShortcutEntity) {
        dao.updateShortcut(shortcut)
    }

    suspend fun deleteShortcut(id: Long) {
        dao.deleteShortcutById(id)
    }

    suspend fun insertDownload(item: DownloadItemEntity): Long = dao.insertDownload(item)

    suspend fun updateDownload(item: DownloadItemEntity) = dao.updateDownload(item)

    suspend fun getDownloadById(id: Long): DownloadItemEntity? = dao.getDownloadById(id)

    suspend fun getActiveDownloads(): List<DownloadItemEntity> = dao.getActiveDownloads()

    suspend fun deleteDownloadById(id: Long) = dao.deleteDownloadById(id)
}
