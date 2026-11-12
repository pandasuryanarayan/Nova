# Nova Browser — Fast, Private & Ad-Free Android Web Browser

<p align="center">
  <strong>Open-Source Android Browser built with Kotlin, Jetpack Compose (Material 3), Room, DataStore & Multi-Tier Ad-Block Shield</strong><br/>
  <code>Package ID: com.suryatech.browser.nova</code> • <code>Version: 6.6.0 (versionCode 12)</code> • <code>License: GNU GPL v3.0</code>
</p>

---

## Overview

**Nova Browser** is a lightweight, privacy-first, open-source Android web browser engineered in 100% **Kotlin** and **Jetpack Compose (Material Design 3)**. It combines a **3-tier Ad-Blocker & Tracker Shield (`Minimal`, `Pro`, `Max`)**, **True Incognito WebView Isolation**, **Multi-Threaded Segmented Download Manager**, **HTML5 Video Stream Sniffer & Background Audio Player**, and **Desktop-Grade Split-Screen DevTools** inside a **~2.39 MB** R8-shrunk release binary.

---

## Key Features

### 1. 3-Tier Ad-Blocker & Tracker Shield (`Minimal` • `Pro` • `Max`)
Nova Browser intercepts ads, trackers, telemetry beacons, and DOM banners at two layers:
1. **Network Interception Layer (`WebViewClient.shouldInterceptRequest`)**: Blocks ad scripts, tracking pixels, RTB auctions, VAST/VMAP video ads, and telemetry calls before a single byte is downloaded, returning an empty `204 No Content` response off the UI thread.
2. **Cosmetic DOM & Player Shield (`evaluateJavascript` + `MutationObserver`)**: Injects high-specificity CSS rules (`display: none !important`) and a throttled `MutationObserver` to strip ad slots, sponsored widgets, and YouTube in-player overlays.

| Mode Tier | Default State | Total Unique Rules | Rule Breakdown | Target Use Case |
| :--- | :--- | :--- | :--- | :--- |
| **Minimal Mode** | **Enabled by Default** | **58 Rules** | `40` Host + `8` Path + `10` Cosmetic DOM | Everyday ultra-fast browsing with core ad & analytics blocking |
| **Pro Mode** | **Disabled** (1-Tap Toggle) | **189 Rules** | `114` Host + `37` Path/Regex + `38` Cosmetic DOM | Aggressive blocking of 80+ ad networks, analytics suites, social pixels, Prebid/TAM/GPT scripts & native widgets |
| **Max Mode** | **Disabled** (1-Tap Toggle) | **220 Rules** | `119` Host + `47` Path/Regex + `54` Cosmetic DOM + Auto-Skip | Full **Pro** protection + **YouTube Ad/Tracking Hosts, Path Filters, Cosmetic Selectors & Video Ad Auto-Skipper** |

Users can switch between **Minimal (Default)**, **Pro**, and **Max** at any time from:
- The **Home Screen** (`Ads • Minimal / Pro / Max` split button below the search bar)
- The **Browser Menu Sheet** (`Ad-Block Shield` 3-pill selector)
- The **Blocked Ads & Filter Modes Sheet** (includes a live **Rules Inspector** tab showing all `58` / `189` / `220` active rules)

---

### 2. Dual-Theme Architecture & Smooth Radial Wave Transition
- **Default Standard Theme (Warm Light Orange)**: Clean, high-contrast warm alabaster & vibrant amber-orange (`#E65100` / `#FF8A3D`) palette for comfortable everyday browsing.
- **Incognito Private Theme (Dark Stealth Cyan)**: Deep obsidian surfaces (`#070B14`) with electric cyan (`#00E5FF`) accents.
- **Full-Screen Radial Wave Animation**: Toggling Incognito ON or OFF triggers a smooth 420ms color-scheme transition accompanied by a canvas radial wave animation across the entire app.
- **True Incognito Isolation**: Incognito tabs never write browsing history, cookies, form data, or DOM storage to disk; closing the last Incognito tab wipes WebView caches and session cookies immediately.

