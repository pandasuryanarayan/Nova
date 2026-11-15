package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.browser.AdBlocker
import com.example.browser.BrowserUrlUtils
import com.example.browser.NovaDownloadHelper
import com.example.browser.NovaWebViewPool
import com.example.data.AdBlockMode
import com.example.data.BrowserRepository
import com.example.data.BrowserTab
import com.example.data.NOVA_HOME_URL
import com.example.data.NovaDatabase
import com.example.data.SiteConfigSettings
import com.example.data.SiteCookieEntry
import com.example.data.SiteCookiePolicy
import com.example.data.SitePermissionMode
import com.example.ui.components.dismissSoftKeyboard
import android.widget.EditText
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
        assertEquals("Netra", appName)
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

    @Test
    fun `verify exact adblocker rule counts match catalog without duplicates`() {
        assertEquals(Triple(40, 8, 10), AdBlocker.getRuleCountsForMode(AdBlockMode.MINIMAL))
        assertEquals(Triple(141, 45, 49), AdBlocker.getRuleCountsForMode(AdBlockMode.PRO))
        assertEquals(Triple(147, 55, 65), AdBlocker.getRuleCountsForMode(AdBlockMode.MAX))

        assertEquals(58, AdBlocker.getTotalRuleCount(AdBlockMode.MINIMAL))
        assertEquals(235, AdBlocker.getTotalRuleCount(AdBlockMode.PRO))
        assertEquals(267, AdBlocker.getTotalRuleCount(AdBlockMode.MAX))

        for (mode in AdBlockMode.entries) {
            val (h, p, d) = AdBlocker.getRuleCountsForMode(mode)
            val catalog = AdBlocker.getRulesCatalogForMode(mode)
            assertEquals("Total count mismatch for $mode", h + p + d, AdBlocker.getTotalRuleCount(mode))
            assertEquals("Catalog size mismatch for $mode", AdBlocker.getTotalRuleCount(mode), catalog.size)
            assertEquals("Duplicate rule in catalog for $mode", catalog.size, catalog.map { it.rule }.distinct().size)
        }

        val minimalRules = AdBlocker.getRulesCatalogForMode(AdBlockMode.MINIMAL).map { it.rule }.toSet()
        val proRules = AdBlocker.getRulesCatalogForMode(AdBlockMode.PRO).map { it.rule }.toSet()
        val maxRules = AdBlocker.getRulesCatalogForMode(AdBlockMode.MAX).map { it.rule }.toSet()

        assertTrue("Pro must contain all Minimal rules", proRules.containsAll(minimalRules))
        assertTrue("Max must contain all Minimal rules", maxRules.containsAll(minimalRules))
        assertTrue("Max must contain all Pro rules", maxRules.containsAll(proRules))
    }

    @Test
    fun `verify cumulative network host and path enforcement across modes`() {
        // 1. Minimal hosts & paths enforced in Minimal, Pro, and Max
        for (host in AdBlocker.MINIMAL_HOST_RULES.keys) {
            val testUrl = "https://sub.$host/pixel.js"
            assertTrue("Minimal failed to block $host", AdBlocker.isAdUrl(testUrl, AdBlockMode.MINIMAL))
            assertTrue("Pro failed to block Minimal host $host", AdBlocker.isAdUrl(testUrl, AdBlockMode.PRO))
            assertTrue("Max failed to block Minimal host $host", AdBlocker.isAdUrl(testUrl, AdBlockMode.MAX))
        }
        for ((pattern, _) in AdBlocker.MINIMAL_PATH_PATTERNS) {
            val testUrl = "https://cdn.example.org${pattern}loader.js"
            assertTrue("Minimal failed path $pattern", AdBlocker.isAdUrl(testUrl, AdBlockMode.MINIMAL))
            assertTrue("Pro failed Minimal path $pattern", AdBlocker.isAdUrl(testUrl, AdBlockMode.PRO))
            assertTrue("Max failed Minimal path $pattern", AdBlocker.isAdUrl(testUrl, AdBlockMode.MAX))
        }

        // 2. Pro hosts & paths enforced in Pro and Max
        for (host in AdBlocker.PRO_HOST_RULES.keys) {
            val testUrl = "https://$host/script.js"
            assertTrue("Pro failed to block $host", AdBlocker.isAdUrl(testUrl, AdBlockMode.PRO))
            assertTrue("Max failed to block Pro host $host", AdBlocker.isAdUrl(testUrl, AdBlockMode.MAX))
        }
        for ((pattern, _) in AdBlocker.PRO_PATH_PATTERNS) {
            val testUrl = if (pattern.contains('.')) {
                "https://$pattern?cb=1"
            } else {
                "https://cdn.example.org${pattern}ad.js"
            }
            assertTrue("Pro failed path $pattern ($testUrl)", AdBlocker.isAdUrl(testUrl, AdBlockMode.PRO))
            assertTrue("Max failed Pro path $pattern ($testUrl)", AdBlocker.isAdUrl(testUrl, AdBlockMode.MAX))
        }

        // Pro-only regexes & hosts must NOT block in Minimal
        assertFalse(AdBlocker.isAdUrl("https://mixpanel.com/track/", AdBlockMode.MINIMAL))
        assertTrue(AdBlocker.isAdUrl("https://mixpanel.com/track/", AdBlockMode.PRO))
        assertTrue(AdBlocker.isAdUrl("https://mixpanel.com/track/", AdBlockMode.MAX))

        assertFalse(AdBlocker.isAdUrl("https://cdn.example.com/ads/vast.xml?v=3", AdBlockMode.MINIMAL))
        assertTrue(AdBlocker.isAdUrl("https://cdn.example.com/ads/vast.xml?v=3", AdBlockMode.PRO))
        assertTrue(AdBlocker.isAdUrl("https://cdn.example.com/ads/vast.xml?v=3", AdBlockMode.MAX))

        assertFalse(AdBlocker.isAdUrl("https://cdn.example.com/pbjs.min.js", AdBlockMode.MINIMAL))
        assertTrue(AdBlocker.isAdUrl("https://cdn.example.com/pbjs.min.js", AdBlockMode.PRO))
        assertTrue(AdBlocker.isAdUrl("https://cdn.example.com/pbjs.min.js", AdBlockMode.MAX))

        // 3. Max YouTube rules enforced in Max
        for (host in AdBlocker.MAX_YOUTUBE_HOST_RULES.keys) {
            val testUrl = "https://$host/ad"
            assertTrue("Max failed YouTube host $host", AdBlocker.isAdUrl(testUrl, AdBlockMode.MAX))
        }
        for ((pattern, _) in AdBlocker.MAX_YOUTUBE_PATH_PATTERNS) {
            val testUrl = if (pattern.startsWith("/")) {
                "https://www.youtube.com${pattern}?v=1"
            } else {
                "https://www.$pattern?v=1"
            }
            assertTrue("Max failed YouTube path $pattern ($testUrl)", AdBlocker.isAdUrl(testUrl, AdBlockMode.MAX))
        }

        // Max-only YouTube paths must NOT block in Minimal/Pro, and safe video/search must pass in all modes
        assertFalse(AdBlocker.isAdUrl("https://www.youtube.com/api/stats/ads?ns=yt", AdBlockMode.MINIMAL))
        assertFalse(AdBlocker.isAdUrl("https://www.youtube.com/api/stats/ads?ns=yt", AdBlockMode.PRO))
        assertTrue(AdBlocker.isAdUrl("https://www.youtube.com/api/stats/ads?ns=yt", AdBlockMode.MAX))

        assertFalse(AdBlocker.isAdUrl("https://rr3---sn-4g5ednsz.googlevideo.com/videoplayback?id=1", AdBlockMode.MAX))
        assertFalse(AdBlocker.isAdUrl("https://www.google.com/search?q=netra", AdBlockMode.MAX))
    }


    @Test
    fun `verify cumulative cosmetic dom selectors across modes`() {
        val minimalJs = AdBlocker.buildCosmeticAdShieldJs(AdBlockMode.MINIMAL)
        val proJs = AdBlocker.buildCosmeticAdShieldJs(AdBlockMode.PRO)
        val maxJs = AdBlocker.buildCosmeticAdShieldJs(AdBlockMode.MAX)

        for (sel in AdBlocker.MINIMAL_DOM_SELECTORS) {
            assertTrue("Minimal JS missing $sel", minimalJs.contains(sel))
            assertTrue("Pro JS missing Minimal $sel", proJs.contains(sel))
            assertTrue("Max JS missing Minimal $sel", maxJs.contains(sel))
        }
        for (sel in AdBlocker.PRO_DOM_SELECTORS) {
            if (sel !in AdBlocker.MINIMAL_DOM_SELECTORS) {
                assertFalse("Minimal JS should not contain Pro-only $sel", minimalJs.contains("'$sel'"))
            }
            assertTrue("Pro JS missing $sel", proJs.contains(sel))
            assertTrue("Max JS missing Pro $sel", maxJs.contains(sel))
        }
        for (sel in AdBlocker.MAX_YOUTUBE_DOM_SELECTORS) {
            if (sel !in AdBlocker.MINIMAL_DOM_SELECTORS) {
                assertFalse("Minimal JS should not contain Max-only $sel", minimalJs.contains("'$sel'"))
            }
            if (sel !in AdBlocker.MINIMAL_DOM_SELECTORS && sel !in AdBlocker.PRO_DOM_SELECTORS) {
                assertFalse("Pro JS should not contain Max-only $sel", proJs.contains("'$sel'"))
            }
            assertTrue("Max JS missing $sel", maxJs.contains(sel))
        }
        assertTrue("Live mode upgrade should clear previous interval", maxJs.contains("clearInterval(window.__novaAdShieldInterval)"))
    }

    @Test
    fun `verify per-site chrome config persistence normalization and reset`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dao = NovaDatabase.getInstance(context).browserDao()
        val repo1 = BrowserRepository(context, dao)

        val defaultCfg = repo1.getSiteConfig("example.com")
        assertTrue(defaultCfg.isDefault)
        assertTrue(defaultCfg.javaScriptEnabled)
        assertEquals(SiteCookiePolicy.ALLOW_ALL, defaultCfg.cookiePolicy)
        assertEquals(SitePermissionMode.ASK, defaultCfg.locationPermission)

        val customized = SiteConfigSettings(
            domain = "www.Example.com",
            isAdBlockEnabled = false,
            javaScriptEnabled = false,
            cookiePolicy = SiteCookiePolicy.BLOCK_THIRD_PARTY,
            popupsAllowed = true,
            imagesEnabled = false,
            soundEnabled = false,
            autoplayEnabled = false,
            domStorageEnabled = false,
            locationPermission = SitePermissionMode.ALLOW,
            cameraPermission = SitePermissionMode.BLOCK,
            microphonePermission = SitePermissionMode.ALLOW,
            protectedContentAllowed = false,
            insecureContentAllowed = false,
            safeBrowsingEnabled = false
        )
        assertFalse(customized.isDefault)
        repo1.saveSiteConfig(customized)

        // New repository instance reads same SharedPreferences
        val repo2 = BrowserRepository(context, dao)
        val loaded = repo2.getSiteConfig("example.com")
        assertEquals("example.com", loaded.domain)
        assertFalse(loaded.isAdBlockEnabled)
        assertFalse(loaded.javaScriptEnabled)
        assertEquals(SiteCookiePolicy.BLOCK_THIRD_PARTY, loaded.cookiePolicy)
        assertTrue(loaded.popupsAllowed)
        assertFalse(loaded.imagesEnabled)
        assertFalse(loaded.soundEnabled)
        assertFalse(loaded.autoplayEnabled)
        assertFalse(loaded.domStorageEnabled)
        assertEquals(SitePermissionMode.ALLOW, loaded.locationPermission)
        assertEquals(SitePermissionMode.BLOCK, loaded.cameraPermission)
        assertEquals(SitePermissionMode.ALLOW, loaded.microphonePermission)
        assertFalse(loaded.protectedContentAllowed)
        assertFalse(loaded.insecureContentAllowed)
        assertFalse(loaded.safeBrowsingEnabled)

        repo2.resetSiteConfig("www.example.com")
        assertTrue(repo2.getSiteConfig("example.com").isDefault)
    }

    @Test
    fun `verify ssl certificate details fallback and cookie entry sizing`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pool = NovaWebViewPool(
            context = context,
            onTabStateUpdated = { _, _, _, _, _, _, _, _, _ -> },
            onHistoryVisited = { _, _, _ -> },
            onAdBlocked = {},
            onVideoLongPressed = {},
            onVideoSizeProbedInWebView = { _, _, _ -> },
            onWebViewDownloadChunk = { _, _, _, _ -> },
            onWebViewDownloadFinished = { _, _ -> },
            onWebViewDownloadError = { _, _ -> },
            onDirectDownloadRequested = { _, _, _, _, _ -> },
            onEnterFullscreenVideo = { _, _ -> },
            onExitFullscreenVideo = {},
            onFindResultUpdated = { _, _ -> },
            onDevToolsElementSelectedJson = { _, _ -> },
            onDevToolsConsoleLogJson = { _, _ -> },
            onDevToolsNetworkEntryJson = { _, _ -> },
            onPageFinishedForDevTools = {},
            isAdBlockEnabledProvider = { true },
            isDarkModeEnabledProvider = { false }
        )

        val httpsDetails = pool.getSslCertificateDetails(
            BrowserTab(url = "https://github.com/trending", isSecureSsl = true)
        )
        assertTrue(httpsDetails.isSecure)
        assertEquals("github.com", httpsDetails.issuedToCommonName)
        assertTrue(httpsDetails.protocol.contains("TLS"))
        assertTrue(httpsDetails.subjectAltNames.contains("github.com"))

        val httpDetails = pool.getSslCertificateDetails(
            BrowserTab(url = "http://neverssl.com", isSecureSsl = false)
        )
        assertFalse(httpDetails.isSecure)
        assertTrue(httpDetails.protocol.contains("HTTP"))

        val homeDetails = pool.getSslCertificateDetails(
            BrowserTab(url = NOVA_HOME_URL, isSecureSsl = true)
        )
        assertTrue(homeDetails.isSecure)
        assertTrue(homeDetails.protocol.contains("netra://"))

        val cookie = SiteCookieEntry(name = "SID", value = "abc123", domain = "google.com")
        assertEquals(9, cookie.byteSize)

        val tab = BrowserTab(url = "https://video-example.com/players", isSecureSsl = true)
        val wv = pool.getOrCreateWebView(tab)
        assertFalse(wv.settings.supportMultipleWindows())
        assertFalse(wv.settings.javaScriptCanOpenWindowsAutomatically)

        pool.applySiteConfig(
            wv,
            tab,
            SiteConfigSettings(domain = "video-example.com", popupsAllowed = true)
        )
        assertTrue(wv.settings.supportMultipleWindows())
        assertTrue(wv.settings.javaScriptCanOpenWindowsAutomatically)
    }

    @Test
    fun `verify content range parsing redirect resolution and keyboard dismissal`() {
        assertEquals(5242880L, NovaDownloadHelper.parseContentRangeTotalBytes("bytes 0-1/5242880"))
        assertEquals(12345678L, NovaDownloadHelper.parseContentRangeTotalBytes("bytes */12345678"))
        assertEquals(-1L, NovaDownloadHelper.parseContentRangeTotalBytes("bytes 0-1/*"))
        assertEquals(-1L, NovaDownloadHelper.parseContentRangeTotalBytes(null))
        assertEquals(-1L, NovaDownloadHelper.parseContentRangeTotalBytes(""))

        assertEquals(
            "http://cdn.video-host.net/files/show 1.mp4".replace(" ", "%20"),
            NovaDownloadHelper.resolveRedirectUrl(
                "https://site.example.com/dl?id=42",
                "http://cdn.video-host.net/files/show 1.mp4"
            )
        )
        assertEquals(
            "https://site.example.com/media/clip.mp4?token=xyz",
            NovaDownloadHelper.resolveRedirectUrl(
                "https://site.example.com/player/watch",
                "/media/clip.mp4?token=xyz"
            )
        )
        assertEquals(
            "https://edge.cdn.net/stream/video.mp4",
            NovaDownloadHelper.resolveRedirectUrl(
                "https://site.example.com/watch",
                "//edge.cdn.net/stream/video.mp4"
            )
        )
        assertEquals(null, NovaDownloadHelper.resolveRedirectUrl("https://site.example.com", "  "))

        val context = ApplicationProvider.getApplicationContext<Context>()
        dismissSoftKeyboard(null)
        val et = EditText(context)
        dismissSoftKeyboard(et)
    }

    @Test
    fun `verify toggling incognito preserves existing tabs and shifts without deleting`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.viewmodel.BrowserViewModel(app)

        // Initially 1 default home tab
        val initialDefaultTabId = vm.uiState.value.activeTabId
        assertEquals(1, vm.uiState.value.tabs.size)
        assertFalse(vm.uiState.value.activeTab.isIncognito)

        // Toggle incognito ON -> must keep initial default home tab AND create/shift to a new private tab
        vm.toggleIncognitoMode()
        assertEquals(2, vm.uiState.value.tabs.size)
        assertTrue(vm.uiState.value.activeTab.isIncognito)
        val privateTabId = vm.uiState.value.activeTabId
        assertTrue(vm.uiState.value.tabs.any { it.id == initialDefaultTabId && !it.isIncognito })
        assertTrue(vm.uiState.value.tabs.any { it.id == privateTabId && it.isIncognito })

        // Toggle incognito OFF -> must keep the private tab (do NOT delete it) and shift back to existing default tab
        vm.toggleIncognitoMode()
        assertEquals(2, vm.uiState.value.tabs.size)
        assertFalse(vm.uiState.value.activeTab.isIncognito)
        assertEquals(initialDefaultTabId, vm.uiState.value.activeTabId)
        assertTrue(vm.uiState.value.tabs.any { it.id == privateTabId && it.isIncognito })

        // Toggle incognito ON again -> shifts back to existing private tab without duplicating
        vm.toggleIncognitoMode()
        assertEquals(2, vm.uiState.value.tabs.size)
        assertTrue(vm.uiState.value.activeTab.isIncognito)
        assertEquals(privateTabId, vm.uiState.value.activeTabId)
    }

    @Test
    fun `verify tab grouping default naming renaming popup and reversible ungrouping in both modes`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.viewmodel.BrowserViewModel(app)

        // 1. Default mode: create 3 normal tabs
        val tab1Id = vm.uiState.value.activeTabId
        vm.openNewTab("https://github.com", isIncognito = false)
        val tab2Id = vm.uiState.value.activeTabId
        vm.openNewTab("https://photos.google.com", isIncognito = false)
        val tab3Id = vm.uiState.value.activeTabId

        // Drop tab2 onto tab1 -> creates "Group 1" and opens group popup
        vm.groupTabs(draggedTabId = tab2Id, targetTabId = tab1Id)
        assertEquals(1, vm.uiState.value.tabGroups.size)
        val defaultGroup = vm.uiState.value.tabGroups.first()
        assertEquals("Group 1", defaultGroup.name)
        assertFalse(defaultGroup.isIncognito)
        assertEquals(defaultGroup.id, vm.uiState.value.openGroupPopupId)
        assertEquals(
            2,
            vm.uiState.value.tabs.count { it.groupId == defaultGroup.id }
        )

        // Drop tab3 onto tab1 -> adds tab3 to "Group 1" (3 tabs in group)
        vm.groupTabs(draggedTabId = tab3Id, targetTabId = tab1Id)
        assertEquals(
            3,
            vm.uiState.value.tabs.count { it.groupId == defaultGroup.id }
        )

        // Rename "Group 1" -> "Dev & Photos"
        vm.renameTabGroup(defaultGroup.id, "Dev & Photos")
        assertEquals(
            "Dev & Photos",
            vm.uiState.value.tabGroups.first { it.id == defaultGroup.id }.name
        )

        // Reverse process: remove tab3 from group -> tab3 returns to normal list, 2 tabs remain in group
        vm.removeTabFromGroup(tab3Id)
        assertEquals(null, vm.uiState.value.tabs.first { it.id == tab3Id }.groupId)
        assertEquals(
            2,
            vm.uiState.value.tabs.count { it.groupId == defaultGroup.id }
        )

        // Reverse process on remaining 2-tab group: remove tab2 -> both tab2 and tab1 return to normal list
        vm.removeTabFromGroup(tab2Id)
        assertTrue(vm.uiState.value.tabs.all { it.groupId == null })
        assertTrue(vm.uiState.value.tabGroups.isEmpty())
        assertEquals(null, vm.uiState.value.openGroupPopupId)

        // 2. Private (Incognito) mode: create 2 private tabs and group them
        vm.openNewTab("https://example.com", isIncognito = true)
        val pTab1Id = vm.uiState.value.activeTabId
        vm.openNewTab("https://duckduckgo.com", isIncognito = true)
        val pTab2Id = vm.uiState.value.activeTabId

        vm.groupTabs(draggedTabId = pTab2Id, targetTabId = pTab1Id)
        assertEquals(1, vm.uiState.value.tabGroups.size)
        val privateGroup = vm.uiState.value.tabGroups.first()
        assertEquals("Group 1", privateGroup.name)
        assertTrue(privateGroup.isIncognito)
        assertEquals(privateGroup.id, vm.uiState.value.openGroupPopupId)

        // Reverse in private mode -> places back in normal private list
        vm.removeTabFromGroup(pTab2Id)
        assertTrue(vm.uiState.value.tabGroups.isEmpty())
        assertTrue(vm.uiState.value.tabs.filter { it.isIncognito }.all { it.groupId == null })

        assertEquals("All Tabs", com.example.ui.i18n.EnglishNovaStrings.openTabsTitle)
        assertEquals("सभी टैब", com.example.ui.i18n.HindiNovaStrings.openTabsTitle)
    }
}


