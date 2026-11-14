package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testParseContentRangeTotalBytes() {
    assertEquals(15728640L, com.example.browser.NovaDownloadHelper.parseContentRangeTotalBytes("bytes 0-1/15728640"))
    assertEquals(50000000L, com.example.browser.NovaDownloadHelper.parseContentRangeTotalBytes("bytes 0-100/50000000"))
    assertEquals(-1L, com.example.browser.NovaDownloadHelper.parseContentRangeTotalBytes("bytes */*"))
    assertEquals(-1L, com.example.browser.NovaDownloadHelper.parseContentRangeTotalBytes(null))
    assertEquals(-1L, com.example.browser.NovaDownloadHelper.parseContentRangeTotalBytes(""))
    assertEquals(-1L, com.example.browser.NovaDownloadHelper.parseContentRangeTotalBytes("invalid"))
  }

  @Test
  fun testResolveRedirectUrl() {
    val base = "https://example.com/videos/stream.mp4"
    assertEquals(
      "https://example.com/videos/final.mp4",
      com.example.browser.NovaDownloadHelper.resolveRedirectUrl(base, "final.mp4")
    )
    assertEquals(
      "https://cdn.example.com/media/file.mp4",
      com.example.browser.NovaDownloadHelper.resolveRedirectUrl(base, "https://cdn.example.com/media/file.mp4")
    )
    assertEquals(
      "https://example.com/videos/final%20file.mp4",
      com.example.browser.NovaDownloadHelper.resolveRedirectUrl(base, "final file.mp4")
    )
  }

  @Test
  fun testFormatBytes() {
    assertEquals("15.00 MB", com.example.browser.BrowserUrlUtils.formatBytes(15 * 1024 * 1024L))
    assertEquals("1.50 GB", com.example.browser.BrowserUrlUtils.formatBytes((1.5 * 1024 * 1024 * 1024L).toLong()))
    assertEquals("500.0 KB", com.example.browser.BrowserUrlUtils.formatBytes(500 * 1024L))
  }

  @Test
  fun testCloseAllTabsStrings() {
    assertEquals("Close all", com.example.ui.i18n.EnglishNovaStrings.closeAllTabsButton)
    assertEquals("सब बंद करें", com.example.ui.i18n.HindiNovaStrings.closeAllTabsButton)
  }
}