---

### 3. Trusted Google Search & Anti-Bot Fingerprinting Protection
- **Trusted Mobile Chrome User-Agent**: Matches standard Android Chrome token formatting without custom bot tokens (`NovaBrowser/...`) that trigger Google Search CAPTCHAs.
- **Navigator & Client-Hints Hardening**: Normalizes `navigator.webdriver = false`, `navigator.languages`, `navigator.plugins`, and `window.chrome` properties at document start while preserving first-party cookies and sending `Sec-GPC: 1` / `DNT: 1` only on third-party cross-site requests.

---

### 4. Smart On-Visit Website Favicon Cache
- Quick Shortcuts on the Home Screen display a crisp monogram fallback by default and **never poll the network continuously**.
- When a user actually opens a website, `ShortcutIconCacheManager` fetches the site's high-resolution icon once in the background, downscales it to a `96×96` WEBP file on disk (`filesDir/shortcut_icons/`) backed by a tiny 1.5 MB RAM `LruCache`, and reuses it offline thereafter.
- Every default or custom Quick Shortcut card includes a **3-dot (`⋮`) menu** to **Edit** (Title & URL) or **Delete** any shortcut individually.

---

### 5. Top Omnibox & Compact Site Info Dropdown
- **Aligned Site Info Dropdown**: Tapping the top URL bar while browsing opens a clean dropdown where the **SSL Security Badge** and **Website Link** start at a consistent `16.dp` left inset, paired with compact icon-only **Copy**, **Edit**, and **Share** actions on the right.
- **Standard End-of-URL Cursor Placement**: Tapping **Edit** opens the inline URL editor with the text cursor automatically positioned at the **end of the URL** (`TextRange(url.length)`) for immediate editing or backspacing.

---

### 6. Built-In Desktop-Grade DevTools, Video Sniffer & Segmented Downloader
- **Split-Screen DevTools**: Inspect **Elements** (interactive DOM tree & live attribute/text editing), **Sources**, **Console** (live JS REPL + log levels), and **Network** (method, status code, MIME type, timing).
- **HTML5 / HLS / MP4 Video Sniffer & Background Audio Player**: Detects `<video>`, `.mp4`, `.m3u8`, and `.webm` streams on the active page for one-tap MP4 download or background audio playback with a persistent mini-controller.
- **Multi-Connection Download Manager**: Supports HTTP `Range` pause/resume, live progress tracking, and Room-backed persistence.

---

## Complete Ad-Blocker Filter Reference (`AdBlocker.kt`)

### Tier 1 — Minimal Mode (Enabled by Default • `58` Filters)

#### A. Minimal Network Host Filters (`40` Domains)
**Ad Networks (`26`):**
`doubleclick.net`, `googlesyndication.com`, `googleadservices.com`, `adservice.google.com`, `ads.Exoclick.com`, `exoclick.com`, `adnxs.com`, `advertising.com`, `taboola.com`, `outbrain.com`, `criteo.com`, `criteo.net`, `pubmatic.com`, `rubiconproject.com`, `openx.net`, `casalemedia.com`, `amazon-adsystem.com`, `adcolony.com`, `applovin.com`, `unityads.unity3d.com`, `vungle.com`, `chartboost.com`, `ironsrc.com`, `inmobi.com`, `medianet.com`, `media.net`

**Analytics, Trackers & Telemetry (`14`):**
`google-analytics.com`, `googletagmanager.com`, `googletagservices.com`, `scorecardresearch.com`, `quantserve.com`, `zedo.com`, `2mdn.net`, `adsafeprotected.com`, `moatads.com`, `hotjar.com`, `Mouseflow.com`, `mouseflow.com`, `freshmarketer.com`, `luckyorange.com`, `clarity.ms`

