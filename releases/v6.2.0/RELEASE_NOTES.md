# Nova Browser v6.2.0 — Signed Release APK

**Package ID:** `com.suryatech.browser.nova`  
**Version:** `6.2.0` (`versionCode = 8`)  
**Build Type:** Signed Release (`isMinifyEnabled = true`, `isShrinkResources = true`)  
**APK Size:** `2.38 MB` (`2,491,464 bytes`)  
**SHA-256:** `7ab765d751b9f3fc24babb5e05509f0a9d61d33c76195af2aeef85f3cd4fa340`  
**License:** GNU General Public License v3.0 (`GPL-3.0`)

## What's New in v6.2.0
- **Smooth App-Wide Light Orange Theme Animation When Incognito is ON**:
  - Clicking **Incognito** triggers a smooth full-screen radial wave animation and animates the entire app's color palette to a **Light Orange Theme** (`NovaIncognitoLightOrangeColorScheme`).
  - Turning **Incognito OFF** smoothly animates the entire app back to the default color scheme.
- **Real Website Icons for Quick Shortcuts (Fetch-Once & Mobile Disk Cache)**:
  - Quick Shortcuts now display the real website/app icon (e.g., Google, YouTube, DuckDuckGo, Wikipedia, GitHub, Reddit, etc.) instead of plain text letters.
  - Icons are fetched once on demand and persisted to internal mobile storage cache (`cacheDir/shortcut_icons/`) with a tiny bounded 32-item `LruCache` (~250 KB max RAM) so there is zero continuous fetching and zero memory or CPU overhead.
- **Individual 3-Dot Menu (`⋮`) on Every Quick Shortcut (Edit & Delete)**:
  - Every Quick Shortcut (including all pre-given default websites) now includes a top-right **3-dot menu (`⋮`)** with **Edit** (change title or URL) and **Delete** options.
  - Deleted pre-given default websites remain permanently removed.
- **Renamed Top Bar Fall-Down Button to `"Edit"`**:
  - In the Top Website Bar fall-down link drawer, the 3 options are now concisely labeled **`Copy Link`**, **`Edit`**, and **`Share`**.
