# 👁️ Netra `v6.9.8` — Full Android Browser Category & Default Browser Role Recognition

**Release Date:** November 17, 2026  
**Application ID:** `com.suryatech.browser.netra`  
**Version:** `6.9.8` (`versionCode = 23`)  
**Min SDK:** `24` (Android 7.0+) &nbsp;•&nbsp; **Target SDK:** `36`

---

## 📦 Signed Release APK Details

| Property | Value |
| :--- | :--- |
| **APK Path** | [`releases/v6.9.8/Netra-v6.9.8-release.apk`](Netra-v6.9.8-release.apk) |
| **SHA-256 Checksum File** | [`releases/v6.9.8/Netra-v6.9.8-release.apk.sha256`](Netra-v6.9.8-release.apk.sha256) |
| **Exact File Size** | `1,028,435 bytes` (**1,004.3 KB** / `~0.98 MiB`) |
| **APK SHA-256 Digest** | `89d454c19e5968a39d7383893854e609abe7d6a6cc03767303d77d8ef46bbad4` |
| **Signing Keystore** | `my-upload-key.jks` (`alias = upload`, RSA 2048-bit, APK Signature Scheme v2) |
| **Signing Cert SHA-256** | `c585099e00ff5e42e727ae4f0cd2921799950805233b2914f0c63e8bb9362203` |
| **Build Optimization** | R8 Full Mode (`isMinifyEnabled = true`, `isShrinkResources = true`, `isCrunchPngs = true`) |

---

## ✨ What's New in `v6.9.8`

### 1. 🌐 Full Android Browser Category & Default Browser Role (`RoleManager.ROLE_BROWSER`) Recognition
- **System Browser Category (`Intent.CATEGORY_APP_BROWSER`):**
  - Added `<category android:name="android.intent.category.APP_BROWSER" />` and `<category android:name="android.intent.category.DEFAULT" />` to `MainActivity` in `app/src/main/AndroidManifest.xml` alongside `android:appCategory="productivity"`.
  - Netra is now recognized in the **Browser** app category by Android launchers, app drawers, and system app pickers alongside Chrome, Brave, Firefox, and Edge.
- **Android Default Browser Role Qualification (`android.app.role.BROWSER`):**
  - Configured a dedicated scheme-only `<intent-filter>` (`ACTION_VIEW` + `CATEGORY_DEFAULT` + `CATEGORY_BROWSABLE` for `http`, `https`, `about`, and `netra` schemes without restrictive MIME filters) so AOSP `BrowserRoleBehavior` lists **Netra** in **Settings → Apps → Default apps → Browser app** and in the system "Open with" link chooser when clicking any web link.
- **HTML / XHTML Document & Local File Support:**
  - Added a dedicated MIME-aware `<intent-filter>` supporting `text/html`, `text/plain`, `application/xhtml+xml`, and `application/vnd.wap.xhtml+xml` over `http`, `https`, `about`, `content`, and `file` schemes.
- **System Web Search, Text Selection Search & Share-to-Browser:**
  - Added `<intent-filter>` declarations for `android.intent.action.WEB_SEARCH`, `android.intent.action.SEARCH`, `android.intent.action.MEDIA_SEARCH`, `android.intent.action.SEND` (`text/plain`), and `android.nfc.action.NDEF_DISCOVERED` (`http`/`https`).

### 2. ⚡ Seamless External Link & Search Intent Routing
- **`MainActivity.kt` (`extractTargetFromBrowserIntent`):**
  - Handles `ACTION_VIEW`, `ACTION_NDEF_DISCOVERED`, `ACTION_WEB_SEARCH`, `ACTION_SEARCH`, `INTENT_ACTION_MEDIA_SEARCH`, and `ACTION_SEND` (`EXTRA_TEXT`) intents on both cold start (`savedInstanceState == null`) and warm start (`onNewIntent`).
- **`BrowserViewModel.kt` (`openExternalLinkOrSearch`) & `BrowserUrlUtils.kt` (`extractExternalTarget`):**
  - When Netra is launched from a clicked link on a fresh start (single blank Home tab), the link loads directly into the active tab without leaving an extra empty Home tab behind.
  - When Netra already has active browsing tabs open, clicking an external link opens and switches to a new tab while preserving all existing tabs.
  - Automatically extracts embedded `http://` / `https://` URLs from shared text strings (`ACTION_SEND`).