#### B. Minimal URL Path & Query Filters (`8` Patterns)
`/pagead/`, `/adsid/`, `/adserver`, `/adframe`, `/doubleclick/`, `/ads/ga-audiences`, `/collect?v=`, `/gtm.js`

#### C. Minimal Cosmetic DOM Selectors (`10` Selectors)
`ins.adsbygoogle`, `[id^="div-gpt-ad"]`, `[id^="google_ads_iframe"]`, `iframe[src*="doubleclick.net"]`, `iframe[src*="googlesyndication.com"]`, `.taboola-widget`, `.OUTBRAIN`, `.ad-banner`, `.advertisement-banner`, `[data-ad-slot]`

---

### Tier 2 — Pro Mode (Optional • `189` Total Unique Filters)
Includes all **Minimal Mode** filters above, plus:

#### A. Expanded Network Host Filters (`~87` Additional Entries)
- **More Ad Networks**:
  `adnxs-simple.com`, `adsafeprotected.com`, `adroll.com`, `adzerk.net`, `appier.net`, `bidswitch.net`, `bidding.cam`, `brealtime.com`, `brighteroption.com`, `bannerflow.com`, `buysellads.com`, `chromeads.com`, `conversantmedia.com`, `criteo.com`, `deltax.ai`, `dlx.addirect.com`, `eclickads.net`, `engagebdr.com`, `epom.com`, `exponential.com`, `fimserve.com`, `flow-ad-blocker.com`, `free-counter.co.uk`, `fuel451.com`, `gan.doubleclick.net`, `getintent.com`, `go-serve.com`, `go2link.net`, `grapeshot.co.uk`, `halogennetwork.com`, `hi.adnetpub.com`, `hit.sharehuts.com`, `imads.intellitxt.com`, `indexww.com`, `infolinks.com`, `inhouse.adsharp.net`, `jemax.com`, `kontera.com`, `liadm.com`, `live-adtech.com`, `lockerdome.com`, `ljmp.com`, `moatads.com`, `native-adblock.com`, `nextaday.com`, `on.clkads.com`, `onesignal.com`, `openx.net`, `ox-d.com`, `pagefair.com`, `performatix.com`, `pgdt.com`, `popads.net`, `popads.org`, `popcash.net`, `popmyads.com`, `propellerads.com`, `pushnami.com`, `pushcrew.com`, `reallygoodarticles.com`, `revcontent.com`, `rlcdn.com`, `ronomaeheuh.com`, `rtb-1.eekkqaa.com`, `rtbpop.com`, `smartadserver.com`, `smaato.net`, `sonobi.com`, `spotxchange.com`, `steelhousemedia.com`, `stroimfitness.ru`, `syndication.exoclick.com`, `teads.tv`, `texthit-service.com`, `trafficjunky.com`, `trafficjunky.net`, `traffichunt.com`, `tradetracker.net`, `valueclick.com`, `videoplaza.com`, `vistapanel.net`, `waveadvertising.com`, `xd.rafgp.com`, `yieldmo.com`, `yieldlab.net`, `zedo.com`, `zestymedia.com`
- **Analytics & Telemetry Expansion**:
  `analytics.google.com`, `region1.google-analytics.com`, `stats.g.doubleclick.net`, `mixpanel.com`, `segment.io`, `amplitude.com`, `heapanalytics.com`, `fullstory.com`, `matomo.cloud`, `statcounter.com`, `clicky.com`, `chartbeat.com`, `parsely.com`, `crashlytics.com`, `sentry.io`, `bugsnag.com`
- **Facebook/Meta & Social Tracking Pixels**:
  `facebook.com/tr`, `facebook.net`, `connect.facebook.net`, `analytics.tiktok.com`, `ads.linkedin.com`, `px.ads.linkedin.com`, `bat.bing.com`, `snap.licdn.com`, `tr.snapchat.com`, `ads.twitter.com`, `static.ads-twitter.com`

