# Nova Browser v6.6.0 — Signed Release APK

![Version](https://img.shields.io/badge/Version-v6.6.0%20(versionCode%2012)-FF6D00?style=flat-square&logo=android&logoColor=white)
![APK Size](https://img.shields.io/badge/Size-2.39%20MB%20(2%2C507%2C848%20bytes)-00B0FF?style=flat-square)
![Ad-Blocker](https://img.shields.io/badge/Ad--Shield-Minimal%20(58)%20%7C%20Pro%20(189)%20%7C%20Max%20(220)-00C853?style=flat-square)
![License](https://img.shields.io/badge/License-GNU%20GPL%20v3.0-D50000?style=flat-square)

---

## What's New in v6.6.0

### 1. 3-Tier Cumulative Ad-Blocker Architecture (`Minimal` • `Pro` • `Max`)
Nova Browser now includes a **3-tier cumulative Ad-Blocker Shield** (`com.example.browser.AdBlocker`) configurable from the **Home Screen Ads Badge**, **Browser Menu Sheet (`⋮`)**, or **Blocked Ads Sheet**:

| Mode | Default Status | Total Active Filters | Breakdown | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Minimal Mode** | **ON (Default)** | **58 Filters** | `40` Host + `8` Path + `10` Cosmetic DOM | Core fast ad, analytics, and telemetry blocking with zero overhead |
| **Pro Mode** | **OFF** (User Toggle) | **189 Filters** | `114` Host + `37` Path/Regex + `38` Cosmetic DOM | All **Minimal** rules + ~87 ad/analytics/social-pixel domains, ~35 path/script patterns & ~33 cosmetic selectors |
| **Max Mode** | **OFF** (User Toggle) | **220 Filters** | `119` Host + `47` Path/Regex + `54` Cosmetic DOM + Auto-Skip | All **Minimal + Pro** rules + YouTube Ad/Tracking Hosts, Path Filters, Cosmetic DOM Filters & Video Ad Auto-Skipper |

---

### 2. Tier Breakdown
- **Minimal Mode (Default — 58 Rules)**:
  - **40 Network Host Filters**: Core ad networks (`doubleclick.net`, `googlesyndication.com`, `taboola.com`, `outbrain.com`, `adnxs.com`, `rubiconproject.com`, `pubmatic.com`, `casalemedia.com`, `exoclick.com`, etc.) and core trackers (`google-analytics.com`, `googletagmanager.com`, `hotjar.com`, `clarity.ms`, `mouseflow.com`, etc.).
  - **8 URL Path & Query Filters**: `/pagead/`, `/adsid/`, `/adserver`, `/adframe`, `/doubleclick/`, `/ads/ga-audiences`, `/collect?v=`, `/gtm.js`.
  - **10 Cosmetic DOM Selectors**: Core banner and iframe ad containers.
- **Pro Mode (189 Total Unique Rules — Cumulative)**:
  - Adds **~87 Expanded Network Host Filters** across Ad Networks (`adnxs-simple.com`, `adsafeprotected.com`, `adroll.com`, `adzerk.net`, `appier.net`, `bidswitch.net`, `bidding.cam`, `brealtime.com`, `brighteroption.com`, `bannerflow.com`, `buysellads.com`, `chromeads.com`, `conversantmedia.com`, `criteo.com`, `deltax.ai`, `dlx.addirect.com`, `eclickads.net`, `engagebdr.com`, `epom.com`, `exponential.com`, `fimserve.com`, `flow-ad-blocker.com`, `free-counter.co.uk`, `fuel451.com`, `gan.doubleclick.net`, `getintent.com`, `go-serve.com`, `go2link.net`, `grapeshot.co.uk`, `halogennetwork.com`, `hi.adnetpub.com`, `hit.sharehuts.com`, `imads.intellitxt.com`, `indexww.com`, `infolinks.com`, `inhouse.adsharp.net`, `jemax.com`, `kontera.com`, `liadm.com`, `live-adtech.com`, `lockerdome.com`, `ljmp.com`, `moatads.com`, `native-adblock.com`, `nextaday.com`, `on.clkads.com`, `onesignal.com`, `openx.net`, `ox-d.com`, `pagefair.com`, `performatix.com`, `pgdt.com`, `popads.net`, `popads.org`, `popcash.net`, `popmyads.com`, `propellerads.com`, `pushnami.com`, `pushcrew.com`, `reallygoodarticles.com`, `revcontent.com`, `rlcdn.com`, `ronomaeheuh.com`, `rtb-1.eekkqaa.com`, `rtbpop.com`, `smartadserver.com`, `smaato.net`, `sonobi.com`, `spotxchange.com`, `steelhousemedia.com`, `stroimfitness.ru`, `syndication.exoclick.com`, `teads.tv`, `texthit-service.com`, `trafficjunky.com`, `trafficjunky.net`, `traffichunt.com`, `tradetracker.net`, `valueclick.com`, `videoplaza.com`, `vistapanel.net`, `waveadvertising.com`, `xd.rafgp.com`, `yieldmo.com`, `yieldlab.net`, `zedo.com`, `zestymedia.com`), Analytics/Telemetry (`analytics.google.com`, `region1.google-analytics.com`, `stats.g.doubleclick.net`, `mixpanel.com`, `segment.io`, `amplitude.com`, `heapanalytics.com`, `fullstory.com`, `matomo.cloud`, `statcounter.com`, `clicky.com`, `chartbeat.com`, `parsely.com`, `crashlytics.com`, `sentry.io`, `bugsnag.com`), and Social Tracking Pixels (`facebook.net`, `connect.facebook.net`, `analytics.tiktok.com`, `ads.linkedin.com`, `px.ads.linkedin.com`, `bat.bing.com`, `snap.licdn.com`, `tr.snapchat.com`, `ads.twitter.com`, `static.ads-twitter.com`).
  - Adds **~35 Expanded Path/Script Pattern Filters** (including AdButler `*/afr.php*`, Revive Adserver `*/delivery/ajs.php*` & `*/delivery/lg.php*`, Prebid `*/prebid*.js` & `*/pbjs*.js`, Amazon TAM `*/apstag.js`, VAST/VMAP XML `*/vast*.xml` & `*/vmap*.xml`, GPT/AdSense `*/gpt.js`, `*/googletag.js`, `*/adsbygoogle.js`, `*/show_ads.js`, Mediavine & AdThrive).
  - Adds **~33 Expanded Cosmetic DOM Selectors** for sponsored cards, AMP ads, Taboola/Outbrain/MGID widgets, and click-tracking links.
- **Max Mode (220 Total Unique Rules — YouTube Ad-Blocker)**:
  - Adds **YouTube Ad/Tracking Hosts** (`youtube.cleverads.vn`, `pagead2.googlesyndication.com`, `googleads.g.doubleclick.net`, `tpc.googlesyndication.com`, `securepubads.g.doubleclick.net`, `pubads.g.doubleclick.net`, `adservice.google.com`).
  - Adds **YouTube Ad Path Filters** (`youtube.com/api/stats/ads*`, `youtube.com/pagead/*`, `youtube.com/ptracking*`, `youtube.com/pcs/activeview*`, `youtube.com/get_midroll_info*`, `/youtubei/v1/log_event*`, `doubleclick.net/instream/ad_status*`, `pagead2.googlesyndication.com/pagead/*`, `ytimg.com/ads-*`, `googlevideo.com/videogoodput*`).
  - Adds **16 YouTube Cosmetic DOM Selectors** + automatic in-player skip/fast-forward for `.ad-showing` video streams.

---

## Signed Release APK Metadata

| Property | Value |
| :--- | :--- |
| **File** | `releases/v6.6.0/NovaBrowser-v6.6.0-release.apk` |
| **Application ID** | `com.suryatech.browser.nova` |
| **Version** | `6.6.0` (`versionCode = 12`) |
| **Size** | `2,507,848 bytes` (~2.39 MB) |
| **SHA-256** | `4fd5ff6ecf7db906d1e32a38336fc286f0dc87c415d54693347f2168c9601b54` |
| **Signing** | V1 + V2 + V3 Signed (`CN=SuryaTech, OU=Mobile, O=SuryaTech, L=Bengaluru, ST=KA, C=IN`) |
