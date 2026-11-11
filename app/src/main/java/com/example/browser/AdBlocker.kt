package com.example.browser

import android.net.Uri
import android.webkit.WebResourceResponse
import com.example.data.BlockedAdEntry
import java.io.ByteArrayInputStream
import java.util.Locale

object AdBlocker {
    private val BLOCKED_HOST_RULES = mapOf(
        "doubleclick.net" to ("Ad Network Script" to "||doubleclick.net^"),
        "googlesyndication.com" to ("AdSense / Banner Ad" to "||googlesyndication.com^"),
        "googleadservices.com" to ("Conversion Ad Tracker" to "||googleadservices.com^"),
        "adservice.google.com" to ("Ad Personalization" to "||adservice.google.com^"),
        "google-analytics.com" to ("Analytics Tracker" to "||google-analytics.com^"),
        "googletagmanager.com" to ("Tag Manager Tracker" to "||googletagmanager.com^"),
        "adnxs.com" to ("AppNexus Ad Exchange" to "||adnxs.com^"),
        "taboola.com" to ("Sponsored Content Ad" to "||taboola.com^"),
        "outbrain.com" to ("Sponsored Feed Ad" to "||outbrain.com^"),
        "criteo.com" to ("Retargeting Ad" to "||criteo.com^"),
        "criteo.net" to ("Retargeting Banner" to "||criteo.net^"),
        "scorecardresearch.com" to ("Behavioral Tracker" to "||scorecardresearch.com^"),
        "adsafeprotected.com" to ("Ad Verification Script" to "||adsafeprotected.com^"),
        "moatads.com" to ("Ad Telemetry Script" to "||moatads.com^"),
        "rubiconproject.com" to ("Programmatic Ad Bid" to "||rubiconproject.com^"),
        "pubmatic.com" to ("Header Bidding Ad" to "||pubmatic.com^"),
        "openx.net" to ("OpenX Ad Server" to "||openx.net^"),
        "popads.net" to ("Pop-Under Ad Script" to "||popads.net^"),
        "popcash.net" to ("Pop-Up Ad Script" to "||popcash.net^"),
        "propellerads.com" to ("Push / Pop Ad" to "||propellerads.com^"),
        "adform.net" to ("Display Ad Banner" to "||adform.net^"),
        "amazon-adsystem.com" to ("Amazon Ad Beacon" to "||amazon-adsystem.com^"),
        "advertising.com" to ("Display Ad Network" to "||advertising.com^"),
        "quantserve.com" to ("Audience Tracker" to "||quantserve.com^"),
        "zedo.com" to ("Interstitial Ad" to "||zedo.com^"),
        "casalemedia.com" to ("Index Exchange Ad" to "||casalemedia.com^"),
        "smartadserver.com" to ("SmartAd Banner" to "||smartadserver.com^"),
        "2mdn.net" to ("Rich Media Ad Asset" to "||2mdn.net^"),
        "adcolony.com" to ("Video Pre-Roll Ad" to "||adcolony.com^"),
        "adsrvr.org" to ("TheTradeDesk Ad" to "||adsrvr.org^"),
        "teads.tv" to ("Outstream Video Ad" to "||teads.tv^"),
        "triplelift.com" to ("Native Ad Placement" to "||triplelift.com^"),
        "sharethrough.com" to ("Native Ad Exchange" to "||sharethrough.com^"),
        "media.net" to ("Contextual Ad Unit" to "||media.net^"),
        "mgid.com" to ("Native Widget Ad" to "||mgid.com^"),
        "revcontent.com" to ("Sponsored Widget Ad" to "||revcontent.com^"),
        "exoclick.com" to ("Banner / Pop Ad" to "||exoclick.com^"),
        "hotjar.com" to ("Session Recording Tracker" to "||hotjar.com^"),
        "clarity.ms" to ("Telemetry Tracker" to "||clarity.ms^"),
        "connect.facebook.net" to ("Social Pixel Tracker" to "||connect.facebook.net^")
    )

    private val AD_PATH_PATTERNS = listOf(
        "/pagead/" to ("AdSense Script" to "/pagead/*"),
        "/ads/ga-audiences" to ("Audience Pixel" to "/ads/ga-audiences*"),
        "/adserver/" to ("Ad Server Frame" to "/adserver/*"),
        "/adframe" to ("Banner Ad Iframe" to "/adframe*"),
        "/ads.js" to ("Ad Loader Script" to "*/ads.js"),
        "/prebid" to ("Header Bidding Script" to "*/prebid*.js"),
        "/gpt.js" to ("Google Publisher Tags" to "*/gpt.js"),
        "/adsbygoogle.js" to ("AdSense Auto-Ads" to "*/adsbygoogle.js")
    )