#### B. Expanded Path/Script Pattern Filters (`~35` Patterns)
- `*/pagead2/*`, `*/banner_ad*`, `*/bannerad*`, `*/banner-ads*`
- `*/adserver/*`, `*/ad-server/*`, `*/adserv/*`
- `*/adframe/*`, `*/adframe.js`, `*/adframe.php`
- `*/adcall/*`, `*/adrouter/*`, `*/adproxy/*`
- `*/afr.php*` (AdButler), `*/delivery/ajs.php*`, `*/delivery/lg.php*` (Revive Adserver)
- `*/ad.js`, `*/ad2.js`, `*/advert.js`, `*/advertisement.js`
- `*/openx/www/delivery/*`, `*/api/adserver/*`, `*/sdk/ads/*`, `*/vast*.xml`, `*/vmap*.xml`
- `*/popunder*`, `*/pop-under*`, `*/popup.js`
- `*/prebid*.js`, `*/pbjs*.js`, `*/apstag.js` (Amazon TAM)
- `*/gpt.js`, `*/googletag.js`, `*/adsbygoogle.js`, `*/show_ads.js`
- `*/tag.min.js*mediavine`, `*/mediavine.js`, `*/adthrive.js`, `*/siteground-banner.js`

#### C. Expanded Cosmetic DOM Selectors (`~33` Selectors)
- `.ad`, `.ads`, `.adv`, `.advert`, `.adverts`, `.advertisement`
- `.ad-container`, `.ad-wrapper`, `.ad-unit`, `.ad-box`, `.ad-holder`, `.ad-label`
- `[id*="google_ads"]`, `[id*="taboola"]`, `[id*="outbrain"]`, `[id*="mgid"]`
- `[class*="sponsored"]`, `[class*="Sponsored"]`, `[data-ad]`, `[data-ads]`
- `[data-ad-unit]`, `[data-ad-slot]`, `[data-ad-client]`, `[data-google-query-id]`
- `amp-ad`, `amp-embed`, `.adcode`, `.adsbox`, `.ad-badge`, `.text-ads`
- `a[href*="doubleclick.net"]`, `a[href*="/adclick"]`
- `ins.adsbygoogle`, `ins[data-ad-slot]`
- `.widget-taboola`, `.OUTBRAIN`, `.ob-widget`, `.trc_rbox`, `.trc_rbox_div`
- `iframe[src*="taboola"]`, `iframe[src*="outbrain"]`, `iframe[src*="mgid"]`

---

### Tier 3 — Max Mode (Optional • `220` Total Unique Filters • YouTube Ad Shield)
Includes all **Minimal** + **Pro** filters above, plus dedicated **YouTube Ad/Tracking Filters**:

#### A. Blockable YouTube Ad/Tracking Hosts
- `youtube.cleverads.vn`
- `pagead2.googlesyndication.com`
- `googleads.g.doubleclick.net`
- `tpc.googlesyndication.com`
- `securepubads.g.doubleclick.net`
- `pubads.g.doubleclick.net`
- `adservice.google.com`

#### B. Path Filters for YouTube
- `youtube.com/api/stats/ads*`
- `youtube.com/pagead/*`
- `youtube.com/ptracking*`
- `youtube.com/pcs/activeview*`
- `youtube.com/get_midroll_info*`
- `/youtubei/v1/log_event*`
- `doubleclick.net/instream/ad_status*`
- `pagead2.googlesyndication.com/pagead/*`
- `ytimg.com/ads-*`
- `googlevideo.com/videogoodput*` *(ad goodput verification only — never blocks primary `*.googlevideo.com/videoplayback` streams)*

