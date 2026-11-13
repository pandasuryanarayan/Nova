package com.example.browser

import android.net.Uri
import android.webkit.WebResourceResponse
import com.example.data.AdBlockMode
import com.example.data.BlockedAdEntry
import java.io.ByteArrayInputStream
import java.util.Locale

data class AdFilterRuleItem(
    val rule: String,
    val category: String,
    val tier: AdBlockMode
)

object AdBlocker {

    private fun hostMap(raw: String): Map<String, Pair<String, String>> =
        linkedMapOf<String, Pair<String, String>>().apply {
            for (line in raw.lines()) {
                val sep = line.indexOf('|')
                if (sep > 0) {
                    val host = line.substring(0, sep)
                    val cat = line.substring(sep + 1)
                    put(host, cat to "||$host^")
                }
            }
        }

    private fun pathList(raw: String): List<Pair<String, Pair<String, String>>> =
        raw.lines().mapNotNull { line ->
            val p1 = line.indexOf('|')
            val p2 = if (p1 > 0) line.indexOf('|', p1 + 1) else -1
            if (p1 > 0 && p2 > p1) {
                line.substring(0, p1) to (line.substring(p1 + 1, p2) to line.substring(p2 + 1))
            } else null
        }

    private fun selectorList(raw: String): List<String> =
        raw.lines().filter { it.isNotEmpty() }

    // =========================================================================
    // 1. MINIMAL MODE RULES (Enabled by Default — 58 Total Rules)
    // =========================================================================

    val MINIMAL_HOST_RULES: Map<String, Pair<String, String>> = hostMap(
        """
doubleclick.net|Ad Network Script
googlesyndication.com|AdSense / Banner Ad
googleadservices.com|Conversion Ad Tracker
adservice.google.com|Ad Personalization
google-analytics.com|Analytics Tracker
googletagmanager.com|Tag Manager Tracker
adnxs.com|AppNexus Ad Exchange
taboola.com|Sponsored Content Ad
outbrain.com|Sponsored Feed Ad
criteo.com|Retargeting Ad
criteo.net|Retargeting Banner
scorecardresearch.com|Behavioral Tracker
adsafeprotected.com|Ad Verification Script
moatads.com|Ad Telemetry Script
rubiconproject.com|Programmatic Ad Bid
pubmatic.com|Header Bidding Ad
openx.net|OpenX Ad Server
popads.net|Pop-Under Ad Script
popcash.net|Pop-Up Ad Script
propellerads.com|Push / Pop Ad
adform.net|Display Ad Banner
amazon-adsystem.com|Amazon Ad Beacon
advertising.com|Display Ad Network
quantserve.com|Audience Tracker
zedo.com|Interstitial Ad
casalemedia.com|Index Exchange Ad
smartadserver.com|SmartAd Banner
2mdn.net|Rich Media Ad Asset
adcolony.com|Video Pre-Roll Ad
adsrvr.org|TheTradeDesk Ad
teads.tv|Outstream Video Ad
triplelift.com|Native Ad Placement
sharethrough.com|Native Ad Exchange
media.net|Contextual Ad Unit
mgid.com|Native Widget Ad
revcontent.com|Sponsored Widget Ad
exoclick.com|Banner / Pop Ad
hotjar.com|Session Recording Tracker
clarity.ms|Telemetry Tracker
connect.facebook.net|Social Pixel Tracker
        """.trimIndent()
    )

    val MINIMAL_PATH_PATTERNS: List<Pair<String, Pair<String, String>>> = pathList(
        """
/pagead/|AdSense Script|/pagead/*
/ads/ga-audiences|Audience Pixel|/ads/ga-audiences*
/adserver/|Ad Server Frame|/adserver/*
/adframe|Banner Ad Iframe|/adframe*
/ads.js|Ad Loader Script|*/ads.js
/prebid|Header Bidding Script|*/prebid*.js
/gpt.js|Google Publisher Tags|*/gpt.js
/adsbygoogle.js|AdSense Auto-Ads|*/adsbygoogle.js
        """.trimIndent()
    )

