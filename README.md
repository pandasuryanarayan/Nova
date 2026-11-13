<div align="center">

# 🌐 Nova Browser

### **Fast, Private, Ad-Free & Developer-Grade Open-Source Android Web Browser**

[![Version](https://img.shields.io/badge/Release-v6.7.0%20(code%2013)-FF6D00?style=for-the-badge&logo=android&logoColor=white)](releases/v6.7.0/NovaBrowser-v6.7.0-release.apk)
[![Platform](https://img.shields.io/badge/Android-API%2024%E2%80%9336-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#building-from-source)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](#tech-stack--architecture)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](#key-features)
[![Ad-Blocker](https://img.shields.io/badge/Ad--Shield-3--Tier%20(Up%20to%20220%20Rules)-00C853?style=for-the-badge&logo=shield&logoColor=white)](#3-tier-cumulative-ad-blocker--tracker-shield)
[![APK Size](https://img.shields.io/badge/APK%20Size-2.41%20MB%20(R8%20Shrunk)-00B0FF?style=for-the-badge&logo=googleplay&logoColor=white)](#signed-release-apk-v670)
[![License](https://img.shields.io/badge/License-GNU%20GPL%20v3.0-D50000?style=for-the-badge&logo=gnu&logoColor=white)](LICENSE)

<br/>

[![Download APK](https://img.shields.io/badge/%E2%AC%87%EF%B8%8F%20Download%20Signed%20Release%20APK-NovaBrowser--v6.7.0--release.apk-FF6D00?style=for-the-badge)](releases/v6.7.0/NovaBrowser-v6.7.0-release.apk)
[![Release Notes](https://img.shields.io/badge/%F0%9F%93%9D%20Release%20Notes-v6.7.0-263238?style=for-the-badge)](releases/v6.7.0/RELEASE_NOTES.md)
[![CI Workflow](https://img.shields.io/badge/GitHub%20Actions-Release%20Pipeline-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)](.github/workflows/release.yml)

---

**Package ID:** `com.suryatech.browser.nova` &nbsp;•&nbsp; **Version:** `6.7.0` (`versionCode = 13`) &nbsp;•&nbsp; **Min SDK:** `24` (Android 7.0+) &nbsp;•&nbsp; **Target SDK:** `36`

</div>

---

## ✨ Feature Chips at a Glance

<div align="center">

![Minimal Ad-Block](https://img.shields.io/badge/%F0%9F%9B%A1%EF%B8%8F%20Minimal%20Mode-58%20Filters%20(Default%20ON)-00C853?style=flat-square)
![Pro Ad-Block](https://img.shields.io/badge/%F0%9F%94%A5%20Pro%20Mode-189%20Filters%20(Minimal%20%2B%20Pro)-FF6D00?style=flat-square)
![Max Ad-Block](https://img.shields.io/badge/%E2%9A%A1%20Max%20Mode-220%20Filters%20(%2B%20YouTube%20Ad--Skip)-D50000?style=flat-square)
![Downloads](https://img.shields.io/badge/%E2%AC%87%EF%B8%8F%20Downloads-Ongoing%20%7C%20Success%20%7C%20Failure-00897B?style=flat-square)
![Languages](https://img.shields.io/badge/%F0%9F%8C%90%20Languages-System%20%7C%20English%20%7C%20%E0%A4%B9%E0%A4%BF%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A5%80-6A1B9A?style=flat-square)
![Default Theme](https://img.shields.io/badge/%F0%9F%8E%A8%20Default%20Theme-Warm%20Light%20Orange-FF8A3D?style=flat-square)
![Incognito Theme](https://img.shields.io/badge/%F0%9F%95%B5%EF%B8%8F%20Incognito%20Mode-Dark%20Stealth%20Cyan%20%2B%20Radial%20Wave-00E5FF?style=flat-square)
![Anti-CAPTCHA](https://img.shields.io/badge/%F0%9F%94%92%20Google%20Search-Trusted%20Anti--CAPTCHA%20Headers-4285F4?style=flat-square)
![Favicon Cache](https://img.shields.io/badge/%F0%9F%96%BC%EF%B8%8F%20Shortcut%20Icons-On--Visit%2096x96%20WEBP%20Cache-8E24AA?style=flat-square)
![DevTools](https://img.shields.io/badge/%F0%9F%9B%A0%EF%B8%8F%20DevTools-Elements%20%7C%20Sources%20%7C%20Console%20%7C%20Network-37474F?style=flat-square)
![Video Sniffer](https://img.shields.io/badge/%F0%9F%8E%AC%20Video%20Sniffer-MP4%20%2F%20HLS%20%2B%20Background%20Audio-E91E63?style=flat-square)

</div>

---

## 📦 Signed Release APK (`v6.7.0`)

A production-ready, R8 full-mode minified, resource-shrunk, V1/V2/V3 signed release APK is included directly in the repository under [`releases/v6.7.0/`](releases/v6.7.0/):

| Property | Details |
| :--- | :--- |
| **APK File** | [`releases/v6.7.0/NovaBrowser-v6.7.0-release.apk`](releases/v6.7.0/NovaBrowser-v6.7.0-release.apk) |
| **Release Notes** | [`releases/v6.7.0/RELEASE_NOTES.md`](releases/v6.7.0/RELEASE_NOTES.md) |
| **Application ID** | `com.suryatech.browser.nova` |
| **Version** | `6.7.0` (`versionCode = 13`) |
| **APK Size** | `2,524,632 bytes` (**~2.41 MB**) |
| **SHA-256 Checksum** | `04af675fa55587b5bcc9139f863e190f3917da1f9d0893cb509ca276d5312f12` |
| **Signing Certificate** | `CN=SuryaTech, OU=Mobile, O=SuryaTech, L=Bengaluru, ST=KA, C=IN` |
| **Optimization** | R8 Full Mode (`isMinifyEnabled = true`, `isShrinkResources = true`, `isCrunchPngs = true`) |

---

## 🛡️ 3-Tier Cumulative Ad-Blocker & Tracker Shield

Nova Browser features a **cumulative 3-tier Ad-Blocker engine** (`com.example.browser.AdBlocker`) operating at two independent layers:
1. **Network Request Interception (`WebViewClient.shouldInterceptRequest`)**: Blocks ad scripts, tracking pixels, RTB auctions, VAST/VMAP video ads, and telemetry beacons **before** any network payload is downloaded (`204 No Content`).
2. **Cosmetic DOM & Player Shield (`evaluateJavascript` + `MutationObserver`)**: Injects high-specificity CSS rules (`display: none !important`) and a throttled DOM observer to remove banner slots, native ad widgets, and YouTube video ad overlays.

### Cumulative Tier Architecture

<div align="center">

![Tier 1 Minimal](https://img.shields.io/badge/Tier%201%3A%20MINIMAL-58%20Rules%20(Default%20ON)-00C853?style=for-the-badge)
&nbsp;➕&nbsp;
![Tier 2 Pro](https://img.shields.io/badge/Tier%202%3A%20PRO-189%20Rules%20(Minimal%20%2B%20131%20Pro)-FF6D00?style=for-the-badge)
&nbsp;➕&nbsp;
![Tier 3 Max](https://img.shields.io/badge/Tier%203%3A%20MAX-220%20Rules%20(Minimal%20%2B%20Pro%20%2B%20YouTube)-D50000?style=for-the-badge)

</div>

> **Cumulative Inheritance Guarantee:**
> - Choosing **Minimal Mode** activates the **58** core rules (`Default ON`).
> - Choosing **Pro Mode** activates **all Minimal rules + all Pro rules** (**189** unique rules total).
> - Choosing **Max Mode** activates **all Minimal rules + all Pro rules + all YouTube Ad/Tracking rules & auto-skipper** (**220** unique rules total).

| Mode Tier | Default State | Host Filters | Path / Regex Filters | Cosmetic DOM Filters | Total Active Rules |
| :--- | :---: | :---: | :---: | :---: | :---: |
| ![Minimal](https://img.shields.io/badge/Minimal-Default%20ON-00C853?style=flat-square) | **ON (Default)** | `40` | `8` | `10` | **`58`** |
| ![Pro](https://img.shields.io/badge/Pro-Optional-FF6D00?style=flat-square) | **OFF** (User Toggle) | `114` | `37` | `38` | **`189`** |
| ![Max](https://img.shields.io/badge/Max-YouTube%20Shield-D50000?style=flat-square) | **OFF** (User Toggle) | `119` | `47` | `54` (+ Auto-Skip) | **`220`** |

---

### 🟢 Tier 1: Minimal Mode (`58` Rules — Enabled by Default)

![Host Filters](https://img.shields.io/badge/Host%20Filters-40%20Domains-00C853?style=flat-square)
![Path Filters](https://img.shields.io/badge/Path%20Filters-8%20Patterns-00B0FF?style=flat-square)
![Cosmetic Selectors](https://img.shields.io/badge/Cosmetic%20DOM-10%20Selectors-AA00FF?style=flat-square)

#### 1. Minimal Network Host Filters (`40` Domains)
- **Ad Networks (`26`)**:
  `doubleclick.net` • `googlesyndication.com` • `googleadservices.com` • `adservice.google.com` • `ads.Exoclick.com` • `exoclick.com` • `adnxs.com` • `advertising.com` • `taboola.com` • `outbrain.com` • `criteo.com` • `criteo.net` • `pubmatic.com` • `rubiconproject.com` • `openx.net` • `casalemedia.com` • `amazon-adsystem.com` • `adcolony.com` • `applovin.com` • `unityads.unity3d.com` • `vungle.com` • `chartboost.com` • `ironsrc.com` • `inmobi.com` • `medianet.com` • `media.net`
- **Analytics, Trackers & Telemetry (`14`)**:
  `google-analytics.com` • `googletagmanager.com` • `googletagservices.com` • `scorecardresearch.com` • `quantserve.com` • `zedo.com` • `2mdn.net` • `adsafeprotected.com` • `moatads.com` • `hotjar.com` • `Mouseflow.com` • `mouseflow.com` • `freshmarketer.com` • `luckyorange.com` • `clarity.ms`

#### 2. Minimal URL Path & Query Filters (`8` Patterns)
`/pagead/` • `/adsid/` • `/adserver` • `/adframe` • `/doubleclick/` • `/ads/ga-audiences` • `/collect?v=` • `/gtm.js`

#### 3. Minimal Cosmetic DOM Selectors (`10` Selectors)
`ins.adsbygoogle` • `[id^="div-gpt-ad"]` • `[id^="google_ads_iframe"]` • `iframe[src*="doubleclick.net"]` • `iframe[src*="googlesyndication.com"]` • `.taboola-widget` • `.OUTBRAIN` • `.ad-banner` • `.advertisement-banner` • `[data-ad-slot]`

---

### 🟠 Tier 2: Pro Mode (`189` Total Unique Rules — Cumulative)

![Combined Hosts](https://img.shields.io/badge/Combined%20Hosts-114%20Unique%20Domains-FF6D00?style=flat-square)
![Combined Paths](https://img.shields.io/badge/Combined%20Path%2FRegex-37%20Unique%20Patterns-00B0FF?style=flat-square)
![Combined DOM](https://img.shields.io/badge/Combined%20Cosmetic%20DOM-38%20Unique%20Selectors-AA00FF?style=flat-square)

Includes **all 58 Minimal Mode rules** plus the following Pro expansions:

#### 1. Expanded Network Host Filters (`~87` Additional Entries)
- **More Ad Networks**:
  `adnxs-simple.com` • `adsafeprotected.com` • `adroll.com` • `adzerk.net` • `appier.net` • `bidswitch.net` • `bidding.cam` • `brealtime.com` • `brighteroption.com` • `bannerflow.com` • `buysellads.com` • `chromeads.com` • `conversantmedia.com` • `criteo.com` • `deltax.ai` • `dlx.addirect.com` • `eclickads.net` • `engagebdr.com` • `epom.com` • `exponential.com` • `fimserve.com` • `flow-ad-blocker.com` • `free-counter.co.uk` • `fuel451.com` • `gan.doubleclick.net` • `getintent.com` • `go-serve.com` • `go2link.net` • `grapeshot.co.uk` • `halogennetwork.com` • `hi.adnetpub.com` • `hit.sharehuts.com` • `imads.intellitxt.com` • `indexww.com` • `infolinks.com` • `inhouse.adsharp.net` • `jemax.com` • `kontera.com` • `liadm.com` • `live-adtech.com` • `lockerdome.com` • `ljmp.com` • `moatads.com` • `native-adblock.com` • `nextaday.com` • `on.clkads.com` • `onesignal.com` *(push ads)* • `openx.net` • `ox-d.com` • `pagefair.com` • `performatix.com` • `pgdt.com` • `popads.net` • `popads.org` • `popcash.net` • `popmyads.com` • `propellerads.com` • `pushnami.com` • `pushcrew.com` • `reallygoodarticles.com` • `revcontent.com` • `rlcdn.com` • `ronomaeheuh.com` • `rtb-1.eekkqaa.com` • `rtbpop.com` • `smartadserver.com` • `smaato.net` • `sonobi.com` • `spotxchange.com` • `steelhousemedia.com` • `stroimfitness.ru` • `syndication.exoclick.com` • `teads.tv` • `texthit-service.com` • `trafficjunky.com` • `trafficjunky.net` • `traffichunt.com` • `tradetracker.net` • `valueclick.com` • `videoplaza.com` • `vistapanel.net` • `waveadvertising.com` • `xd.rafgp.com` • `yieldmo.com` • `yieldlab.net` • `zedo.com` • `zestymedia.com`
- **Analytics & Telemetry Expansion**:
  `analytics.google.com` • `region1.google-analytics.com` • `stats.g.doubleclick.net` • `mixpanel.com` • `segment.io` • `amplitude.com` • `heapanalytics.com` • `fullstory.com` • `matomo.cloud` • `statcounter.com` • `clicky.com` • `chartbeat.com` • `parsely.com` • `crashlytics.com` • `sentry.io` • `bugsnag.com`
- **Facebook/Meta & Social Tracking Pixels**:
  `facebook.com/tr` • `facebook.net` • `connect.facebook.net` • `analytics.tiktok.com` • `ads.linkedin.com` • `px.ads.linkedin.com` • `bat.bing.com` • `snap.licdn.com` • `tr.snapchat.com` • `ads.twitter.com` • `static.ads-twitter.com`

#### 2. Expanded Path & Script Pattern Filters (`~35` Patterns)
```text
*/pagead2/*
*/banner_ad*          */bannerad*          */banner-ads*
*/adserver/*          */ad-server/*        */adserv/*
*/adframe/*           */adframe.js         */adframe.php
*/adcall/*            */adrouter/*         */adproxy/*
*/afr.php*            (AdButler)
*/delivery/ajs.php*   */delivery/lg.php*   (Revive Adserver)
*/ad.js               */ad2.js             */advert.js         */advertisement.js
*/openx/www/delivery/*
*/api/adserver/*      */sdk/ads/*          */vast*.xml         */vmap*.xml
*/popunder*           */pop-under*         */popup.js
*/prebid*.js          */pbjs*.js           */apstag.js         (Amazon TAM)
*/gpt.js              */googletag.js       */adsbygoogle.js    */show_ads.js
*/tag.min.js*mediavine */mediavine.js      */adthrive.js       */siteground-banner.js
```

#### 3. Expanded Cosmetic DOM Selectors (`~33` Selectors)
```css
/* Ad slots & containers */
.ad, .ads, .adv, .advert, .adverts, .advertisement
.ad-container, .ad-wrapper, .ad-unit, .ad-box, .ad-holder, .ad-label
[id*="google_ads"], [id*="taboola"], [id*="outbrain"], [id*="mgid"]
[class*="sponsored"], [class*="Sponsored"], [data-ad], [data-ads]
[data-ad-unit], [data-ad-slot], [data-ad-client], [data-google-query-id]
amp-ad, amp-embed, .adcode, .adsbox, .ad-badge, .text-ads
a[href*="doubleclick.net"], a[href*="/adclick"]
ins.adsbygoogle, ins[data-ad-slot]
.widget-taboola, .OUTBRAIN, .ob-widget, .trc_rbox, .trc_rbox_div
iframe[src*="taboola"], iframe[src*="outbrain"], iframe[src*="mgid"]
```

---

### 🔴 Tier 3: Max Mode (`220` Total Unique Rules — YouTube Ad & Tracker Shield)

![Combined Hosts](https://img.shields.io/badge/Combined%20Hosts-119%20Unique%20Domains-D50000?style=flat-square)
![Combined Paths](https://img.shields.io/badge/Combined%20Path%2FRegex-47%20Unique%20Patterns-00B0FF?style=flat-square)
![Combined DOM](https://img.shields.io/badge/Combined%20Cosmetic%20DOM-54%20Unique%20Selectors-AA00FF?style=flat-square)
![Auto Skipper](https://img.shields.io/badge/YouTube%20Player-Auto--Skip%20%26%20Fast--Forward-00C853?style=flat-square)

Includes **all Minimal + Pro rules** (`189` rules) plus **YouTube-specific network, path, and cosmetic rules**:

#### A. Blockable YouTube Ad/Tracking Hosts
`youtube.cleverads.vn` • `pagead2.googlesyndication.com` • `googleads.g.doubleclick.net` • `tpc.googlesyndication.com` • `securepubads.g.doubleclick.net` • `pubads.g.doubleclick.net` • `adservice.google.com`

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
- `googlevideo.com/videogoodput*` *(ad goodput verification only — never blocks `*.googlevideo.com/videoplayback` video streams)*

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
> **In-Player Video Ad Auto-Skipper:** When `#movie_player.ad-showing` is active in **Max Mode**, Nova Browser automatically clicks `.ytp-ad-skip-button` / `.ytp-ad-skip-button-modern` / `.ytp-skip-ad-button` and fast-forwards unskippable ad segments to `video.duration`.

---

## 🚀 Key Features & UX Refinements

### 🎨 Dual-Theme System & Smooth Radial Wave Animation
- **Default Theme (Warm Light Orange)**: High-contrast warm alabaster backgrounds (`#FFF8F4`) with deep amber-orange (`#E65100` / `#FF8A3D`) accents and crisp SSL lock visibility.
- **Incognito Theme (Dark Stealth Cyan)**: Deep obsidian surfaces (`#070B14`) with electric cyan (`#00E5FF`) accents.
- **App-Wide Radial Wave Transition**: Toggling Incognito ON or OFF runs a smooth 420ms color-scheme interpolation paired with a full-screen radial wave canvas animation.

### 🔒 Trusted Google Search & Anti-CAPTCHA Hardening
- Uses a standard mobile Chrome User-Agent without non-standard bot tokens (`NovaBrowser/...`) that trigger Google Search CAPTCHA challenges.
- Normalizes `navigator.webdriver = false`, `navigator.languages`, `navigator.plugins`, and `window.chrome` runtime objects while preserving first-party session cookies and restricting `DNT: 1` / `Sec-GPC: 1` headers to cross-site third-party requests.

### 🖼️ Smart On-Visit Favicon Cache & Editable Quick Shortcuts
- **Zero Idle Network Overhead**: Quick Shortcuts on the Home screen render instant vector/monogram badges by default and never poll the network in the background.
- **On-Visit Disk + RAM Cache**: When a website is actually opened, `ShortcutIconCacheManager` fetches its icon once, downscales it to a `96×96` WEBP file in `filesDir/shortcut_icons/` backed by a 1.5 MB RAM `LruCache`, and reuses it offline.
- **Individual `⋮` Menu per Shortcut**: Every default or custom shortcut has a 3-dot (`⋮`) menu to **Edit** (Title & URL) or **Delete** it.

### 🔗 Aligned Top Omnibox Dropdown & End-of-URL Cursor
- Tapping the top URL bar opens a compact dropdown where the **SSL Security Badge** and **Website Link** align at an exact `16.dp` left margin, with icon-only **Copy**, **Edit**, and **Share** buttons on the right.
- Tapping **Edit** focuses the inline URL field with the cursor placed at the **end of the URL** (`TextRange(url.length)`) so you can immediately backspace or append paths.

### 📥 3-Section Downloads Manager (`Ongoing` • `Success` • `Failure`)
- **Ongoing**: Newly started, active (`RUNNING` / `PENDING`), and `PAUSED` downloads appear in **Ongoing** with real-time progress and pause/resume controls.
- **Success**: Finished downloads (`COMPLETED`) automatically move into **Success** for 1-tap playback/opening.
- **Failure**: Failed downloads (`FAILED`) automatically move into **Failure** with error details and 1-tap **Retry** (returning the item back into **Ongoing**).

### 🌐 Android Inbuilt System Language & 3-Dot Language Selector
- **Automatic Device Language**: Defaults to **System** (`AppLanguage.SYSTEM`), reading Android's inbuilt configuration locale (`Resources.getSystem().configuration` + `locales_config.xml`) so the app automatically matches the device language (`English` or `हिन्दी`).
- **3-Dot Menu Language Section**: Switch anytime between **System**, **English**, and **हिन्दी (Hindi)** directly inside the 3-dot browser menu without reloading active `WebView` tabs.

### 🛠️ Desktop-Grade Split-Screen DevTools, Video Sniffer & Downloader
- **DevTools Panel**: Live **Elements** DOM tree inspector & editor, **Sources** viewer, **Console** JS REPL, and **Network** request profiler.
- **Video Stream Sniffer & Background Player**: Detects `<video>`, `.mp4`, `.m3u8`, and `.webm` streams for 1-tap download or background audio playback.
- **Multi-Threaded Hybrid Downloader**: System `DownloadManager`, HTTP/2 OkHttp fallback, and WebView stream bridge with Room-persisted download state.

---

## 🏗️ Tech Stack & Architecture

<div align="center">

![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean%20StateFlow-00897B?style=flat-square)
![Database](https://img.shields.io/badge/Database-Room%20SQLite%20(KSP)-FF6D00?style=flat-square&logo=sqlite&logoColor=white)
![Preferences](https://img.shields.io/badge/Preferences-Jetpack%20DataStore-00B0FF?style=flat-square)
![WebView](https://img.shields.io/badge/Engine-Android%20WebView%20Pool-3DDC84?style=flat-square&logo=googlechrome&logoColor=white)

</div>

```text
app/src/main/java/com/example/
├── MainActivity.kt                  # Edge-to-Edge activity & deep-link intent router
├── browser/
│   ├── AdBlocker.kt                 # 3-Tier Ad/Tracker Shield (Minimal: 58, Pro: 189, Max: 220)
│   ├── NovaDownloadHelper.kt        # Segmented HTTP Range download engine with pause/resume
│   ├── NovaWebViewPool.kt           # Multi-tab WebView pool, JS bridges & anti-CAPTCHA headers
│   └── ShortcutIconCacheManager.kt  # On-visit favicon fetcher (96x96 WEBP disk + 1.5MB LruCache)
├── data/
│   ├── AppDatabase.kt               # Room SQLite database (Bookmarks, History, Downloads, Shortcuts)
│   ├── BrowserDao.kt                # Reactive Coroutine Flow DAOs
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

## 🔧 Building from Source

### Prerequisites
- **JDK 17+**
- **Android SDK 36**

### Build Commands
```bash
# Assemble debug APK
gradle :app:assembleDebug

# Assemble R8-optimized signed release APK
STORE_PASSWORD=android KEY_PASSWORD=android gradle :app:assembleRelease

# Verify SHA-256 checksum
sha256sum app/build/outputs/apk/release/app-release.apk
```

---

## 📄 License

<div align="center">

[![GNU GPL v3.0](https://img.shields.io/badge/License-GNU%20General%20Public%20License%20v3.0-D50000?style=for-the-badge&logo=gnu&logoColor=white)](LICENSE)

Copyright © **SuryaTech** (`com.suryatech.browser.nova`).  
Distributed under the **GNU General Public License v3.0 (GPL-3.0)**. See [`LICENSE`](LICENSE) for the full license text.

</div>
