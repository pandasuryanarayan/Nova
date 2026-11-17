# 👁️ Netra `v6.9.9` — Continuous Searching Bar & Forward Snake Download Progress Animation

**Release Date:** November 17, 2026  
**Application ID:** `com.suryatech.browser.netra`  
**Version:** `6.9.9` (`versionCode = 24`)  
**Min SDK:** `24` (Android 7.0+) &nbsp;•&nbsp; **Target SDK:** `36`

---

## 📦 Signed Release APK Details

| Property | Value |
| :--- | :--- |
| **APK Path** | [`releases/v6.9.9/Netra-v6.9.9-release.apk`](Netra-v6.9.9-release.apk) |
| **SHA-256 Checksum File** | [`releases/v6.9.9/Netra-v6.9.9-release.apk.sha256`](Netra-v6.9.9-release.apk.sha256) |
| **Exact File Size** | `1,041,743 bytes` (**1,017.3 KB** / `~0.99 MiB`) |
| **APK SHA-256 Digest** | `bf84cc41f643e1300c3bdfb08bca0726061a2ef4b25488cb232c1b8eee111cc8` |
| **Signing Keystore** | `my-upload-key.jks` (`alias = upload`, RSA 2048-bit, APK Signature Scheme v2) |
| **Signing Cert SHA-256** | `c585099e00ff5e42e727ae4f0cd2921799950805233b2914f0c63e8bb9362203` |
| **Build Optimization** | R8 Full Mode (`isMinifyEnabled = true`, `isShrinkResources = true`, `isCrunchPngs = true`) |

---

## ✨ What's New in `v6.9.9`

### 1. 🔍 Continuous Inside-Bar Horizontal Searching Animation (Pre-Download & Size Discovery Phase)
- **Removed Static 25%/35% Fallback Progress Bar:**
  - Fixed the issue in `DownloadItemCard` (`app/src/main/java/com/example/ui/components/BrowserSheets.kt`) where a hardcoded `0.35f` fallback caused a static ~25–35% progress bar to appear before the download started or before the video's MB size was identified.
- **`SearchingInsideBarAnimation` Composable:**
  - Whenever a video download has not yet started receiving bytes (`downloadedBytes <= 0L`) or its total MB size has not yet been identified (`totalBytes <= 0L`), the progress bar displays a **continuous horizontal inside-bar searching animation** with a glowing dual-sweep scanner beam and subtle interior guide dots.

### 2. 🐍 Forward-Moving Snake Progress Bar & Live Dynamic Percentage (Active Download Phase)
- **`SnakeDownloadProgressBar` Composable:**
  - Once the video's MB size is discovered (`totalBytes > 0L`) and byte transfer begins (`downloadedBytes > 0L`), the progress bar transitions to a **sinusoidal snake animation moving forward** along the bar from `0%` to `100%`, complete with a glowing leading snake head and illuminated trail, accompanied by a live percentage pill (`1%`–`100%`).

### 3. ⬇️ Exact `v6.9.7` Download, Direct MP4 & Header Probing Parity
- **Unmodified `v6.9.7` Download & Direct MP4 Logic (`NetraDownloadHelper.kt`, `NetraDatabase.kt`, `video_detector.js`, `video_test_lab.html`):**
  - Preserved the exact `v6.9.7` Direct MP4 detection (`video_detector.js`), 5-profile `Range: bytes=0-1` HTTP header probing, `DownloadManager` / HTTP streaming / WebView byte-stream pipeline, `video_test_lab.html` test page, and clean `v6.9.7` download card text & tab switching (`Ongoing` → `Success` / `Failure`) without any unauthorized behavioral changes.
- **Browser Category Support (`AndroidManifest.xml` from `v6.9.8`):**
  - Includes `android.intent.category.APP_BROWSER`, `BROWSABLE`, `VIEW`, `WEB_SEARCH`, `SEND`, and `NDEF_DISCOVERED` so Netra appears alongside Chrome and Brave as a default/clickable Android browser.