    val MINIMAL_DOM_SELECTORS: List<String> = selectorList(
        """
ins.adsbygoogle
[id^="div-gpt-ad"]
[id^="google_ads_iframe"]
iframe[src*="doubleclick.net"]
iframe[src*="googlesyndication.com"]
iframe[id*="google_ads"]
[class*="ad-banner"]
[class*="ad-slot"]
[data-ad-client]
[data-ad-slot]
        """.trimIndent()
    )

    // =========================================================================
    // 2. PRO MODE EXPANDED RULES (Disabled by Default — User Can Turn ON)
    // =========================================================================

    val PRO_HOST_RULES: Map<String, Pair<String, String>> = hostMap(
        """
adnxs-simple.com|Ad Exchange Network
adsafeprotected.com|Ad Verification Script
adroll.com|Retargeting Ad Network
adzerk.net|Native Ad Server
appier.net|AI Ad Retargeting
bidswitch.net|Programmatic Bid Switch
bidding.cam|Header Bidding Network
brealtime.com|Real-Time Ad Exchange
brighteroption.com|Ad Tracking Network
bannerflow.com|Display Banner Cloud
buysellads.com|Display Ad Marketplace
chromeads.com|Promotional Ad Script
conversantmedia.com|Personalized Ad Media
criteo.com|Retargeting Ad
deltax.ai|Ad Attribution Tracker
dlx.addirect.com|Direct Ad Exchange
eclickads.net|Click Ad Network
engagebdr.com|Programmatic Video/Display
epom.com|Epom Ad Server
exponential.com|Rich Media Ad Network
fimserve.com|Ad Serving Engine
flow-ad-blocker.com|Anti-Adblock / Ad Script
free-counter.co.uk|Tracking Pixel Counter
fuel451.com|Ad Campaign Tracker
gan.doubleclick.net|Google Affiliate Ad
getintent.com|Programmatic DSP Ad
go-serve.com|Ad Redirect Server
go2link.net|Affiliate Ad Redirect
grapeshot.co.uk|Contextual Ad Targeting
halogennetwork.com|Display Ad Network
hi.adnetpub.com|Publisher Ad Network
hit.sharehuts.com|Pop / Redirect Tracker
imads.intellitxt.com|In-Text Keyword Ad
indexww.com|Index Exchange Header Bid
infolinks.com|In-Text / Fold Ad
inhouse.adsharp.net|AdSharp Ad Server
jemax.com|Ad Network Script
kontera.com|In-Text Ad Link
liadm.com|LiveIntent Identity Tracker
live-adtech.com|AdTech Delivery Host
lockerdome.com|Native Widget Ad
ljmp.com|Ad Redirect Tracker
moatads.com|Ad Telemetry Script
native-adblock.com|Native Ad Injector
nextaday.com|Pop / Banner Ad
on.clkads.com|ClickAds Pop Network
onesignal.com|Push Notification Ad
openx.net|OpenX Ad Server
ox-d.com|OpenX Bid Exchange
pagefair.com|Ad Recovery Script
performatix.com|Performance Ad Tracker
pgdt.com|Ad Data Beacon
popads.net|Pop-Under Ad Script
popads.org|Pop-Under Ad Mirror
popcash.net|Pop-Up Ad Script
popmyads.com|Pop-Under Ad Network
propellerads.com|Push / Pop Ad
pushnami.com|Web Push Ad Network
pushcrew.com|Push Campaign Tracker
reallygoodarticles.com|Sponsored Content Redirect
revcontent.com|Sponsored Widget Ad
rlcdn.com|RapLeaf / TowerData Pixel
ronomaeheuh.com|Pop / Malware Ad Host
rtb-1.eekkqaa.com|RTB Bid Injector
rtbpop.com|RTB Pop-Under Ad
smartadserver.com|SmartAd Banner
smaato.net|Mobile Ad Exchange
sonobi.com|JetStream Header Bidding
spotxchange.com|Video Ad Exchange
steelhousemedia.com|Retargeting Ad Campaign
stroimfitness.ru|Spam Ad Redirect
syndication.exoclick.com|ExoClick Ad Syndication
teads.tv|Outstream Video Ad
texthit-service.com|In-Text Ad Service
trafficjunky.com|Banner Ad Network
trafficjunky.net|Banner Ad Delivery
traffichunt.com|Ad Exchange Network
tradetracker.net|Affiliate Ad Tracker
valueclick.com|Display Ad Network
videoplaza.com|Video Ad Server
vistapanel.net|Injected Hosting Ad
waveadvertising.com|Wave Display Ad
xd.rafgp.com|Pop / Redirect Script
yieldmo.com|Mobile Format Ad
yieldlab.net|Programmatic Yield Ad
zedo.com|Interstitial Ad
zestymedia.com|Display Ad Server
analytics.google.com|Google Analytics Endpoint
region1.google-analytics.com|Regional GA Telemetry
stats.g.doubleclick.net|DoubleClick Stats Beacon
mixpanel.com|Mixpanel Product Telemetry
segment.io|Segment Customer Data Pixel
amplitude.com|Amplitude Event Tracker
heapanalytics.com|Heap Session Analytics
fullstory.com|FullStory Session Replay
matomo.cloud|Matomo Cloud Analytics
statcounter.com|StatCounter Web Tracker
clicky.com|Clicky Web Analytics
chartbeat.com|Chartbeat Content Telemetry
parsely.com|Parse.ly Audience Tracker
crashlytics.com|Crash Telemetry Reporter
sentry.io|Sentry Telemetry Beacon
bugsnag.com|Bugsnag Telemetry Tracker
facebook.net|Meta Tracking Network
connect.facebook.net|Meta Pixel SDK
analytics.tiktok.com|TikTok Pixel Tracker
ads.linkedin.com|LinkedIn Ad Tracker
px.ads.linkedin.com|LinkedIn Insight Pixel
bat.bing.com|Bing UET Ad Conversion
snap.licdn.com|LinkedIn Snap Pixel
tr.snapchat.com|Snapchat Conversion Pixel
ads.twitter.com|X / Twitter Ad Tracker
static.ads-twitter.com|X / Twitter Universal Tag
        """.trimIndent()
    )

