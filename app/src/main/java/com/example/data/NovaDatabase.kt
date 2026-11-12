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
    version = 2,
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
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }

        fun defaultShortcuts(): List<QuickShortcutEntity> = listOf(
            QuickShortcutEntity(
                title = "Google",
                url = "https://www.google.com",
                subtitle = "Search Engine",
                badgeText = "G",
                accentColorHex = 0xFF4285F4,
                sortOrder = 1
            ),
            QuickShortcutEntity(
                title = "YouTube",
                url = "https://m.youtube.com",
                subtitle = "Videos & Creators",
                badgeText = "YT",
                accentColorHex = 0xFFFF5252,
                sortOrder = 2
            ),
            QuickShortcutEntity(
                title = "DuckDuckGo",
                url = "https://duckduckgo.com",
                subtitle = "Private Search",
                badgeText = "DDG",
                accentColorHex = 0xFFFF7043,
                sortOrder = 3
            ),
            QuickShortcutEntity(
                title = "Wikipedia",
                url = "https://www.wikipedia.org",
                subtitle = "Free Encyclopedia",
                badgeText = "W",
                accentColorHex = 0xFF90A4AE,
                sortOrder = 4
            ),
            QuickShortcutEntity(
                title = "GitHub",
                url = "https://github.com",
                subtitle = "Code & Releases",
                badgeText = "GH",
                accentColorHex = 0xFF00E5FF,
                sortOrder = 5
            ),
            QuickShortcutEntity(
                title = "Reddit",
                url = "https://www.reddit.com",
                subtitle = "Communities",
                badgeText = "RD",
                accentColorHex = 0xFFFFB300,
                sortOrder = 6
            ),
            QuickShortcutEntity(
                title = "Archive.org",
                url = "https://archive.org",
                subtitle = "Digital Library",
                badgeText = "IA",
                accentColorHex = 0xFF00E676,
                sortOrder = 7
            ),
            QuickShortcutEntity(
                title = "StackOverflow",
                url = "https://stackoverflow.com",
                subtitle = "Developer Q&A",
                badgeText = "SO",
                accentColorHex = 0xFF8C62FF,
                sortOrder = 8
            )
        )
    }
}
