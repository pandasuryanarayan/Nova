package com.example.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class BrowserDao(private val dbHelper: NovaSqliteHelper) {
    private val _bookmarks = MutableStateFlow<List<BookmarkEntity>>(emptyList())
    private val _history = MutableStateFlow<List<HistoryEntity>>(emptyList())
    private val _shortcuts = MutableStateFlow<List<QuickShortcutEntity>>(emptyList())
    private val _downloads = MutableStateFlow<List<DownloadItemEntity>>(emptyList())

    init {
        refreshBookmarks()
        refreshHistory()
        refreshShortcuts()
        refreshDownloads()
    }

    private fun refreshBookmarks() {
        val list = mutableListOf<BookmarkEntity>()
        dbHelper.readableDatabase.rawQuery(
            "SELECT id, title, url, domain, createdAt FROM bookmarks ORDER BY createdAt DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    BookmarkEntity(
                        id = c.getLong(0),
                        title = c.getString(1) ?: "",
                        url = c.getString(2) ?: "",
                        domain = c.getString(3) ?: "",
                        createdAt = c.getLong(4)
                    )
                )
            }
        }
        _bookmarks.value = list
    }

    private fun refreshHistory() {
        val list = mutableListOf<HistoryEntity>()
        dbHelper.readableDatabase.rawQuery(
            "SELECT id, title, url, domain, visitedAt FROM history ORDER BY visitedAt DESC LIMIT 300",
            null
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    HistoryEntity(
                        id = c.getLong(0),
                        title = c.getString(1) ?: "",
                        url = c.getString(2) ?: "",
                        domain = c.getString(3) ?: "",
                        visitedAt = c.getLong(4)
                    )
                )
            }
        }
        _history.value = list
    }

    private fun refreshShortcuts() {
        val list = mutableListOf<QuickShortcutEntity>()
        dbHelper.readableDatabase.rawQuery(
            "SELECT id, title, url, subtitle, badgeText, accentColorHex, sortOrder FROM shortcuts ORDER BY sortOrder ASC, id ASC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    QuickShortcutEntity(
                        id = c.getLong(0),
                        title = c.getString(1) ?: "",
                        url = c.getString(2) ?: "",
                        subtitle = c.getString(3) ?: "",
                        badgeText = c.getString(4) ?: "",
                        accentColorHex = c.getLong(5),
                        sortOrder = c.getInt(6)
                    )
                )
            }
        }
        _shortcuts.value = list
    }

    private fun refreshDownloads() {
        val list = mutableListOf<DownloadItemEntity>()
        dbHelper.readableDatabase.rawQuery(
            "SELECT id, systemDownloadId, fileName, url, pageTitle, domain, mimeType, filePath, totalBytes, downloadedBytes, status, errorReason, userAgent, cookies, referer, createdAt FROM downloads ORDER BY createdAt DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    DownloadItemEntity(
                        id = c.getLong(0),
                        systemDownloadId = c.getLong(1),
                        fileName = c.getString(2) ?: "",
                        url = c.getString(3) ?: "",
                        pageTitle = c.getString(4) ?: "",
                        domain = c.getString(5) ?: "",
                        mimeType = c.getString(6) ?: "video/mp4",
                        filePath = c.getString(7) ?: "",
                        totalBytes = c.getLong(8),
                        downloadedBytes = c.getLong(9),
                        status = c.getString(10) ?: DownloadStatus.PENDING.name,
                        errorReason = c.getString(11),
                        userAgent = c.getString(12) ?: "",
                        cookies = c.getString(13),
                        referer = c.getString(14),
                        createdAt = c.getLong(15)
                    )
                )
            }
        }
        _downloads.value = list
    }

    // Bookmarks
    fun getAllBookmarks(): Flow<List<BookmarkEntity>> = _bookmarks.asStateFlow()

    suspend fun insertBookmark(bookmark: BookmarkEntity): Long = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            if (bookmark.id > 0) put("id", bookmark.id)
            put("title", bookmark.title)
            put("url", bookmark.url)
            put("domain", bookmark.domain)
            put("createdAt", bookmark.createdAt)
        }
        val rowId = dbHelper.writableDatabase.insertWithOnConflict(
            "bookmarks", null, cv, SQLiteDatabase.CONFLICT_REPLACE
        )
        refreshBookmarks()
        rowId
    }

    suspend fun deleteBookmarkById(id: Long) = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.delete("bookmarks", "id = ?", arrayOf(id.toString()))
        refreshBookmarks()
    }

    suspend fun deleteBookmarkByUrl(url: String) = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.delete("bookmarks", "url = ?", arrayOf(url))
        refreshBookmarks()
    }

    // History
    fun getAllHistory(): Flow<List<HistoryEntity>> = _history.asStateFlow()

    suspend fun insertHistory(history: HistoryEntity): Long = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            if (history.id > 0) put("id", history.id)
            put("title", history.title)
            put("url", history.url)
            put("domain", history.domain)
            put("visitedAt", history.visitedAt)
        }
        val rowId = dbHelper.writableDatabase.insertWithOnConflict(
            "history", null, cv, SQLiteDatabase.CONFLICT_REPLACE
        )
        refreshHistory()
        rowId
    }

    suspend fun deleteHistoryById(id: Long) = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.delete("history", "id = ?", arrayOf(id.toString()))
        refreshHistory()
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.delete("history", null, null)
        refreshHistory()
    }

    // Quick Shortcuts
    fun getAllShortcuts(): Flow<List<QuickShortcutEntity>> = _shortcuts.asStateFlow()

    suspend fun getShortcutsCount(): Int = withContext(Dispatchers.IO) {
        dbHelper.readableDatabase.rawQuery("SELECT COUNT(*) FROM shortcuts", null).use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }
    }

    suspend fun insertShortcuts(shortcuts: List<QuickShortcutEntity>) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            shortcuts.forEach { s ->
                val cv = shortcutValues(s, includeId = s.id > 0)
                db.insertWithOnConflict("shortcuts", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        refreshShortcuts()
    }

    suspend fun insertShortcut(shortcut: QuickShortcutEntity): Long = withContext(Dispatchers.IO) {
        val rowId = dbHelper.writableDatabase.insertWithOnConflict(
            "shortcuts", null, shortcutValues(shortcut, includeId = shortcut.id > 0), SQLiteDatabase.CONFLICT_REPLACE
        )
        refreshShortcuts()
        rowId
    }

    suspend fun updateShortcut(shortcut: QuickShortcutEntity) = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.update(
            "shortcuts", shortcutValues(shortcut, includeId = false), "id = ?", arrayOf(shortcut.id.toString())
        )
        refreshShortcuts()
    }

    suspend fun deleteShortcutById(id: Long) = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.delete("shortcuts", "id = ?", arrayOf(id.toString()))
        refreshShortcuts()
    }

    private fun shortcutValues(s: QuickShortcutEntity, includeId: Boolean): ContentValues =
        ContentValues().apply {
            if (includeId) put("id", s.id)
            put("title", s.title)
            put("url", s.url)
            put("subtitle", s.subtitle)
            put("badgeText", s.badgeText)
            put("accentColorHex", s.accentColorHex)
            put("sortOrder", s.sortOrder)
        }

    // Downloads
    fun getAllDownloads(): Flow<List<DownloadItemEntity>> = _downloads.asStateFlow()

    suspend fun getDownloadById(id: Long): DownloadItemEntity? = withContext(Dispatchers.IO) {
        _downloads.value.find { it.id == id }
    }

    suspend fun getActiveDownloads(): List<DownloadItemEntity> = withContext(Dispatchers.IO) {
        _downloads.value.filter { it.status == "PENDING" || it.status == "RUNNING" }
    }

    suspend fun insertDownload(item: DownloadItemEntity): Long = withContext(Dispatchers.IO) {
        val rowId = dbHelper.writableDatabase.insertWithOnConflict(
            "downloads", null, downloadValues(item, includeId = item.id > 0), SQLiteDatabase.CONFLICT_REPLACE
        )
        refreshDownloads()
        rowId
    }

    suspend fun updateDownload(item: DownloadItemEntity) = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.update(
            "downloads", downloadValues(item, includeId = false), "id = ?", arrayOf(item.id.toString())
        )
        refreshDownloads()
    }

    suspend fun deleteDownloadById(id: Long) = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.delete("downloads", "id = ?", arrayOf(id.toString()))
        refreshDownloads()
    }

    private fun downloadValues(d: DownloadItemEntity, includeId: Boolean): ContentValues =
        ContentValues().apply {
            if (includeId) put("id", d.id)
            put("systemDownloadId", d.systemDownloadId)
            put("fileName", d.fileName)
            put("url", d.url)
            put("pageTitle", d.pageTitle)
            put("domain", d.domain)
            put("mimeType", d.mimeType)
            put("filePath", d.filePath)
            put("totalBytes", d.totalBytes)
            put("downloadedBytes", d.downloadedBytes)
            put("status", d.status)
            if (d.errorReason != null) put("errorReason", d.errorReason) else putNull("errorReason")
            put("userAgent", d.userAgent)
            if (d.cookies != null) put("cookies", d.cookies) else putNull("cookies")
            if (d.referer != null) put("referer", d.referer) else putNull("referer")
            put("createdAt", d.createdAt)
        }
}