    val PRO_PATH_PATTERNS: List<Pair<String, Pair<String, String>>> = pathList(
        """
facebook.com/tr|Meta Tracking Pixel|||facebook.com/tr*
/pagead2/|Google PageAd2 Script|*/pagead2/*
/banner_ad|Banner Ad Asset|*/banner_ad*
/bannerad|BannerAd Asset|*/bannerad*
/banner-ads|Banner-Ads Asset|*/banner-ads*
/adserver/|AdServer Endpoint|*/adserver/*
/ad-server/|Ad-Server Endpoint|*/ad-server/*
/adserv/|AdServ Endpoint|*/adserv/*
/adframe/|AdFrame Path|*/adframe/*
/adframe.js|AdFrame Script|*/adframe.js
/adframe.php|AdFrame PHP Frame|*/adframe.php
/adcall/|AdCall Request|*/adcall/*
/adrouter/|AdRouter Endpoint|*/adrouter/*
/adproxy/|AdProxy Endpoint|*/adproxy/*
/afr.php|AdButler Frame|*/afr.php*
/delivery/ajs.php|Revive Adserver JS|*/delivery/ajs.php*
/delivery/lg.php|Revive Adserver Log|*/delivery/lg.php*
/ad.js|Generic Ad Script|*/ad.js
/ad2.js|Secondary Ad Script|*/ad2.js
/advert.js|Advert Script|*/advert.js
/advertisement.js|Advertisement Script|*/advertisement.js
/openx/www/delivery/|OpenX Delivery Path|*/openx/www/delivery/*
/api/adserver/|AdServer API|*/api/adserver/*
/sdk/ads/|Mobile Ads SDK Path|*/sdk/ads/*
/popunder|Pop-Under Script|*/popunder*
/pop-under|Pop-Under Loader|*/pop-under*
/popup.js|Pop-Up Script|*/popup.js
/apstag.js|Amazon TAM Header Bid|*/apstag.js
/gpt.js|Google Publisher Tags|*/gpt.js
/googletag.js|GoogleTag Ad Loader|*/googletag.js
/adsbygoogle.js|AdSense Auto-Ads|*/adsbygoogle.js
/show_ads.js|Legacy AdSense Loader|*/show_ads.js
/mediavine.js|Mediavine Ad Script|*/mediavine.js
/adthrive.js|AdThrive / Raptive Script|*/adthrive.js
/siteground-banner.js|SiteGround Promo Banner|*/siteground-banner.js
        """.trimIndent()
    )