    fun isAdUrl(url: String): Boolean {
        return matchAdRequest("", url, "") != null
    }

    fun matchAdRequest(
        tabId: String,
        url: String,
        pageUrl: String
    ): BlockedAdEntry? {
        val parsed = try {
            Uri.parse(url)
        } catch (_: Exception) {
            return null
        }
        val host = parsed.host?.lowercase(Locale.US) ?: return null
        val lowerUrl = url.lowercase(Locale.US)
        val pageDomain = BrowserUrlUtils.extractDomain(pageUrl.ifBlank { url })

        // 1. Check host suffix rules
        for ((blockedDomain, meta) in BLOCKED_HOST_RULES) {
            if (host == blockedDomain || host.endsWith(".$blockedDomain")) {
                return BlockedAdEntry(
                    tabId = tabId,
                    adUrl = url.take(260),
                    adDomain = host,
                    pageDomain = pageDomain,
                    category = meta.first,
                    filterRule = meta.second
                )
            }
        }

        // 2. Check path / script rules
        for ((pattern, meta) in AD_PATH_PATTERNS) {
            if (lowerUrl.contains(pattern)) {
                return BlockedAdEntry(
                    tabId = tabId,
                    adUrl = url.take(260),
                    adDomain = host,
                    pageDomain = pageDomain,
                    category = meta.first,
                    filterRule = meta.second
                )
            }
        }

        return null
    }

    fun createEmptyResource(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "utf-8",
            204,
            "No Content",
            mapOf("Access-Control-Allow-Origin" to "*"),
            ByteArrayInputStream(ByteArray(0))
        )
    }

    const val COSMETIC_AD_SHIELD_JS = """
        (function() {
          if (window.__novaAdShieldCosmeticInstalled) return;
          window.__novaAdShieldCosmeticInstalled = true;
          var reportedSelectors = {};
          var adSelectors = [
            'ins.adsbygoogle',
            '[id^="div-gpt-ad"]',
            '[id^="google_ads_iframe"]',
            'iframe[src*="doubleclick.net"]',
            'iframe[src*="googlesyndication.com"]',
            'iframe[id*="google_ads"]',
            '[class*="ad-banner"]',
            '[class*="ad-slot"]',
            '[data-ad-client]',
            '[data-ad-slot]'
          ];
          function sweepDomAds() {
            try {
              var selectorStr = adSelectors.join(',');
              var nodes = document.querySelectorAll(selectorStr);
              for (var i = 0; i < nodes.length; i++) {
                var el = nodes[i];
                if (el.__novaAdHidden) continue;
                el.__novaAdHidden = true;
                el.style.setProperty('display', 'none', 'important');
                el.style.setProperty('height', '0px', 'important');
                var tag = (el.tagName || 'div').toLowerCase();
                var idOrClass = el.id ? ('#' + el.id) : (el.className && typeof el.className === 'string' ? ('.' + el.className.trim().split(/\s+/)[0]) : '');
                var ruleKey = '##' + tag + idOrClass;
                if (!reportedSelectors[ruleKey] && window.NovaVideoBridge && window.NovaVideoBridge.onDomAdBlocked) {
                  reportedSelectors[ruleKey] = true;
                  var srcUrl = el.getAttribute('src') || el.getAttribute('data-ad-client') || (window.location.origin + '/' + ruleKey);
                  window.NovaVideoBridge.onDomAdBlocked(JSON.stringify({
                    adUrl: String(srcUrl),
                    adDomain: window.location.hostname || 'dom-element',
                    pageDomain: window.location.hostname || 'page',
                    category: 'DOM Ad Container',
                    filterRule: ruleKey
                  }));
                }
              }
            } catch (e) {}
          }
          sweepDomAds();
          setInterval(sweepDomAds, 2200);
        })();
    """
}

object BrowserUrlUtils {
    private val STREAMING_EXTENSION_REGEX = Regex("""\.(m3u8|mpd|ism)(\?|#|$)""", RegexOption.IGNORE_CASE)
    private val DIRECT_VIDEO_EXTENSION_REGEX = Regex("""\.(mp4|mov|webm|mkv|m4v)(\?|#|$)""", RegexOption.IGNORE_CASE)
    private val PROTECTED_DOMAINS = listOf(
        "youtube.com",
        "youtu.be",
        "instagram.com",
        "facebook.com",
        "fb.watch",
        "tiktok.com",
        "netflix.com",
        "disneyplus.com",
        "primevideo.com"
    )

