# Netra v6.9.3 — Real Webpage Previews in Tab Switcher & Non-Destructive Incognito Tab Switching (<1 MB) Signed Release APK

## ✨ What's New in v6.9.3 (`versionCode = 18`)

1. **Real Webpage Previews in Tab List (`TabPreviewCache.kt` & `BrowserSheets.kt`)**:
   - Replaced static demo placeholders in the Tabs Switcher (`TabsSwitcherSheet`) with **real rendered webpage previews** inside each tab card (just like Chrome's tab switcher and similar to how real website icons work in `ShortcutIconCache`).
   - Live `WebView` viewports are captured automatically into high-clarity `420×280` bitmap thumbnails (`TabPreviewCache.captureWebView`) during page load progress (`>= 75%`), `onPageFinished` (with delayed SPA captures), and immediately before opening the Tabs sheet or switching tabs.
   - Non-incognito previews are cached in memory (`LruCache`) and on disk (`filesDir/tab_previews/`) with real webpage screenshot fallback (`s.wordpress.com/mshots/v1/`) so every open website tab (e.g., `github.com`, `photos.google.com`, `youtube.com`) displays its actual webpage preview inside the tab card alongside its real favicon (`rememberShortcutIcon`).

2. **Preserve Existing Tabs When Toggling Incognito Mode (`BrowserViewModel.kt`)**:
   - Toggling Incognito ON or OFF no longer deletes or mutates existing tabs (including Home tabs).
   - Both **Default Tabs** and **Private Tabs** remain intact in their respective tab lists; toggling Incognito smoothly shifts to the most recent existing tab of the target mode (`switchTab`), or creates and shifts to a new tab of that mode (`openNewTab`) if none exists yet.

3. **Tab Switcher Filter & Header Controls**:
   - Bottom bar features **Default Tabs** and **Private Tabs** mode filter buttons with live tab count badges.
   - Header bar places **+ New Tab** immediately to the left of **Close all** for quick access.

---

## 📦 Release Artifact Details

| Property | Value |
| :--- | :--- |
| **Application ID** | `com.suryatech.browser.netra` |
| **Version** | `6.9.3` (`versionCode = 18`) |
| **APK File** | `releases/v6.9.3/Netra-v6.9.3-release.apk` |
| **APK Size (Compressed)** | `998,039 bytes` (`974.6 KB` — strictly `< 1 MB`) |
| **Installed Payload (Uncompressed)** | `1,986,541 bytes` (`1.89 MB` — strictly `< 5 MB`) |
| **SHA-256 Checksum** | `202c96e13fa67fafc9f30af10762b2c89f73967197d751c42154d63dab705ab8` |
| **Signing Certificate** | `CN=Nova Browser, OU=Mobile, O=Nova, L=SF, ST=CA, C=US` (`SHA-256: c585099e00ff5e42e727ae4f0cd2921799950805233b2914f0c63e8bb9362203`) |