    private val PRO_REGEX_PATTERNS: List<Pair<Regex, Pair<String, String>>> = listOf(
        Regex("""/vast[^/?#]*\.xml(\?|#|$)""", RegexOption.IGNORE_CASE) to ("VAST Video Ad XML" to "*/vast*.xml"),
        Regex("""/vmap[^/?#]*\.xml(\?|#|$)""", RegexOption.IGNORE_CASE) to ("VMAP Video Ad Schedule" to "*/vmap*.xml"),
        Regex("""/prebid[^/?#]*\.js(\?|#|$)""", RegexOption.IGNORE_CASE) to ("Prebid.js Header Bidding" to "*/prebid*.js"),
        Regex("""/pbjs[^/?#]*\.js(\?|#|$)""", RegexOption.IGNORE_CASE) to ("PBJS Header Bidding" to "*/pbjs*.js"),
        Regex("""/tag\.min\.js.*mediavine""", RegexOption.IGNORE_CASE) to ("Mediavine Tag Loader" to "*/tag.min.js*mediavine")
    )

    val PRO_DOM_SELECTORS: List<String> = selectorList(
        """
.ad
.ads
.adv
.advert
.adverts
.advertisement
.ad-container
.ad-wrapper
.ad-unit
.ad-box
.ad-holder
.ad-label
[id*="google_ads"]
[id*="taboola"]
[id*="outbrain"]
[id*="mgid"]
[class*="sponsored"]
[class*="Sponsored"]
[data-ad]
[data-ads]
[data-ad-unit]
[data-ad-slot]
[data-ad-client]
[data-google-query-id]
amp-ad
amp-embed
.adcode
.adsbox
.ad-badge
.text-ads
a[href*="doubleclick.net"]
a[href*="/adclick"]
ins.adsbygoogle
ins[data-ad-slot]
.widget-taboola
.OUTBRAIN
.ob-widget
.trc_rbox
.trc_rbox_div
iframe[src*="taboola"]
iframe[src*="outbrain"]
iframe[src*="mgid"]
        """.trimIndent()
    )

    // =========================================================================
    // 3. MAX MODE YOUTUBE & STREAMING RULES (Includes Minimal + Pro + YouTube)
    // =========================================================================

    val MAX_YOUTUBE_HOST_RULES: Map<String, Pair<String, String>> = hostMap(
        """
youtube.cleverads.vn|YouTube CleverAds Host
pagead2.googlesyndication.com|YouTube / Google PageAd2
googleads.g.doubleclick.net|YouTube DoubleClick Video Ad
tpc.googlesyndication.com|YouTube Syndication Ad Frame
securepubads.g.doubleclick.net|YouTube SecurePubAds
pubads.g.doubleclick.net|YouTube PubAds Server
adservice.google.com|YouTube AdService
        """.trimIndent()
    )

