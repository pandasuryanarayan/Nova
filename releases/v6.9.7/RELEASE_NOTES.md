# Netra v6.9.7 — Complete Codebase Rename (`Nova` → `Netra`) & Signed Release APK

**Release Date:** November 15, 2026  
**Application ID:** `com.suryatech.browser.netra`  
**Version:** `6.9.7` (`versionCode = 22`)  
**Signed Release APK:** [`releases/v6.9.7/Netra-v6.9.7-release.apk`](Netra-v6.9.7-release.apk)  
**SHA-256 Checksum:** `c49a001e4cdbed84bde6f4786b90367ad631baebb67b36ef835419f96e20afea`  
**APK Size:** `1,026,187 bytes` (`1,002.1 KB` / `~0.98 MiB` R8-minified & resource-shrunk)

---

## ✨ What's New in v6.9.7

### 1. 🏷️ Complete File & Symbol Rename (`Nova` → `Netra`)
- **Renamed All 8 Source Files:**
  - `NovaDownloadHelper.kt` → `NetraDownloadHelper.kt`
  - `NovaWebViewPool.kt` → `NetraWebViewPool.kt`
  - `NovaDatabase.kt` → `NetraDatabase.kt`
  - `NovaBrowserApp.kt` → `NetraBrowserApp.kt`
  - `NovaHomeContent.kt` → `NetraHomeContent.kt`
  - `NovaPrimitives.kt` → `NetraPrimitives.kt`
  - `NovaStrings.kt` → `NetraStrings.kt`
  - `NovaIcons.kt` → `NetraIcons.kt`
- **Unified All Internal Symbols, Bridges & Persistence Keys:**
  - Updated Kotlin classes, Composables, theme tokens, URL constants (`NETRA_HOME_URL`, `NETRA_VIDEO_LAB_URL`), database/preferences keys (`netra_browser.db`, `netra_browser_prefs`), JavaScript bridges (`NetraVideoBridge`, `NetraDevToolsBridge`, `__netra*`), ProGuard rules, unit/Robolectric tests, and release documentation (`releases/v6.9.4/RELEASE_NOTES.md`) cleanly with zero broken references or regressions.

### 2. 📦 R8-Optimized Signed Release APK (`v6.9.7`)
- Built with R8 full-mode minification (`isMinifyEnabled = true`), resource shrinking (`isShrinkResources = true`), PNG crunching (`isCrunchPngs = true`), and V1/V2/V3 signing verification.
