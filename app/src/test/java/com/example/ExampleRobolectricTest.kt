package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.browser.BrowserUrlUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Nova Browser", appName)
    }

    @Test
    fun `verify direct mp4 versus protected streaming detection`() {
        // Direct MP4 URLs (like W3Schools & Sample Videos)
        assertTrue(
            BrowserUrlUtils.isDirectVideoCandidate(
                "https://www.w3schools.com/html/mov_bbb.mp4",
                "video/mp4"
            )
        )
        // Video with no extension in URL but Content-Type is video/mp4
        assertTrue(
            BrowserUrlUtils.isDirectVideoCandidate(
                "https://cdn.example.com/media/stream?id=48219",
                "video/mp4"
            )
        )
        // HLS (.m3u8), DASH (.mpd), blob:, and YouTube must be flagged as protected/streaming
        assertTrue(BrowserUrlUtils.isProtectedOrStreamingUrl("https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"))
        assertTrue(BrowserUrlUtils.isProtectedOrStreamingUrl("https://example.com/manifest.mpd"))
        assertTrue(BrowserUrlUtils.isProtectedOrStreamingUrl("blob:https://www.youtube.com/1234-5678"))
        assertTrue(BrowserUrlUtils.isProtectedOrStreamingUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertFalse(BrowserUrlUtils.isDirectVideoCandidate("https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"))
    }
}