    val MAX_YOUTUBE_PATH_PATTERNS: List<Pair<String, Pair<String, String>>> = pathList(
        """
youtube.com/api/stats/ads|YouTube Ad Stats Beacon|youtube.com/api/stats/ads*
youtube.com/pagead/|YouTube PageAd Endpoint|youtube.com/pagead/*
youtube.com/ptracking|YouTube Ad Ptracking|youtube.com/ptracking*
youtube.com/pcs/activeview|YouTube ActiveView Ad Ping|youtube.com/pcs/activeview*
youtube.com/get_midroll_info|YouTube Midroll Ad Info|youtube.com/get_midroll_info*
/youtubei/v1/log_event|YouTube Telemetry Log Event|/youtubei/v1/log_event*
doubleclick.net/instream/ad_status|DoubleClick Instream Ad Status|doubleclick.net/instream/ad_status*
pagead2.googlesyndication.com/pagead/|PageAd2 Syndication Path|pagead2.googlesyndication.com/pagead/*
ytimg.com/ads-|YouTube Static Ad Asset|ytimg.com/ads-*
googlevideo.com/videogoodput|GoogleVideo Ad Goodput Verify|googlevideo.com/videogoodput*
        """.trimIndent()
    )

    val MAX_YOUTUBE_DOM_SELECTORS: List<String> = selectorList(
        """
.ytp-ad-module
.ytp-ad-overlay-container
.ytp-ad-text-overlay
.ytp-ad-image-overlay
.ytp-ad-message-container
.ytp-ad-player-overlay
.ytd-display-ad-renderer
.ytd-promoted-sparkles-web-renderer
.ytd-promoted-video-renderer
ytd-ad-slot-renderer
ytd-in-feed-ad-layout-renderer
ytd-compact-promoted-video-renderer
#masthead-ad
#player-ads
#background-promo-renderer
ytd-rich-item-renderer:has(ytd-display-ad-renderer)
        """.trimIndent()
    )

    // =========================================================================
    // RULE COUNTS & CATALOG HELPERS FOR UI
    // =========================================================================

    fun getRuleCountsForMode(mode: AdBlockMode): Triple<Int, Int, Int> {
        return when (mode) {
            AdBlockMode.MINIMAL -> Triple(
                MINIMAL_HOST_RULES.size,
                MINIMAL_PATH_PATTERNS.size,
                MINIMAL_DOM_SELECTORS.size
            )
            AdBlockMode.PRO -> {
                val combinedHosts = (MINIMAL_HOST_RULES.keys + PRO_HOST_RULES.keys).distinct().size
                val combinedPaths = (MINIMAL_PATH_PATTERNS.map { it.second.second } +
                    PRO_PATH_PATTERNS.map { it.second.second } +
                    PRO_REGEX_PATTERNS.map { it.second.second }).distinct().size
                val combinedDom = (MINIMAL_DOM_SELECTORS + PRO_DOM_SELECTORS).distinct().size
                Triple(combinedHosts, combinedPaths, combinedDom)
            }
            AdBlockMode.MAX -> {
                val combinedHosts = (MINIMAL_HOST_RULES.keys + PRO_HOST_RULES.keys + MAX_YOUTUBE_HOST_RULES.keys).distinct().size
                val combinedPaths = (MINIMAL_PATH_PATTERNS.map { it.second.second } +
                    PRO_PATH_PATTERNS.map { it.second.second } +
                    PRO_REGEX_PATTERNS.map { it.second.second } +
                    MAX_YOUTUBE_PATH_PATTERNS.map { it.second.second }).distinct().size
                val combinedDom = (MINIMAL_DOM_SELECTORS + PRO_DOM_SELECTORS + MAX_YOUTUBE_DOM_SELECTORS).distinct().size
                Triple(combinedHosts, combinedPaths, combinedDom)
            }
        }
    }

