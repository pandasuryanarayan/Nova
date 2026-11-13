# Nova Browser v6.8.0 — Sub-1 MB (<1 MB) Signed Release APK, AMOLED Black Theme & Hindi/UI Polish

## Release Summary (`v6.8.0` • `versionCode = 14`)

- **APK File:** `releases/v6.8.0/NovaBrowser-v6.8.0-release.apk`
- **Package ID:** `com.suryatech.browser.nova`
- **APK Download Size:** `986,223 bytes` (**0.94 MiB / 986.2 KB** — strictly below `1 MB`)
- **Installed Uncompressed Size:** `1,915,465 bytes` (**1.83 MiB / 1.92 MB** — strictly below `5 MB`)
- **SHA-256 Checksum:** `716913511d87d472cf70861884093429d8a211ff80e560cc926aac4290760ff1`

## What's New & Fixed in v6.8.0

1. **Sub-1 MB Signed Release APK (`986,223 bytes`) & `< 5 MB` Installed Size with Original `v6.7.0` Nova Icon**:
   - Preserved the restored `v6.7.0` Nova Browser launcher icon (`img_nova_launcher_fg.jpg` + all legacy `mdpi`–`xxxhdpi` mipmaps) and Nova Home badge untouched.
   - Replaced heavy `material3`, `Room`, `OkHttp`, `Coil`, `DataStore`, and `material-icons-extended` bloat with lightweight Compose Foundation primitives, native Android `EditText` (`NovaEditText`), indexed vector icon cache (`NovaIcons`), compact rule tables (`AdBlocker`), and full R8 shrinking/repackaging with **zero feature loss**.
2. **True AMOLED Black Dark Theme in Normal Mode**:
   - Pure `#000000` AMOLED background with `#050507` / `#101014` dark sheets in Normal Mode dark theme (Light Mode remains default; Incognito keeps Cosmic Obsidian).
3. **Hindi & English Localization Refinements**:
   - Bottom bar 3-dot vertical button reads **सेटिंग्स** in Hindi (`SETTINGS` in English).
   - Bottom bar Saved tab renamed to **BOOKMARK** (**बुकमार्क** in Hindi).
   - Ad-Shield tier names in Hindi set to **न्यूनतम** (Minimal), **अधिक** (Pro), and **अधिकतम** (Max), with **RULES** / **नियम** counts displayed on all three tiers (`Minimal`, `Pro`, and `Max`).
4. **Cleaner Control Center & Bookmarks UI**:
   - Removed the redundant 3-dot/tune badge next to the **Control center** header.
   - Removed the unnecessary *"Long press to add..."* hint banner inside the Bookmarks sheet.
5. **Complete Browser Suite Intact**:
   - Includes **3-Section Downloads Manager** (`Ongoing` • `Success` • `Failure`), **3-Language Switcher** (`System` • `English` • `हिन्दी`), **3-Tier Ad-Blocker** (`58` Minimal / `189` Pro / `220` Max rules), **DevTools** (`Elements`, `Sources`, `Console`, `Network`), Video Sniffer, Background Audio Player, SSL/Cookie Inspector, and Incognito mode.