#### C. Cosmetic Filters for YouTube (via WebView JS Injection)
```css
.ytp-ad-module, .ytp-ad-overlay-container, .ytp-ad-text-overlay,
.ytp-ad-image-overlay, .ytp-ad-message-container, .ytp-ad-player-overlay,
.ytd-display-ad-renderer, .ytd-promoted-sparkles-web-renderer,
.ytd-promoted-video-renderer, ytd-ad-slot-renderer,
ytd-in-feed-ad-layout-renderer, ytd-compact-promoted-video-renderer,
#masthead-ad, #player-ads, #background-promo-renderer,
ytd-rich-item-renderer:has(ytd-display-ad-renderer)
```
Plus automatic in-player ad fast-forward & `.ytp-ad-skip-button` / `.ytp-ad-skip-button-modern` auto-click when `#movie_player.ad-showing` is detected.

---

## Project Architecture

```text
app/src/main/java/com/example/
├── MainActivity.kt                  # Edge-to-Edge host activity & intent router
├── browser/
│   ├── AdBlocker.kt                 # 3-Tier Ad/Tracker engine (Minimal: 58, Pro: 189, Max: 220)
│   ├── NovaDownloadHelper.kt        # Multi-threaded HTTP Range download engine
│   ├── NovaWebViewPool.kt           # Multi-tab WebView pool, JS bridges & anti-CAPTCHA headers
│   └── ShortcutIconCacheManager.kt  # On-visit favicon fetcher (96x96 WEBP disk + 1.5MB LruCache)
├── data/
│   ├── AppDatabase.kt               # Room SQLite database (Bookmarks, History, Downloads, Shortcuts)
│   ├── BrowserDao.kt                # Reactive Flow DAOs
│   ├── BrowserModels.kt             # Domain models, AdBlockMode enum, DevTools & Tab states
│   └── BrowserRepository.kt         # DataStore preferences & Room repository
├── ui/
│   ├── NovaBrowserApp.kt            # Root Scaffold, radial wave canvas & sheet orchestration
│   ├── components/
│   │   ├── BrowserSheets.kt         # Menu, Blocked Ads + Mode/Rule Inspector, Tabs, History, Downloads
│   │   ├── BrowserToolbars.kt       # Top URL bar (Copy/Edit/Share) & Bottom navigation bar
│   │   ├── DevToolsPanel.kt         # Split-screen Elements, Sources, Console & Network inspector
│   │   └── NovaHomeContent.kt       # Centric Home screen, Omnibox, Split buttons & Quick Shortcuts
│   └── theme/
│       └── Theme.kt                 # Warm Light Orange default & Dark Cyan Incognito M3 themes
└── viewmodel/
    └── BrowserViewModel.kt          # Unidirectional state holder & tab/ad-block coordinator
```

---

## Signed Release APK (`v6.6.0`)

A production-ready, R8-minified, resource-shrunk, signed release APK is pre-built in [`releases/v6.6.0/`](releases/v6.6.0/):

| Property | Value |
| :--- | :--- |
| **APK Path** | `releases/v6.6.0/NovaBrowser-v6.6.0-release.apk` |
| **Release Notes** | `releases/v6.6.0/RELEASE_NOTES.md` |
| **Application ID** | `com.suryatech.browser.nova` |
| **Version** | `6.6.0` (`versionCode = 12`) |
| **Min / Target SDK** | `minSdk = 24` (Android 7.0) / `targetSdk = 36` |
| **APK Size** | `2,507,848 bytes` (~2.39 MB) |
| **SHA-256 Checksum** | `4fd5ff6ecf7db906d1e32a38336fc286f0dc87c415d54693347f2168c9601b54` |

---

## Building from Source

### Prerequisites
- **JDK 17+**
- **Android SDK 36**

### Build Commands
```bash
# Compile & assemble debug APK
gradle :app:assembleDebug

# Build R8-optimized signed release APK
STORE_PASSWORD=android KEY_PASSWORD=android gradle :app:assembleRelease
```

---

## License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)** — see the [LICENSE](LICENSE) file for details.