    fun getTotalRuleCount(mode: AdBlockMode): Int {
        val (hosts, paths, dom) = getRuleCountsForMode(mode)
        return hosts + paths + dom
    }

    fun getRulesCatalogForMode(mode: AdBlockMode): List<AdFilterRuleItem> {
        val list = mutableListOf<AdFilterRuleItem>()
        // Minimal
        MINIMAL_HOST_RULES.values.forEach { (cat, rule) ->
            list.add(AdFilterRuleItem(rule = rule, category = "Host • $cat", tier = AdBlockMode.MINIMAL))
        }
        MINIMAL_PATH_PATTERNS.forEach { (_, meta) ->
            list.add(AdFilterRuleItem(rule = meta.second, category = "Path • ${meta.first}", tier = AdBlockMode.MINIMAL))
        }
        MINIMAL_DOM_SELECTORS.forEach { sel ->
            list.add(AdFilterRuleItem(rule = "##$sel", category = "DOM • Cosmetic Selector", tier = AdBlockMode.MINIMAL))
        }

        if (mode == AdBlockMode.PRO || mode == AdBlockMode.MAX) {
            PRO_HOST_RULES.forEach { (host, meta) ->
                if (!MINIMAL_HOST_RULES.containsKey(host)) {
                    list.add(AdFilterRuleItem(rule = meta.second, category = "Pro Host • ${meta.first}", tier = AdBlockMode.PRO))
                }
            }
            PRO_PATH_PATTERNS.forEach { (_, meta) ->
                if (MINIMAL_PATH_PATTERNS.none { it.second.second == meta.second }) {
                    list.add(AdFilterRuleItem(rule = meta.second, category = "Pro Path • ${meta.first}", tier = AdBlockMode.PRO))
                }
            }
            PRO_REGEX_PATTERNS.forEach { (_, meta) ->
                list.add(AdFilterRuleItem(rule = meta.second, category = "Pro Pattern • ${meta.first}", tier = AdBlockMode.PRO))
            }
            PRO_DOM_SELECTORS.forEach { sel ->
                if (!MINIMAL_DOM_SELECTORS.contains(sel)) {
                    list.add(AdFilterRuleItem(rule = "##$sel", category = "Pro DOM • Cosmetic Slot", tier = AdBlockMode.PRO))
                }
            }
        }

        if (mode == AdBlockMode.MAX) {
            MAX_YOUTUBE_HOST_RULES.forEach { (_, meta) ->
                list.add(AdFilterRuleItem(rule = meta.second, category = "Max YouTube Host • ${meta.first}", tier = AdBlockMode.MAX))
            }
            MAX_YOUTUBE_PATH_PATTERNS.forEach { (_, meta) ->
                list.add(AdFilterRuleItem(rule = meta.second, category = "Max YouTube Path • ${meta.first}", tier = AdBlockMode.MAX))
            }
            MAX_YOUTUBE_DOM_SELECTORS.forEach { sel ->
                list.add(AdFilterRuleItem(rule = "##$sel", category = "Max YouTube DOM • Ad Renderer", tier = AdBlockMode.MAX))
            }
        }
        return list
    }

    // =========================================================================
    // NETWORK REQUEST INTERCEPTION MATCHER
    // =========================================================================

    fun isAdUrl(url: String, mode: AdBlockMode = AdBlockMode.MINIMAL): Boolean {
        return matchAdRequest("", url, "", mode) != null
    }

    private fun hit(
        tabId: String,
        url: String,
        host: String,
        pageDomain: String,
        meta: Pair<String, String>,
        mode: AdBlockMode
    ) = BlockedAdEntry(
        tabId = tabId,
        adUrl = url.take(260),
        adDomain = host,
        pageDomain = pageDomain,
        category = meta.first,
        filterRule = meta.second,
        modeTier = mode.shortBadge
    )

