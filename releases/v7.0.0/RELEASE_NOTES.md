# 👁️ Netra `v7.0.0` — Advanced Settings Pop-Out, Password Manager & Localized Android Info

**Release Date:** November 18, 2026  
**Application ID:** `com.suryatech.browser.netra`  
**Version:** `7.0.0` (`versionCode = 25`)  
**Min SDK:** `24` (Android 7.0+) &nbsp;•&nbsp; **Target SDK:** `36`

---

## 📦 Signed Release APK Details

| Property | Value |
| :--- | :--- |
| **APK Path** | [`releases/v7.0.0/Netra-v7.0.0-release.apk`](Netra-v7.0.0-release.apk) |
| **SHA-256 Checksum File** | [`releases/v7.0.0/Netra-v7.0.0-release.apk.sha256`](Netra-v7.0.0-release.apk.sha256) |
| **Exact File Size** | `1,092,543 bytes` (**1,066.9 KB** / `~1.04 MiB`) |
| **APK SHA-256 Digest** | `62cb0e35b47dba4b5d9dda94a24ad7119b789a6ba067e2719808ab4f1e75c3cf` |
| **Signing Keystore** | `my-upload-key.jks` (`alias = upload`, RSA 2048-bit, APK Signature Scheme v2) |
| **Signing Cert SHA-256** | `c585099e00ff5e42e727ae4f0cd2921799950805233b2914f0c63e8bb9362203` |
| **Build Optimization** | R8 Full Mode (`isMinifyEnabled = true`, `isShrinkResources = true`, `isCrunchPngs = true`) |

---

## ✨ What's New in `v7.0.0`

### 1. ⚙️ Advanced Settings Pop-Out Overlay & Interactive Close Toggle
- **Replaced Top Quick-Action Copy Link Button (`BrowserSheets.kt`):**
  - Replaced the top quick-action **Copy Link** tile inside the Settings control center sheet with the new **Advanced** (`AdvancedSettingsToggleTile`) button featuring the custom `ic_advanced_settings.png` icon, dynamically tinted to adapt cleanly across **Warm Light Orange**, **AMOLED Dark**, and **Incognito Cyan** themes.
- **Pop-Out / Overlap Surface & Morphing Close (`✕`) Button:**
  - Tapping **Advanced** smoothly pops an **Advanced Settings** overlay directly above the existing Settings sheet content, while converting that same top-right toggle tile into a **Cross / Close (`✕`)** icon so users can dismiss the overlay cleanly with one tap.

### 2. 🔐 Built-In Password Manager (SQLite Vault + WebView Auto-Save & Autofill)
- **Full Credential Vault (`AdvancedSettingsOverlay.kt`, `NetraDatabase.kt`, `BrowserRepository.kt`, `BrowserViewModel.kt`):**
  - Upgraded the local SQLite database schema (`v2 → v3`) with a dedicated `passwords` table supporting full **Add**, **Edit**, **Delete**, **Search**, **Show/Hide Password** (eye toggle), **One-Tap Copy**, and a **Strong Password Generator**.
- **Automatic Login Capture & One-Tap Autofill (`NetraWebViewPool.kt`, `NetraBrowserApp.kt`):**
  - Added a toggleable **Password Manager** switch (enabled by default), automatic WebView login-form detection with a **Save Password** confirmation dialog, and **1-Tap Autofill** directly into active website login forms.

### 3. 🌐 Shifted App Language Settings & Localized Netra Android Header
- **App Language Moved to Advanced Settings:**
  - Relocated the **App Language** (`System` / `English` / `हिन्दी`) selector from the main Settings sheet into the **Advanced Settings** overlay for a cleaner primary menu layout.
- **Localized App Title & Android Version Banner:**
  - Added a dedicated identity banner inside Advanced Settings displaying **Netra** in English (`Netra`) or Hindi (`नेत्र`) dynamically according to the active language, paired with the literal **`Android`** label and live app version (`Android • v7.0.0`).

### 4. ⬇️ Preserved `v6.9.7` / `v6.9.9` Download Parity & Progress Animations
- **Unmodified Download Pipeline (`NetraDownloadHelper.kt`, `video_detector.js`, `video_test_lab.html`):**
  - Kept the exact `v6.9.7` Direct MP4 detection, 5-profile `Range: bytes=0-1` HTTP header probing, `DownloadManager` / HTTP streaming / WebView byte-stream pipeline, continuous inside-bar searching animation, and forward-moving snake progress bar completely intact.