class NovaSqliteHelper(context: Context) : SQLiteOpenHelper(context, "nova_browser.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS bookmarks (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, url TEXT NOT NULL, domain TEXT NOT NULL, createdAt INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS history (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, url TEXT NOT NULL, domain TEXT NOT NULL, visitedAt INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS shortcuts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, url TEXT NOT NULL, subtitle TEXT NOT NULL, badgeText TEXT NOT NULL, accentColorHex INTEGER NOT NULL, sortOrder INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS downloads (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, systemDownloadId INTEGER NOT NULL, fileName TEXT NOT NULL, url TEXT NOT NULL, pageTitle TEXT NOT NULL, domain TEXT NOT NULL, mimeType TEXT NOT NULL, filePath TEXT NOT NULL, totalBytes INTEGER NOT NULL, downloadedBytes INTEGER NOT NULL, status TEXT NOT NULL, errorReason TEXT, userAgent TEXT NOT NULL, cookies TEXT, referer TEXT, createdAt INTEGER NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }
}

class NovaDatabase private constructor(context: Context) {
    private val dao = BrowserDao(NovaSqliteHelper(context.applicationContext))

    fun browserDao(): BrowserDao = dao

    companion object {
        @Volatile
        private var INSTANCE: NovaDatabase? = null

        fun getInstance(context: Context): NovaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NovaDatabase(context).also { INSTANCE = it }
            }
        }

        fun defaultShortcuts(): List<QuickShortcutEntity> = listOf(
            QuickShortcutEntity(title = "Google", url = "https://www.google.com", subtitle = "Search Engine", badgeText = "G", accentColorHex = 0xFF4285F4, sortOrder = 1),
            QuickShortcutEntity(title = "YouTube", url = "https://m.youtube.com", subtitle = "Videos & Creators", badgeText = "YT", accentColorHex = 0xFFFF5252, sortOrder = 2),
            QuickShortcutEntity(title = "DuckDuckGo", url = "https://duckduckgo.com", subtitle = "Private Search", badgeText = "DDG", accentColorHex = 0xFFFF7043, sortOrder = 3),
            QuickShortcutEntity(title = "Wikipedia", url = "https://www.wikipedia.org", subtitle = "Free Encyclopedia", badgeText = "W", accentColorHex = 0xFF90A4AE, sortOrder = 4),
            QuickShortcutEntity(title = "GitHub", url = "https://github.com", subtitle = "Code & Releases", badgeText = "GH", accentColorHex = 0xFF00E5FF, sortOrder = 5),
            QuickShortcutEntity(title = "Reddit", url = "https://www.reddit.com", subtitle = "Communities", badgeText = "RD", accentColorHex = 0xFFFFB300, sortOrder = 6),
            QuickShortcutEntity(title = "Archive.org", url = "https://archive.org", subtitle = "Digital Library", badgeText = "IA", accentColorHex = 0xFF00E676, sortOrder = 7),
            QuickShortcutEntity(title = "StackOverflow", url = "https://stackoverflow.com", subtitle = "Developer Q&A", badgeText = "SO", accentColorHex = 0xFF8C62FF, sortOrder = 8)
        )
    }
}
