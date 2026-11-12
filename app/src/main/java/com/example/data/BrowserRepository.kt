package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.browserDataStore by preferencesDataStore(name = "nova_browser_prefs")

data class BrowserPreferences(
    val isDarkMode: Boolean = true,
    val isAdBlockEnabled: Boolean = true,
    val defaultTextZoom: Int = 100,
    val totalAdsBlocked: Long = 0L
)

class BrowserRepository(
    private val context: Context,
    private val dao: BrowserDao
) {
    private object PrefKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val AD_BLOCK_ENABLED = booleanPreferencesKey("ad_block_enabled")
        val DEFAULT_TEXT_ZOOM = intPreferencesKey("default_text_zoom")
        val TOTAL_ADS_BLOCKED = longPreferencesKey("total_ads_blocked")
        val DEFAULT_SHORTCUTS_SEEDED = booleanPreferencesKey("default_shortcuts_seeded")
    }

    val preferencesFlow: Flow<BrowserPreferences> = context.browserDataStore.data.map { prefs ->
        BrowserPreferences(
            isDarkMode = prefs[PrefKeys.DARK_MODE] ?: true,
            isAdBlockEnabled = prefs[PrefKeys.AD_BLOCK_ENABLED] ?: true,
            defaultTextZoom = prefs[PrefKeys.DEFAULT_TEXT_ZOOM] ?: 100,
            totalAdsBlocked = prefs[PrefKeys.TOTAL_ADS_BLOCKED] ?: 0L
        )
    }

    val bookmarksFlow: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()
    val historyFlow: Flow<List<HistoryEntity>> = dao.getAllHistory()
    val shortcutsFlow: Flow<List<QuickShortcutEntity>> = dao.getAllShortcuts()
    val downloadsFlow: Flow<List<DownloadItemEntity>> = dao.getAllDownloads()

    suspend fun ensureDefaultShortcutsSeeded() {
        val prefs = context.browserDataStore.data.first()
        val alreadySeeded = prefs[PrefKeys.DEFAULT_SHORTCUTS_SEEDED] ?: false
        if (!alreadySeeded) {
            if (dao.getShortcutsCount() == 0) {
                dao.insertShortcuts(NovaDatabase.defaultShortcuts())
            }
            context.browserDataStore.edit { it[PrefKeys.DEFAULT_SHORTCUTS_SEEDED] = true }
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.browserDataStore.edit { it[PrefKeys.DARK_MODE] = enabled }
    }

    suspend fun setAdBlockEnabled(enabled: Boolean) {
        context.browserDataStore.edit { it[PrefKeys.AD_BLOCK_ENABLED] = enabled }
    }

    suspend fun setDefaultTextZoom(zoom: Int) {
        context.browserDataStore.edit { it[PrefKeys.DEFAULT_TEXT_ZOOM] = zoom.coerceIn(50, 200) }
    }

    suspend fun incrementAdsBlocked() {
        context.browserDataStore.edit { prefs ->
            val current = prefs[PrefKeys.TOTAL_ADS_BLOCKED] ?: 0L
            prefs[PrefKeys.TOTAL_ADS_BLOCKED] = current + 1L
        }
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
