# Nova Browser

![Version](https://img.shields.io/badge/version-v6.3.0-00E5FF?style=flat-square)
![Platform](https://img.shields.io/badge/platform-Android%207.0%2B%20(API%2024--36)-00E676?style=flat-square)
![APK Size](https://img.shields.io/badge/APK%20size-2.38%20MB%20(R8%20Signed)-8C62FF?style=flat-square)
![License](https://img.shields.io/badge/license-GNU%20GPL%20v3.0-blue?style=flat-square)

**Nova Browser** (`com.suryatech.browser.nova`) is a fast, lightweight, privacy-focused Android web browser built with **Kotlin** and **Jetpack Compose (Material Design 3)**, engineered to run smoothly even on budget Android devices. It features a Chrome-style centric Home screen with real disk-cached website icons and a 3-dot Edit/Delete menu on every Quick Shortcut, a smooth app-wide Light Orange theme transition when Incognito is ON, a minimal Home bottom bar (Tab View & 3-Dot Menu only), and an on-demand 2-bar website layout (Top Website Title, Lock & 1-Tap Bookmark Bar with a compact Fall-Down URL Drawer featuring inline Copy/Edit/Share icon actions + Bottom 7-Action Toolbar with smooth Left-to-Right slide animation).

---

## 📦 Download Signed Release APK (`v6.3.0`)

The latest signed, R8-minified release APK is included directly in this repository and automatically published to **GitHub Releases**:

- **Direct APK in Repo**: [`releases/v6.3.0/NovaBrowser-v6.3.0-release.apk`](releases/v6.3.0/NovaBrowser-v6.3.0-release.apk)
- **Release Notes**: [`releases/v6.3.0/RELEASE_NOTES.md`](releases/v6.3.0/RELEASE_NOTES.md)
- **Package ID**: `com.suryatech.browser.nova`
- **Version Name / Code**: `v6.3.0` (`versionCode = 9`, `versionName = "6.3.0"`)
- **Size**: `2.38 MB` (`2,491,464 bytes`)
- **SHA-256**: `ddfcdb2da49014844e3a6408b3e6228cc21c92a320819a0c7211c7fb6ab3564c`

---

## ✨ Key Features

### 1. Chrome-Style Centric Home & On-Demand Dual-Bar Website Layout
- **Centric Home Screen**: Prominent centered Nova Browser app icon above a large `Nova Browser` title and middle search/URL omnibox bar. On the Home screen, the bottom bar stays minimal with only **Tab View (`[N]`)** and **3-Dot Menu**.
- **Top Website Bar (On Demand)**: Shows the **Lock** icon, the **Website Title** (set by the website), a **1-Tap Bookmark Button** right next to the dropdown button, and a **Dropdown Link Button** (`▼`/`▲`) that opens a fall-down drawer with the full website link (and vanishes on re-click).
- **Lock Inspector Sheet**: Clicking the Lock icon lets you inspect the **Page SSL/TLS Certificate**, view **Available Cookies** (`CookieManager`), and manage **Site Configuration** (showing all enabled settings & toggles).
- **Bottom Website Toolbar (Smooth Left-to-Right Slide)**: When a website opens, **Back (Left Arrow)**, **Home**, **Inspect (DevTools)**, **Video Detector**, and **Refresh/Stop** smoothly slide in from **Left to Right** alongside **Tab View (`[N]`)** and **3-Dot Menu**, and reverse-slide **Right to Left** when returning to the Home screen.

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
- **UI Framework**: Jetpack Compose + Material Design 3 (Lightweight flat tonal surfaces & 150ms animations for smooth 60/120fps budget phone performance)
- **Architecture**: MVVM (`BrowserViewModel` + reactive `StateFlow` + `NovaWebViewPool`)
- **Local Persistence**: Jetpack Room (`NovaDatabase`) & Jetpack DataStore Preferences (`BrowserPreferences`)
- **Networking & Media**: OkHttp 4, Android `WebView`, Android System `DownloadManager`
- **Build & Optimization**: Gradle Kotlin DSL (`.gradle.kts`), KSP, R8 Code Shrinking & Resource Optimization (~3.05 MB release APK)

---

## 📂 Project Structure

```text
├── LICENSE                            # GNU General Public License v3.0 (GPL-3.0)
├── README.md                          # Project documentation
├── releases/v5.0.0/
│   ├── NovaBrowser-v5.0.0-release.apk # Signed v5.0.0 Release APK (3.05 MB)
│   └── RELEASE_NOTES.md               # Release v5.0.0 changelog & checksums
├── .github/workflows/
│   └── release.yml                    # Auto-publishes GitHub Release v5.0.0 on push
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
