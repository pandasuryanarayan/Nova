# Nova Browser

![Version](https://img.shields.io/badge/version-v4.0.0-00E5FF?style=flat-square)
![Platform](https://img.shields.io/badge/platform-Android%207.0%2B%20(API%2024--36)-00E676?style=flat-square)
![APK Size](https://img.shields.io/badge/APK%20size-3.05%20MB%20(R8%20Signed)-8C62FF?style=flat-square)
![License](https://img.shields.io/badge/license-GNU%20GPL%20v3.0-blue?style=flat-square)

**Nova Browser** is a fast, lightweight, privacy-focused Android web browser built with **Kotlin** and **Jetpack Compose (Material Design 3)**. It features a clean 2-bar website layout (Top Website Title & Lock Bar with Fall-Down URL Drawer + Bottom 7-Action Toolbar), multi-tab & Incognito browsing, a real-time Ad-Block Shield with a detailed blocked-requests inspector log, true 1280px Desktop Site emulation, built-in Desktop DevTools (`Elements`, `Sources`, `Console`, `Network`), and a smart long-press MP4 video downloader powered by the Android System `DownloadManager`.

---

## 📦 Download Signed Release APK (`v4.0.0`)

The latest signed, R8-minified release APK is included directly in this repository and automatically published to **GitHub Releases**:

- **Direct APK in Repo**: [`releases/v4.0.0/NovaBrowser-v4.0.0-release.apk`](releases/v4.0.0/NovaBrowser-v4.0.0-release.apk)
- **Release Notes**: [`releases/v4.0.0/RELEASE_NOTES.md`](releases/v4.0.0/RELEASE_NOTES.md)
- **Version Name / Code**: `v4.0.0` (`versionCode = 4`, `versionName = "4.0.0"`)
- **Size**: `3.05 MB` (`3,198,508 bytes`)
- **SHA-256**: `01a881a17f99d3a61762051cefe0897b5346701d7d1cf91b47b74ac8428bcbfe`

---

## ✨ Key Features

### 1. Dual-Bar Website Layout & Lock Inspector
- **Top Website Bar**: Shows ONLY the **Lock** icon, the **Website Title** (set by the website), and a **Dropdown Link Button** (`▼`/`▲`) that opens a fall-down drawer with the full website link (and vanishes on re-click).
- **Lock Inspector Sheet**: Clicking the Lock icon lets you inspect the **Page SSL/TLS Certificate**, view **Available Cookies** (`CookieManager`), and manage **Site Configuration** (showing all enabled settings & toggles).
- **Bottom 7-Button Toolbar**: Dedicated bottom bar with **Back (Left Arrow)**, **Home**, **Inspect (DevTools)**, **Video Detector**, **Refresh/Stop**, **Tab View (`[N]`)**, and **3-Dot Menu**.

### 2. Multi-Tab & Private Incognito Browsing
- **Pooled WebView Architecture**: Instant tab switching without reloading active pages.
- **Incognito Mode**: Isolated private tabs where browsing history, cookies, and site data are never persisted to local storage.

### 3. Ad-Block Shield & Detailed Blocked Ads Inspector
- **Real-Time Request Interception**: Blocks intrusive ad networks, tracking pixels, and analytics scripts at the `WebViewClient.shouldInterceptRequest` layer.
- **Blocked Ads Inspector Sheet**: View the exact list of blocked ad URLs, domains, matched filter rules, and timestamps in real time, with one-tap copy or log clearing.

### 4. Smart Long-Press & Floating MP4 Video Downloader
- **Automatic Media Sniffing**: Detects HTML5 `<video>` streams and direct media URLs (`.mp4`, `.webm`, `.mkv`, `.mov`) on any webpage.
- **Long-Press Video Context Sheet**: Long-press any video or tap the **Video Detector** button to inspect real file size, resolution, and duration.
- **System DownloadManager Integration**: Hands off downloads directly to the native Android System `DownloadManager` with full browser headers (`Cookie`, `Referer`, `User-Agent`) for reliable background downloading and system notification progress.

### 5. True 1280px Desktop Site Mode
- **Desktop User-Agent & Viewport Override**: Spoofs a desktop Chrome User-Agent (`navigator.platform`, `navigator.userAgentData`, `window.screen`) and injects a `width=1280` viewport meta tag so responsive websites render their genuine desktop layout.

### 6. Built-In Desktop DevTools (`F12` Inspector)
- **Elements / DOM Tree**: Inspect live HTML markup and computed styles.
- **Sources Explorer**: Browse and view loaded page HTML, external `<script src="...">`, and `<link rel="stylesheet">` source code with line numbers.
- **JavaScript Console**: View `console.log` / `warn` / `error` output and execute live JavaScript expressions against the active page.
- **Network Monitor**: Inspect network requests, status codes, MIME types, and timings.

---

## 🛠️ Tech Stack & Architecture

- **Language**: 100% Kotlin
- **UI Framework**: Jetpack Compose + Material Design 3 (Dynamic Dark/Light Theme, Edge-to-Edge WindowInsets)
- **Architecture**: MVVM (`BrowserViewModel` + reactive `StateFlow` + `NovaWebViewPool`)
- **Local Persistence**: Jetpack Room (`NovaDatabase`) & Jetpack DataStore Preferences (`BrowserPreferences`)
- **Networking & Media**: OkHttp 4, Android `WebView`, Android System `DownloadManager`
- **Build & Optimization**: Gradle Kotlin DSL (`.gradle.kts`), KSP, R8 Code Shrinking & Resource Optimization (~3.05 MB release APK)

---

## 📂 Project Structure

```text
├── LICENSE                            # GNU General Public License v3.0 (GPL-3.0)
├── README.md                          # Project documentation
├── releases/v4.0.0/
│   ├── NovaBrowser-v4.0.0-release.apk # Signed v4.0.0 Release APK (3.05 MB)
│   └── RELEASE_NOTES.md               # Release v4.0.0 changelog & checksums
├── .github/workflows/
│   └── release.yml                    # Auto-publishes GitHub Release v4.0.0 on push
└── app/src/main/java/com/example/
    ├── MainActivity.kt              # Edge-to-Edge Activity entry point
    ├── browser/
    │   ├── AdBlockEngine.kt         # Domain/rule ad & tracker blocker
    │   ├── NovaDownloadHelper.kt    # Media metadata prober & System DownloadManager handoff
    │   └── NovaWebViewPool.kt       # Pooled WebView manager, JS bridges & Desktop Mode spoofing
    ├── data/
    │   ├── BrowserModels.kt         # Tab, Media, Ad-Block, DevTools & UI state models
    │   ├── BrowserPreferences.kt    # DataStore preferences (Search engine, Ad-Block, Zoom)
    │   └── NovaDatabase.kt          # Room entities & DAOs (Bookmarks, History, Downloads)
    ├── ui/
    │   ├── NovaBrowserApp.kt        # Root Compose screen, sheets & navigation handlers
    │   ├── components/
    │   │   ├── BottomBrowserBar.kt  # Wide bottom search/URL bar & navigation controls
    │   │   ├── BrowserMenuSheet.kt  # Browser tools, Desktop Mode toggle & Ad-Block shield
    │   │   ├── BrowserSheets.kt     # Tabs, Bookmarks, History, Downloads & Blocked Ads sheets
    │   │   ├── DevToolsSheet.kt     # In-app Desktop DevTools inspector
    │   │   ├── NovaHomeContent.kt   # Speed-dial home screen & privacy shield stats card
    │   │   └── VideoActionSheet.kt  # Long-press video preview & MP4 download sheet
    │   └── theme/
    │       ├── Color.kt             # Midnight Sapphire & Electric Cyan palette
    │       ├── Theme.kt             # Material 3 theme configuration
    │       └── Type.kt              # Custom Google Fonts typography
    └── viewmodel/
        └── BrowserViewModel.kt      # Central state holder for tabs, downloads, DevTools & persistence
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK 11+**
- **Android SDK 36** (Min SDK 24 / Android 7.0+)

### Build & Run (Debug)
```bash
./gradlew :app:assembleDebug
```

### Build Signed Release APK (R8 Minified)
```bash
STORE_PASSWORD=android KEY_PASSWORD=android ./gradlew :app:assembleRelease
```

---

## 📄 License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)** — see the [LICENSE](LICENSE) file for details.