    fun matchAdRequest(
        tabId: String,
        url: String,
        pageUrl: String,
        mode: AdBlockMode = AdBlockMode.MINIMAL
    ): BlockedAdEntry? {
        val parsed = try {
            Uri.parse(url)
        } catch (_: Exception) {
            return null
        }
        val host = parsed.host?.lowercase(Locale.US) ?: return null
        val lowerUrl = url.lowercase(Locale.US)
        val pageDomain = BrowserUrlUtils.extractDomain(pageUrl.ifBlank { url })

        if (host.contains("recaptcha") ||
            host == "accounts.google.com" ||
            host == "www.gstatic.com" ||
            host == "ssl.gstatic.com" ||
            host == "fonts.gstatic.com" ||
            host == "encrypted-tbn0.gstatic.com"
        ) {
            return null
        }
        if ((host == "www.google.com" || host == "google.com") &&
            !lowerUrl.contains("/pagead/") &&
            !lowerUrl.contains("/aclk")
        ) {
            return null
        }
        if (host.endsWith("googlevideo.com") && !lowerUrl.contains("videogoodput")) {
            return null
        }

        if (mode == AdBlockMode.MAX) {
            for ((blockedHost, meta) in MAX_YOUTUBE_HOST_RULES) {
                if (host == blockedHost || host.endsWith(".$blockedHost")) {
                    return hit(tabId, url, host, pageDomain, meta, AdBlockMode.MAX)
                }
            }
            for ((pattern, meta) in MAX_YOUTUBE_PATH_PATTERNS) {
                if (lowerUrl.contains(pattern)) {
                    return hit(tabId, url, host, pageDomain, meta, AdBlockMode.MAX)
                }
            }
        }

        for ((blockedDomain, meta) in MINIMAL_HOST_RULES) {
            if (host == blockedDomain || host.endsWith(".$blockedDomain")) {
                return hit(tabId, url, host, pageDomain, meta, AdBlockMode.MINIMAL)
            }
        }

        if (mode == AdBlockMode.PRO || mode == AdBlockMode.MAX) {
            for ((blockedDomain, meta) in PRO_HOST_RULES) {
                if (host == blockedDomain || host.endsWith(".$blockedDomain")) {
                    return hit(tabId, url, host, pageDomain, meta, AdBlockMode.PRO)
                }
            }
        }

        for ((pattern, meta) in MINIMAL_PATH_PATTERNS) {
            if (lowerUrl.contains(pattern)) {
                return hit(tabId, url, host, pageDomain, meta, AdBlockMode.MINIMAL)
            }
        }

        if (mode == AdBlockMode.PRO || mode == AdBlockMode.MAX) {
            for ((pattern, meta) in PRO_PATH_PATTERNS) {
                if (lowerUrl.contains(pattern)) {
                    return hit(tabId, url, host, pageDomain, meta, AdBlockMode.PRO)
                }
            }
            for ((regex, meta) in PRO_REGEX_PATTERNS) {
                if (regex.containsMatchIn(lowerUrl)) {
                    return hit(tabId, url, host, pageDomain, meta, AdBlockMode.PRO)
                }
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

    // =========================================================================
    // COSMETIC DOM AD-SHIELD JS GENERATOR (MINIMAL / PRO / MAX)
    // =========================================================================

    fun buildCosmeticAdShieldJs(mode: AdBlockMode): String {
        val selectors = buildList {
            addAll(MINIMAL_DOM_SELECTORS)
            if (mode == AdBlockMode.PRO || mode == AdBlockMode.MAX) {
                addAll(PRO_DOM_SELECTORS)
            }
            if (mode == AdBlockMode.MAX) {
                addAll(MAX_YOUTUBE_DOM_SELECTORS)
            }
        }.distinct()

        val jsSelectorArray = selectors.joinToString(",") { sel ->
            "'" + sel.replace("\\", "\\\\").replace("'", "\\'") + "'"
        }

        val isMaxMode = mode == AdBlockMode.MAX
        val modeName = mode.name

        return """
            (function() {
              var currentHost = (window.location.hostname || '').toLowerCase();
              // Never run cosmetic ad stripping on Google Search or reCAPTCHA to preserve search trust
              if ((currentHost.indexOf('google.') !== -1 && currentHost.indexOf('youtube.') === -1) || currentHost.indexOf('recaptcha') !== -1) {
                return;
              }
              if (window.__novaAdShieldMode === '$modeName') return;
              window.__novaAdShieldMode = '$modeName';
              var reportedSelectors = window.__novaReportedDomSelectors || {};
              window.__novaReportedDomSelectors = reportedSelectors;

              var adSelectors = [$jsSelectorArray];

              // Inject persistent CSS stylesheet for instant zero-flicker hiding
              try {
                var styleId = '__nova_adshield_css__';
                var existingStyle = document.getElementById(styleId);
                if (!existingStyle && document.head) {
                  existingStyle = document.createElement('style');
                  existingStyle.id = styleId;
                  document.head.appendChild(existingStyle);
                }
                if (existingStyle) {
                  existingStyle.textContent = adSelectors.join(', ') + ' { display: none !important; height: 0px !important; min-height: 0px !important; pointer-events: none !important; }';
                }
              } catch (e) {}

              function sweepDomAds() {
                try {
                  for (var s = 0; s < adSelectors.length; s++) {
                    var sel = adSelectors[s];
                    var nodes;
                    try {
                      nodes = document.querySelectorAll(sel);
                    } catch (qErr) {
                      continue;
                    }
                    for (var i = 0; i < nodes.length; i++) {
                      var el = nodes[i];
                      if (!el || el.__novaAdHidden) continue;
                      el.__novaAdHidden = true;
                      el.style.setProperty('display', 'none', 'important');
                      el.style.setProperty('height', '0px', 'important');
                      var ruleKey = '##' + sel;
                      if (!reportedSelectors[ruleKey] && window.NovaVideoBridge && window.NovaVideoBridge.onDomAdBlocked) {
                        reportedSelectors[ruleKey] = true;
                        var srcUrl = el.getAttribute('src') || el.getAttribute('href') || el.getAttribute('data-ad-client') || (window.location.origin + '/' + ruleKey);
                        window.NovaVideoBridge.onDomAdBlocked(JSON.stringify({
                          adUrl: String(srcUrl),
                          adDomain: currentHost || 'dom-element',
                          pageDomain: currentHost || 'page',
                          category: ${if (isMaxMode) "currentHost.indexOf('youtube') !== -1 ? 'YouTube DOM Ad' : 'DOM Ad Container'" else "'DOM Ad Container'"},
                          filterRule: ruleKey
                        }));
                      }
                    }
                  }

                  // In MAX Mode on YouTube: automatically skip/fast-forward active video ad overlays
                  if (${if (isMaxMode) "true" else "false"} && currentHost.indexOf('youtube') !== -1) {
                    var skipBtns = document.querySelectorAll('.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .ytp-skip-ad-button, .ytp-ad-overlay-close-button');
                    for (var k = 0; k < skipBtns.length; k++) {
                      if (skipBtns[k] && typeof skipBtns[k].click === 'function') {
                        skipBtns[k].click();
                      }
                    }
                    var player = document.querySelector('.html5-video-player.ad-showing video, .html5-video-player.ad-interrupting video');
                    if (player && !isNaN(player.duration) && player.duration > 0 && player.duration < 180) {
                      player.currentTime = player.duration;
                    }
                  }
                } catch (e) {}
              }

              sweepDomAds();
              if (!window.__novaAdShieldInterval) {
                window.__novaAdShieldInterval = setInterval(sweepDomAds, 1800);
              }
            })();
        """.trimIndent()
    }
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
