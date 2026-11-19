package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

data class BrowserPreferences(
    val isDarkMode: Boolean = false,
    val isAdBlockEnabled: Boolean = true,
    val adBlockMode: AdBlockMode = AdBlockMode.MINIMAL,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val defaultTextZoom: Int = 100,
    val totalAdsBlocked: Long = 0L,
    val isPasswordManagerEnabled: Boolean = true
)

class BrowserRepository(
    context: Context,
    private val dao: BrowserDao
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("netra_browser_prefs", Context.MODE_PRIVATE)

    private object PrefKeys {
        const val DARK_MODE = "dark_mode"
        const val AD_BLOCK_ENABLED = "ad_block_enabled"
        const val AD_BLOCK_MODE = "ad_block_mode"
        const val APP_LANGUAGE = "app_language"
        const val DEFAULT_TEXT_ZOOM = "default_text_zoom"
        const val TOTAL_ADS_BLOCKED = "total_ads_blocked"
        const val DEFAULT_SHORTCUTS_SEEDED = "default_shortcuts_seeded"
        const val SITE_CONFIGS_JSON = "site_configs_json"
        const val PASSWORD_MANAGER_ENABLED = "password_manager_enabled"
    }

    private fun normalizeDomainKey(domain: String): String =
        domain.trim().lowercase().removePrefix("www.")

    private fun readPreferences(): BrowserPreferences = BrowserPreferences(
        isDarkMode = prefs.getBoolean(PrefKeys.DARK_MODE, false),
        isAdBlockEnabled = prefs.getBoolean(PrefKeys.AD_BLOCK_ENABLED, true),
        adBlockMode = AdBlockMode.fromName(prefs.getString(PrefKeys.AD_BLOCK_MODE, null)),
        appLanguage = AppLanguage.fromCode(prefs.getString(PrefKeys.APP_LANGUAGE, null)),
        defaultTextZoom = prefs.getInt(PrefKeys.DEFAULT_TEXT_ZOOM, 100),
        totalAdsBlocked = prefs.getLong(PrefKeys.TOTAL_ADS_BLOCKED, 0L),
        isPasswordManagerEnabled = prefs.getBoolean(PrefKeys.PASSWORD_MANAGER_ENABLED, true)
    )

    private fun readSiteConfigs(): Map<String, SiteConfigSettings> {
        val raw = prefs.getString(PrefKeys.SITE_CONFIGS_JSON, null) ?: return emptyMap()
        return try {
            val root = JSONObject(raw)
            val map = LinkedHashMap<String, SiteConfigSettings>()
            val keys = root.keys()
            while (keys.hasNext()) {
                val domainKey = normalizeDomainKey(keys.next())
                val o = root.optJSONObject(domainKey) ?: root.optJSONObject("www.$domainKey") ?: continue
                if (domainKey.isBlank()) continue
                map[domainKey] = SiteConfigSettings(
                    domain = domainKey,
                    isAdBlockEnabled = o.optBoolean("adBlock", true),
                    javaScriptEnabled = o.optBoolean("js", true),
                    cookiePolicy = SiteCookiePolicy.fromName(o.optString("cookies", null)),
                    popupsAllowed = o.optBoolean("popups", false),
                    imagesEnabled = o.optBoolean("images", true),
                    soundEnabled = o.optBoolean("sound", true),
                    autoplayEnabled = o.optBoolean("autoplay", true),
                    domStorageEnabled = o.optBoolean("domStorage", true),
                    locationPermission = SitePermissionMode.fromName(o.optString("location", null)),
                    cameraPermission = SitePermissionMode.fromName(o.optString("camera", null)),
                    microphonePermission = SitePermissionMode.fromName(o.optString("mic", null)),
                    protectedContentAllowed = o.optBoolean("drm", true),
                    insecureContentAllowed = o.optBoolean("mixed", true),
                    safeBrowsingEnabled = o.optBoolean("safeBrowsing", true)
                )
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun writeSiteConfigs(map: Map<String, SiteConfigSettings>) {
        val root = JSONObject()
        for ((key, cfg) in map) {
            if (cfg.isDefault) continue
            val o = JSONObject().apply {
                put("adBlock", cfg.isAdBlockEnabled)
                put("js", cfg.javaScriptEnabled)
                put("cookies", cfg.cookiePolicy.name)
                put("popups", cfg.popupsAllowed)
                put("images", cfg.imagesEnabled)
                put("sound", cfg.soundEnabled)
                put("autoplay", cfg.autoplayEnabled)
                put("domStorage", cfg.domStorageEnabled)
                put("location", cfg.locationPermission.name)
                put("camera", cfg.cameraPermission.name)
                put("mic", cfg.microphonePermission.name)
                put("drm", cfg.protectedContentAllowed)
                put("mixed", cfg.insecureContentAllowed)
                put("safeBrowsing", cfg.safeBrowsingEnabled)
            }
            root.put(normalizeDomainKey(key), o)
        }
        prefs.edit().putString(PrefKeys.SITE_CONFIGS_JSON, root.toString()).apply()
    }

    private val _preferencesFlow = MutableStateFlow(readPreferences())
    val preferencesFlow: Flow<BrowserPreferences> = _preferencesFlow.asStateFlow()

    private val _siteConfigsFlow = MutableStateFlow(readSiteConfigs())
    val siteConfigsFlow: Flow<Map<String, SiteConfigSettings>> = _siteConfigsFlow.asStateFlow()

    fun getSiteConfig(domain: String): SiteConfigSettings {
        val key = normalizeDomainKey(domain)
        return _siteConfigsFlow.value[key] ?: SiteConfigSettings(domain = key)
    }

    fun saveSiteConfig(config: SiteConfigSettings) {
        val key = normalizeDomainKey(config.domain)
        if (key.isBlank()) return
        val normalized = config.copy(domain = key)
        val next = _siteConfigsFlow.value.toMutableMap()
        if (normalized.isDefault) {
            next.remove(key)
        } else {
            next[key] = normalized
        }
        writeSiteConfigs(next)
        _siteConfigsFlow.value = next
    }

    fun resetSiteConfig(domain: String) {
        val key = normalizeDomainKey(domain)
        if (key.isBlank()) return
        val next = _siteConfigsFlow.value.toMutableMap()
        next.remove(key)
        writeSiteConfigs(next)
        _siteConfigsFlow.value = next
    }

    val bookmarksFlow: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()
    val historyFlow: Flow<List<HistoryEntity>> = dao.getAllHistory()
    val shortcutsFlow: Flow<List<QuickShortcutEntity>> = dao.getAllShortcuts()
    val downloadsFlow: Flow<List<DownloadItemEntity>> = dao.getAllDownloads()
    val passwordsFlow: Flow<List<SavedPasswordEntity>> = dao.getAllPasswords()

    suspend fun ensureDefaultShortcutsSeeded() {
        val alreadySeeded = prefs.getBoolean(PrefKeys.DEFAULT_SHORTCUTS_SEEDED, false)
        if (!alreadySeeded) {
            if (dao.getShortcutsCount() == 0) {
                dao.insertShortcuts(NetraDatabase.defaultShortcuts())
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
        if (url.startsWith("netra://") || url.isBlank() || url == "about:blank") return
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

    suspend fun setPasswordManagerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PrefKeys.PASSWORD_MANAGER_ENABLED, enabled).apply()
        _preferencesFlow.value = readPreferences()
    }

    suspend fun savePassword(
        id: Long = 0L,
        siteName: String,
        domain: String,
        url: String,
        username: String,
        password: String
    ): Long = dao.upsertPassword(
        SavedPasswordEntity(
            id = id,
            siteName = siteName,
            domain = domain,
            url = url,
            username = username,
            password = password,
            updatedAt = System.currentTimeMillis()
        )
    )

    suspend fun deletePassword(id: Long) = dao.deletePasswordById(id)

    suspend fun clearAllPasswords() = dao.clearAllPasswords()
}