    fun normalizeInputToUrl(rawInput: String): String {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return "https://www.google.com"
        if (trimmed.startsWith("nova://", ignoreCase = true) ||
            trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("file://", ignoreCase = true)
        ) {
            return trimmed
        }
        val hasNoSpaces = !trimmed.contains(' ')
        val hasDot = trimmed.contains('.')
        if (hasNoSpaces && hasDot) {
            return "https://$trimmed"
        }
        return "https://www.google.com/search?q=${Uri.encode(trimmed)}"
    }

    /**
     * Converts mobile subdomain URLs (e.g. en.m.wikipedia.org, m.youtube.com, m.facebook.com, mobile.twitter.com)
     * into their full Desktop URL equivalents when Desktop Site Mode is enabled.
     */
    fun toDesktopUrl(url: String): String {
        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            return url
        }
        return try {
            val uri = Uri.parse(url)
            val host = uri.host ?: return url
            val desktopHost = when {
                host.contains(".m.") -> host.replaceFirst(".m.", ".")
                host.startsWith("m.") && host.count { it == '.' } >= 2 -> "www." + host.removePrefix("m.")
                host.startsWith("mobile.") && host.count { it == '.' } >= 2 -> "www." + host.removePrefix("mobile.")
                else -> host
            }
            var builder = uri.buildUpon().authority(
                if (uri.port != -1) "$desktopHost:${uri.port}" else desktopHost
            )
            // For YouTube/Wikipedia desktop enforcement, add desktop query hint if needed
            if (desktopHost.contains("youtube.com") && uri.getQueryParameter("app") == null) {
                builder = builder.appendQueryParameter("app", "desktop")
            }
            builder.build().toString()
        } catch (_: Exception) {
            url
        }
    }

    /**
     * Restores desktop-forced URLs (such as ?app=desktop) back to clean mobile URLs when switching off Desktop Mode.
     */
    fun toMobileUrl(url: String): String {
        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            return url
        }
        return try {
            val uri = Uri.parse(url)
            if (uri.getQueryParameter("app") == "desktop") {
                val builder = uri.buildUpon().clearQuery()
                for (name in uri.queryParameterNames) {
                    if (name != "app") {
                        uri.getQueryParameters(name).forEach { v ->
                            builder.appendQueryParameter(name, v)
                        }
                    }
                }
                builder.build().toString()
            } else {
                url
            }
        } catch (_: Exception) {
            url
        }
    }

    fun extractDomain(url: String): String {
        if (url.startsWith("nova://")) return "Nova Browser"
        return try {
            val host = Uri.parse(url).host ?: return url.take(30)
            host.removePrefix("www.")
        } catch (_: Exception) {
            url.take(30)
        }
    }

    fun isProtectedOrStreamingUrl(url: String, contentType: String? = null): Boolean {
        val lower = url.trim().lowercase(Locale.US)
        if (lower.startsWith("blob:") || lower.startsWith("mediasource:") || lower.startsWith("data:")) {
            return true
        }
        if (STREAMING_EXTENSION_REGEX.containsMatchIn(lower)) {
            return true
        }
        val host = extractDomain(lower)
        if (PROTECTED_DOMAINS.any { host == it || host.endsWith(".$it") }) {
            return true
        }
        val mime = contentType?.lowercase(Locale.US) ?: ""
        if (mime.contains("mpegurl") || mime.contains("dash+xml") || mime.contains("vnd.ms-sstr")) {
            return true
        }
        return false
    }

    fun isDirectVideoCandidate(url: String, contentType: String? = null): Boolean {
        if (isProtectedOrStreamingUrl(url, contentType)) return false
        val lower = url.trim().lowercase(Locale.US)
        val mime = contentType?.lowercase(Locale.US) ?: ""
        return DIRECT_VIDEO_EXTENSION_REGEX.containsMatchIn(lower) ||
            mime.startsWith("video/") ||
            mime.contains("application/mp4") ||
            mime.contains("octet-stream")
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return "Unknown size"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.2f MB", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
            else -> "$bytes B"
        }
    }

    fun formatDuration(seconds: Int): String {
        if (seconds <= 0) return ""
        val mins = seconds / 60
        val secs = seconds % 60
        val hrs = mins / 60
        return if (hrs > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hrs, mins % 60, secs)
        } else {
            String.format(Locale.US, "%d:%02d", mins, secs)
        }
    }
}
