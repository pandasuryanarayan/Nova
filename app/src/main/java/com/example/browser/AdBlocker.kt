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

    // =========================================================================
    // 1. MINIMAL MODE RULES (Enabled by Default — 58 Total Rules)
    // =========================================================================

    val MINIMAL_HOST_RULES: Map<String, Pair<String, String>> = linkedMapOf(
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

    val MINIMAL_PATH_PATTERNS: List<Pair<String, Pair<String, String>>> = listOf(
        "/pagead/" to ("AdSense Script" to "/pagead/*"),
        "/ads/ga-audiences" to ("Audience Pixel" to "/ads/ga-audiences*"),
        "/adserver/" to ("Ad Server Frame" to "/adserver/*"),
        "/adframe" to ("Banner Ad Iframe" to "/adframe*"),
        "/ads.js" to ("Ad Loader Script" to "*/ads.js"),
        "/prebid" to ("Header Bidding Script" to "*/prebid*.js"),
        "/gpt.js" to ("Google Publisher Tags" to "*/gpt.js"),
        "/adsbygoogle.js" to ("AdSense Auto-Ads" to "*/adsbygoogle.js")
    )

    val MINIMAL_DOM_SELECTORS: List<String> = listOf(
        "ins.adsbygoogle",
        "[id^=\"div-gpt-ad\"]",
        "[id^=\"google_ads_iframe\"]",
        "iframe[src*=\"doubleclick.net\"]",
        "iframe[src*=\"googlesyndication.com\"]",
        "iframe[id*=\"google_ads\"]",
        "[class*=\"ad-banner\"]",
        "[class*=\"ad-slot\"]",
        "[data-ad-client]",
        "[data-ad-slot]"
    )

    // =========================================================================
    // 2. PRO MODE EXPANDED RULES (Disabled by Default — User Can Turn ON)
    // =========================================================================

    val PRO_HOST_RULES: Map<String, Pair<String, String>> = linkedMapOf(
        // Expanded Ad Networks
        "adnxs-simple.com" to ("Ad Exchange Network" to "||adnxs-simple.com^"),
        "adsafeprotected.com" to ("Ad Verification Script" to "||adsafeprotected.com^"),
        "adroll.com" to ("Retargeting Ad Network" to "||adroll.com^"),
        "adzerk.net" to ("Native Ad Server" to "||adzerk.net^"),
        "appier.net" to ("AI Ad Retargeting" to "||appier.net^"),
        "bidswitch.net" to ("Programmatic Bid Switch" to "||bidswitch.net^"),
        "bidding.cam" to ("Header Bidding Network" to "||bidding.cam^"),
        "brealtime.com" to ("Real-Time Ad Exchange" to "||brealtime.com^"),
        "brighteroption.com" to ("Ad Tracking Network" to "||brighteroption.com^"),
        "bannerflow.com" to ("Display Banner Cloud" to "||bannerflow.com^"),
        "buysellads.com" to ("Display Ad Marketplace" to "||buysellads.com^"),
        "chromeads.com" to ("Promotional Ad Script" to "||chromeads.com^"),
        "conversantmedia.com" to ("Personalized Ad Media" to "||conversantmedia.com^"),
        "criteo.com" to ("Retargeting Ad" to "||criteo.com^"),
        "deltax.ai" to ("Ad Attribution Tracker" to "||deltax.ai^"),
        "dlx.addirect.com" to ("Direct Ad Exchange" to "||dlx.addirect.com^"),
        "eclickads.net" to ("Click Ad Network" to "||eclickads.net^"),
        "engagebdr.com" to ("Programmatic Video/Display" to "||engagebdr.com^"),
        "epom.com" to ("Epom Ad Server" to "||epom.com^"),
        "exponential.com" to ("Rich Media Ad Network" to "||exponential.com^"),
        "fimserve.com" to ("Ad Serving Engine" to "||fimserve.com^"),
        "flow-ad-blocker.com" to ("Anti-Adblock / Ad Script" to "||flow-ad-blocker.com^"),
        "free-counter.co.uk" to ("Tracking Pixel Counter" to "||free-counter.co.uk^"),
        "fuel451.com" to ("Ad Campaign Tracker" to "||fuel451.com^"),
        "gan.doubleclick.net" to ("Google Affiliate Ad" to "||gan.doubleclick.net^"),
        "getintent.com" to ("Programmatic DSP Ad" to "||getintent.com^"),
        "go-serve.com" to ("Ad Redirect Server" to "||go-serve.com^"),
        "go2link.net" to ("Affiliate Ad Redirect" to "||go2link.net^"),
        "grapeshot.co.uk" to ("Contextual Ad Targeting" to "||grapeshot.co.uk^"),
        "halogennetwork.com" to ("Display Ad Network" to "||halogennetwork.com^"),
        "hi.adnetpub.com" to ("Publisher Ad Network" to "||hi.adnetpub.com^"),
        "hit.sharehuts.com" to ("Pop / Redirect Tracker" to "||hit.sharehuts.com^"),
        "imads.intellitxt.com" to ("In-Text Keyword Ad" to "||imads.intellitxt.com^"),
        "indexww.com" to ("Index Exchange Header Bid" to "||indexww.com^"),
        "infolinks.com" to ("In-Text / Fold Ad" to "||infolinks.com^"),
        "inhouse.adsharp.net" to ("AdSharp Ad Server" to "||inhouse.adsharp.net^"),
        "jemax.com" to ("Ad Network Script" to "||jemax.com^"),
        "kontera.com" to ("In-Text Ad Link" to "||kontera.com^"),
        "liadm.com" to ("LiveIntent Identity Tracker" to "||liadm.com^"),
        "live-adtech.com" to ("AdTech Delivery Host" to "||live-adtech.com^"),
        "lockerdome.com" to ("Native Widget Ad" to "||lockerdome.com^"),
        "ljmp.com" to ("Ad Redirect Tracker" to "||ljmp.com^"),
        "moatads.com" to ("Ad Telemetry Script" to "||moatads.com^"),
        "native-adblock.com" to ("Native Ad Injector" to "||native-adblock.com^"),
        "nextaday.com" to ("Pop / Banner Ad" to "||nextaday.com^"),
        "on.clkads.com" to ("ClickAds Pop Network" to "||on.clkads.com^"),
        "onesignal.com" to ("Push Notification Ad" to "||onesignal.com^"),
        "openx.net" to ("OpenX Ad Server" to "||openx.net^"),
        "ox-d.com" to ("OpenX Bid Exchange" to "||ox-d.com^"),
        "pagefair.com" to ("Ad Recovery Script" to "||pagefair.com^"),
        "performatix.com" to ("Performance Ad Tracker" to "||performatix.com^"),
        "pgdt.com" to ("Ad Data Beacon" to "||pgdt.com^"),
        "popads.net" to ("Pop-Under Ad Script" to "||popads.net^"),
        "popads.org" to ("Pop-Under Ad Mirror" to "||popads.org^"),
        "popcash.net" to ("Pop-Up Ad Script" to "||popcash.net^"),
        "popmyads.com" to ("Pop-Under Ad Network" to "||popmyads.com^"),
        "propellerads.com" to ("Push / Pop Ad" to "||propellerads.com^"),
        "pushnami.com" to ("Web Push Ad Network" to "||pushnami.com^"),
        "pushcrew.com" to ("Push Campaign Tracker" to "||pushcrew.com^"),
        "reallygoodarticles.com" to ("Sponsored Content Redirect" to "||reallygoodarticles.com^"),
        "revcontent.com" to ("Sponsored Widget Ad" to "||revcontent.com^"),
        "rlcdn.com" to ("RapLeaf / TowerData Pixel" to "||rlcdn.com^"),
        "ronomaeheuh.com" to ("Pop / Malware Ad Host" to "||ronomaeheuh.com^"),
        "rtb-1.eekkqaa.com" to ("RTB Bid Injector" to "||rtb-1.eekkqaa.com^"),
        "rtbpop.com" to ("RTB Pop-Under Ad" to "||rtbpop.com^"),
        "smartadserver.com" to ("SmartAd Banner" to "||smartadserver.com^"),
        "smaato.net" to ("Mobile Ad Exchange" to "||smaato.net^"),
        "sonobi.com" to ("JetStream Header Bidding" to "||sonobi.com^"),
        "spotxchange.com" to ("Video Ad Exchange" to "||spotxchange.com^"),
        "steelhousemedia.com" to ("Retargeting Ad Campaign" to "||steelhousemedia.com^"),
        "stroimfitness.ru" to ("Spam Ad Redirect" to "||stroimfitness.ru^"),
        "syndication.exoclick.com" to ("ExoClick Ad Syndication" to "||syndication.exoclick.com^"),
        "teads.tv" to ("Outstream Video Ad" to "||teads.tv^"),
        "texthit-service.com" to ("In-Text Ad Service" to "||texthit-service.com^"),
        "trafficjunky.com" to ("Banner Ad Network" to "||trafficjunky.com^"),
        "trafficjunky.net" to ("Banner Ad Delivery" to "||trafficjunky.net^"),
        "traffichunt.com" to ("Ad Exchange Network" to "||traffichunt.com^"),
        "tradetracker.net" to ("Affiliate Ad Tracker" to "||tradetracker.net^"),
        "valueclick.com" to ("Display Ad Network" to "||valueclick.com^"),
        "videoplaza.com" to ("Video Ad Server" to "||videoplaza.com^"),
        "vistapanel.net" to ("Injected Hosting Ad" to "||vistapanel.net^"),
        "waveadvertising.com" to ("Wave Display Ad" to "||waveadvertising.com^"),
        "xd.rafgp.com" to ("Pop / Redirect Script" to "||xd.rafgp.com^"),
        "yieldmo.com" to ("Mobile Format Ad" to "||yieldmo.com^"),
        "yieldlab.net" to ("Programmatic Yield Ad" to "||yieldlab.net^"),
        "zedo.com" to ("Interstitial Ad" to "||zedo.com^"),
        "zestymedia.com" to ("Display Ad Server" to "||zestymedia.com^"),

        // Analytics / Telemetry Expansion
        "analytics.google.com" to ("Google Analytics Endpoint" to "||analytics.google.com^"),
        "region1.google-analytics.com" to ("Regional GA Telemetry" to "||region1.google-analytics.com^"),
        "stats.g.doubleclick.net" to ("DoubleClick Stats Beacon" to "||stats.g.doubleclick.net^"),
        "mixpanel.com" to ("Mixpanel Product Telemetry" to "||mixpanel.com^"),
        "segment.io" to ("Segment Customer Data Pixel" to "||segment.io^"),
        "amplitude.com" to ("Amplitude Event Tracker" to "||amplitude.com^"),
        "heapanalytics.com" to ("Heap Session Analytics" to "||heapanalytics.com^"),
        "fullstory.com" to ("FullStory Session Replay" to "||fullstory.com^"),
        "matomo.cloud" to ("Matomo Cloud Analytics" to "||matomo.cloud^"),
        "statcounter.com" to ("StatCounter Web Tracker" to "||statcounter.com^"),
        "clicky.com" to ("Clicky Web Analytics" to "||clicky.com^"),
        "chartbeat.com" to ("Chartbeat Content Telemetry" to "||chartbeat.com^"),
        "parsely.com" to ("Parse.ly Audience Tracker" to "||parsely.com^"),
        "crashlytics.com" to ("Crash Telemetry Reporter" to "||crashlytics.com^"),
        "sentry.io" to ("Sentry Telemetry Beacon" to "||sentry.io^"),
        "bugsnag.com" to ("Bugsnag Telemetry Tracker" to "||bugsnag.com^"),

        // Facebook / Meta & Tracking Pixels
        "facebook.net" to ("Meta Tracking Network" to "||facebook.net^"),
        "connect.facebook.net" to ("Meta Pixel SDK" to "||connect.facebook.net^"),
        "analytics.tiktok.com" to ("TikTok Pixel Tracker" to "||analytics.tiktok.com^"),
        "ads.linkedin.com" to ("LinkedIn Ad Tracker" to "||ads.linkedin.com^"),
        "px.ads.linkedin.com" to ("LinkedIn Insight Pixel" to "||px.ads.linkedin.com^"),
        "bat.bing.com" to ("Bing UET Ad Conversion" to "||bat.bing.com^"),
        "snap.licdn.com" to ("LinkedIn Snap Pixel" to "||snap.licdn.com^"),
        "tr.snapchat.com" to ("Snapchat Conversion Pixel" to "||tr.snapchat.com^"),
        "ads.twitter.com" to ("X / Twitter Ad Tracker" to "||ads.twitter.com^"),
        "static.ads-twitter.com" to ("X / Twitter Universal Tag" to "||static.ads-twitter.com^")
    )

    val PRO_PATH_PATTERNS: List<Pair<String, Pair<String, String>>> = listOf(
        "facebook.com/tr" to ("Meta Tracking Pixel" to "||facebook.com/tr*"),
        "/pagead2/" to ("Google PageAd2 Script" to "*/pagead2/*"),
        "/banner_ad" to ("Banner Ad Asset" to "*/banner_ad*"),
        "/bannerad" to ("BannerAd Asset" to "*/bannerad*"),
        "/banner-ads" to ("Banner-Ads Asset" to "*/banner-ads*"),
        "/adserver/" to ("AdServer Endpoint" to "*/adserver/*"),
        "/ad-server/" to ("Ad-Server Endpoint" to "*/ad-server/*"),
        "/adserv/" to ("AdServ Endpoint" to "*/adserv/*"),
        "/adframe/" to ("AdFrame Path" to "*/adframe/*"),
        "/adframe.js" to ("AdFrame Script" to "*/adframe.js"),
        "/adframe.php" to ("AdFrame PHP Frame" to "*/adframe.php"),
        "/adcall/" to ("AdCall Request" to "*/adcall/*"),
        "/adrouter/" to ("AdRouter Endpoint" to "*/adrouter/*"),
        "/adproxy/" to ("AdProxy Endpoint" to "*/adproxy/*"),
        "/afr.php" to ("AdButler Frame" to "*/afr.php*"),
        "/delivery/ajs.php" to ("Revive Adserver JS" to "*/delivery/ajs.php*"),
        "/delivery/lg.php" to ("Revive Adserver Log" to "*/delivery/lg.php*"),
        "/ad.js" to ("Generic Ad Script" to "*/ad.js"),
        "/ad2.js" to ("Secondary Ad Script" to "*/ad2.js"),
        "/advert.js" to ("Advert Script" to "*/advert.js"),
        "/advertisement.js" to ("Advertisement Script" to "*/advertisement.js"),
        "/openx/www/delivery/" to ("OpenX Delivery Path" to "*/openx/www/delivery/*"),
        "/api/adserver/" to ("AdServer API" to "*/api/adserver/*"),
        "/sdk/ads/" to ("Mobile Ads SDK Path" to "*/sdk/ads/*"),
        "/popunder" to ("Pop-Under Script" to "*/popunder*"),
        "/pop-under" to ("Pop-Under Loader" to "*/pop-under*"),
        "/popup.js" to ("Pop-Up Script" to "*/popup.js"),
        "/apstag.js" to ("Amazon TAM Header Bid" to "*/apstag.js"),
        "/gpt.js" to ("Google Publisher Tags" to "*/gpt.js"),
        "/googletag.js" to ("GoogleTag Ad Loader" to "*/googletag.js"),
        "/adsbygoogle.js" to ("AdSense Auto-Ads" to "*/adsbygoogle.js"),
        "/show_ads.js" to ("Legacy AdSense Loader" to "*/show_ads.js"),
        "/mediavine.js" to ("Mediavine Ad Script" to "*/mediavine.js"),
        "/adthrive.js" to ("AdThrive / Raptive Script" to "*/adthrive.js"),
        "/siteground-banner.js" to ("SiteGround Promo Banner" to "*/siteground-banner.js")
    )

    private val PRO_REGEX_PATTERNS: List<Pair<Regex, Pair<String, String>>> = listOf(
        Regex("""/vast[^/?#]*\.xml(\?|#|$)""", RegexOption.IGNORE_CASE) to ("VAST Video Ad XML" to "*/vast*.xml"),
        Regex("""/vmap[^/?#]*\.xml(\?|#|$)""", RegexOption.IGNORE_CASE) to ("VMAP Video Ad Schedule" to "*/vmap*.xml"),
        Regex("""/prebid[^/?#]*\.js(\?|#|$)""", RegexOption.IGNORE_CASE) to ("Prebid.js Header Bidding" to "*/prebid*.js"),
        Regex("""/pbjs[^/?#]*\.js(\?|#|$)""", RegexOption.IGNORE_CASE) to ("PBJS Header Bidding" to "*/pbjs*.js"),
        Regex("""/tag\.min\.js.*mediavine""", RegexOption.IGNORE_CASE) to ("Mediavine Tag Loader" to "*/tag.min.js*mediavine")
    )

    val PRO_DOM_SELECTORS: List<String> = listOf(
        ".ad", ".ads", ".adv", ".advert", ".adverts", ".advertisement",
        ".ad-container", ".ad-wrapper", ".ad-unit", ".ad-box", ".ad-holder", ".ad-label",
        "[id*=\"google_ads\"]", "[id*=\"taboola\"]", "[id*=\"outbrain\"]", "[id*=\"mgid\"]",
        "[class*=\"sponsored\"]", "[class*=\"Sponsored\"]", "[data-ad]", "[data-ads]",
        "[data-ad-unit]", "[data-ad-slot]", "[data-ad-client]", "[data-google-query-id]",
        "amp-ad", "amp-embed", ".adcode", ".adsbox", ".ad-badge", ".text-ads",
        "a[href*=\"doubleclick.net\"]", "a[href*=\"/adclick\"]",
        "ins.adsbygoogle", "ins[data-ad-slot]",
        ".widget-taboola", ".OUTBRAIN", ".ob-widget", ".trc_rbox", ".trc_rbox_div",
        "iframe[src*=\"taboola\"]", "iframe[src*=\"outbrain\"]", "iframe[src*=\"mgid\"]"
    )

    // =========================================================================
    // 3. MAX MODE YOUTUBE & STREAMING RULES (Includes Minimal + Pro + YouTube)
    // =========================================================================

    val MAX_YOUTUBE_HOST_RULES: Map<String, Pair<String, String>> = linkedMapOf(
        "youtube.cleverads.vn" to ("YouTube CleverAds Host" to "||youtube.cleverads.vn^"),
        "pagead2.googlesyndication.com" to ("YouTube / Google PageAd2" to "||pagead2.googlesyndication.com^"),
        "googleads.g.doubleclick.net" to ("YouTube DoubleClick Video Ad" to "||googleads.g.doubleclick.net^"),
        "tpc.googlesyndication.com" to ("YouTube Syndication Ad Frame" to "||tpc.googlesyndication.com^"),
        "securepubads.g.doubleclick.net" to ("YouTube SecurePubAds" to "||securepubads.g.doubleclick.net^"),
        "pubads.g.doubleclick.net" to ("YouTube PubAds Server" to "||pubads.g.doubleclick.net^"),
        "adservice.google.com" to ("YouTube AdService" to "||adservice.google.com^")
    )

    val MAX_YOUTUBE_PATH_PATTERNS: List<Pair<String, Pair<String, String>>> = listOf(
        "youtube.com/api/stats/ads" to ("YouTube Ad Stats Beacon" to "youtube.com/api/stats/ads*"),
        "youtube.com/pagead/" to ("YouTube PageAd Endpoint" to "youtube.com/pagead/*"),
        "youtube.com/ptracking" to ("YouTube Ad Ptracking" to "youtube.com/ptracking*"),
        "youtube.com/pcs/activeview" to ("YouTube ActiveView Ad Ping" to "youtube.com/pcs/activeview*"),
        "youtube.com/get_midroll_info" to ("YouTube Midroll Ad Info" to "youtube.com/get_midroll_info*"),
        "/youtubei/v1/log_event" to ("YouTube Telemetry Log Event" to "/youtubei/v1/log_event*"),
        "doubleclick.net/instream/ad_status" to ("DoubleClick Instream Ad Status" to "doubleclick.net/instream/ad_status*"),
        "pagead2.googlesyndication.com/pagead/" to ("PageAd2 Syndication Path" to "pagead2.googlesyndication.com/pagead/*"),
        "ytimg.com/ads-" to ("YouTube Static Ad Asset" to "ytimg.com/ads-*"),
        "googlevideo.com/videogoodput" to ("GoogleVideo Ad Goodput Verify" to "googlevideo.com/videogoodput*")
    )

    val MAX_YOUTUBE_DOM_SELECTORS: List<String> = listOf(
        ".ytp-ad-module",
        ".ytp-ad-overlay-container",
        ".ytp-ad-text-overlay",
        ".ytp-ad-image-overlay",
        ".ytp-ad-message-container",
        ".ytp-ad-player-overlay",
        ".ytd-display-ad-renderer",
        ".ytd-promoted-sparkles-web-renderer",
        ".ytd-promoted-video-renderer",
        "ytd-ad-slot-renderer",
        "ytd-in-feed-ad-layout-renderer",
        "ytd-compact-promoted-video-renderer",
        "#masthead-ad",
        "#player-ads",
        "#background-promo-renderer",
        "ytd-rich-item-renderer:has(ytd-display-ad-renderer)"
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

        // 0. Never block reCAPTCHA, Google Account Auth, or first-party Google Search results/static assets
        //    so Google Search never triggers CAPTCHA / bot challenges.
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
        // Never block actual YouTube video media streams (googlevideo.com/videoplayback)
        if (host.endsWith("googlevideo.com") && !lowerUrl.contains("videogoodput")) {
            return null
        }

        // 1. Check MAX Mode YouTube Rules first when mode == MAX
        if (mode == AdBlockMode.MAX) {
            for ((blockedHost, meta) in MAX_YOUTUBE_HOST_RULES) {
                if (host == blockedHost || host.endsWith(".$blockedHost")) {
                    return BlockedAdEntry(
                        tabId = tabId,
                        adUrl = url.take(260),
                        adDomain = host,
                        pageDomain = pageDomain,
                        category = meta.first,
                        filterRule = meta.second,
                        modeTier = AdBlockMode.MAX.shortBadge
                    )
                }
            }
            for ((pattern, meta) in MAX_YOUTUBE_PATH_PATTERNS) {
                if (lowerUrl.contains(pattern)) {
                    return BlockedAdEntry(
                        tabId = tabId,
                        adUrl = url.take(260),
                        adDomain = host,
                        pageDomain = pageDomain,
                        category = meta.first,
                        filterRule = meta.second,
                        modeTier = AdBlockMode.MAX.shortBadge
                    )
                }
            }
        }

        // 2. Check MINIMAL Host Rules (Active in MINIMAL, PRO, and MAX)
        for ((blockedDomain, meta) in MINIMAL_HOST_RULES) {
            if (host == blockedDomain || host.endsWith(".$blockedDomain")) {
                return BlockedAdEntry(
                    tabId = tabId,
                    adUrl = url.take(260),
                    adDomain = host,
                    pageDomain = pageDomain,
                    category = meta.first,
                    filterRule = meta.second,
                    modeTier = AdBlockMode.MINIMAL.shortBadge
                )
            }
        }

        // 3. Check PRO Host Rules when mode is PRO or MAX
        if (mode == AdBlockMode.PRO || mode == AdBlockMode.MAX) {
            for ((blockedDomain, meta) in PRO_HOST_RULES) {
                if (host == blockedDomain || host.endsWith(".$blockedDomain")) {
                    return BlockedAdEntry(
                        tabId = tabId,
                        adUrl = url.take(260),
                        adDomain = host,
                        pageDomain = pageDomain,
                        category = meta.first,
                        filterRule = meta.second,
                        modeTier = AdBlockMode.PRO.shortBadge
                    )
                }
            }
        }

        // 4. Check MINIMAL Path / Script Rules (Active in MINIMAL, PRO, and MAX)
        for ((pattern, meta) in MINIMAL_PATH_PATTERNS) {
            if (lowerUrl.contains(pattern)) {
                return BlockedAdEntry(
                    tabId = tabId,
                    adUrl = url.take(260),
                    adDomain = host,
                    pageDomain = pageDomain,
                    category = meta.first,
                    filterRule = meta.second,
                    modeTier = AdBlockMode.MINIMAL.shortBadge
                )
            }
        }

        // 5. Check PRO Path & Regex Rules when mode is PRO or MAX
        if (mode == AdBlockMode.PRO || mode == AdBlockMode.MAX) {
            for ((pattern, meta) in PRO_PATH_PATTERNS) {
                if (lowerUrl.contains(pattern)) {
                    return BlockedAdEntry(
                        tabId = tabId,
                        adUrl = url.take(260),
                        adDomain = host,
                        pageDomain = pageDomain,
                        category = meta.first,
                        filterRule = meta.second,
                        modeTier = AdBlockMode.PRO.shortBadge
                    )
                }
            }
            for ((regex, meta) in PRO_REGEX_PATTERNS) {
                if (regex.containsMatchIn(lowerUrl)) {
                    return BlockedAdEntry(
                        tabId = tabId,
                        adUrl = url.take(260),
                        adDomain = host,
                        pageDomain = pageDomain,
                        category = meta.first,
                        filterRule = meta.second,
                        modeTier = AdBlockMode.PRO.shortBadge
                    )
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
