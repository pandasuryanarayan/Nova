package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowserDao {
    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteBookmarkByUrl(url: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url)")
    fun isBookmarkedFlow(url: String): Flow<Boolean>

    // History
    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT 300")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity): Long

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM history")
    suspend fun clearAllHistory()

    // Quick Shortcuts
    @Query("SELECT * FROM shortcuts ORDER BY sortOrder ASC, id ASC")
    fun getAllShortcuts(): Flow<List<QuickShortcutEntity>>

    @Query("SELECT COUNT(*) FROM shortcuts")
    suspend fun getShortcutsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcuts(shortcuts: List<QuickShortcutEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: QuickShortcutEntity): Long

    @Query("DELETE FROM shortcuts WHERE id = :id")
    suspend fun deleteShortcutById(id: Long)

    // Downloads
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadItemEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownloadById(id: Long): DownloadItemEntity?

    @Query("SELECT * FROM downloads WHERE status IN ('PENDING', 'RUNNING') ORDER BY createdAt DESC")
    suspend fun getActiveDownloads(): List<DownloadItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(item: DownloadItemEntity): Long

    @Update
    suspend fun updateDownload(item: DownloadItemEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: Long)

    @Query("DELETE FROM downloads")
    suspend fun clearAllDownloads()
}

@Database(
    entities = [
        BookmarkEntity::class,
        HistoryEntity::class,
        QuickShortcutEntity::class,
        DownloadItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NovaDatabase : RoomDatabase() {
    abstract fun browserDao(): BrowserDao

    companion object {
        @Volatile
        private var INSTANCE: NovaDatabase? = null

        fun getInstance(context: Context): NovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NovaDatabase::class.java,
                    "nova_browser.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }

        fun defaultShortcuts(): List<QuickShortcutEntity> = listOf(
            QuickShortcutEntity(
                title = "Video Test Lab",
                url = NOVA_VIDEO_LAB_URL,
                subtitle = "Multi-Quality MP4 & Stream Test",
                badgeText = "MP4 LAB",
                accentColorHex = 0xFF00E5FF,
                sortOrder = 1
            ),
            QuickShortcutEntity(
                title = "W3Schools Video",
                url = "https://www.w3schools.com/html/html5_video.asp",
                subtitle = "HTML5 <video> Test Page",
                badgeText = "W3C",
                accentColorHex = 0xFF00E676,
                sortOrder = 2
            ),
            QuickShortcutEntity(
                title = "Sample Videos",
                url = "https://samplelib.com/sample-mp4.html",
                subtitle = "Direct MP4 Video Samples",
                badgeText = "MP4",
                accentColorHex = 0xFF8C62FF,
                sortOrder = 3
            ),
            QuickShortcutEntity(
                title = "Wikimedia Video",
                url = "https://commons.wikimedia.org/wiki/Category:Videos",
                subtitle = "Open Media Repository",
                badgeText = "WIKI",
                accentColorHex = 0xFFFFB300,
                sortOrder = 4
            ),
            QuickShortcutEntity(
                title = "Google Search",
                url = "https://www.google.com",
                subtitle = "Web & Video Search",
                badgeText = "G",
                accentColorHex = 0xFF4285F4,
                sortOrder = 5
            ),
            QuickShortcutEntity(
                title = "DuckDuckGo",
                url = "https://duckduckgo.com",
                subtitle = "Private Web Search",
                badgeText = "DDG",
                accentColorHex = 0xFFFF7043,
                sortOrder = 6
            ),
            QuickShortcutEntity(
                title = "Wikipedia",
                url = "https://www.wikipedia.org",
                subtitle = "Free Encyclopedia",
                badgeText = "W",
                accentColorHex = 0xFF90A4AE,
                sortOrder = 7
            ),
            QuickShortcutEntity(
                title = "Internet Archive",
                url = "https://archive.org/details/movies",
                subtitle = "Public Domain Films",
                badgeText = "IA",
                accentColorHex = 0xFF26C6DA,
                sortOrder = 8
            )
        )
    }
}
