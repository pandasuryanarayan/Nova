# Netra v6.9.2 — Signed Release APK (`< 1 MB`)

- **Package ID:** `com.suryatech.browser.netra`
- **App Name:** `Netra` (English) / `नेत्र` (Hindi)
- **Version:** `6.9.2` (`versionCode = 17`)
- **Signed Release APK:** `releases/v6.9.2/Netra-v6.9.2-release.apk`
- **APK Size:** `995,903` bytes (`972.56 KB` / `0.950 MiB`)
- **Installed Payload Size (Uncompressed):** `1,985,381` bytes (`1.893 MiB`)
- **SHA-256:** `1a178771e102fe071d2ffebf0f99a6f91071f3c82a1e6b718a959934215d2791`

## Highlights
1. **Default Tabs & Private Tabs Switcher + Header Action Bar:**
   - Replaced the two action buttons below the header with **Default Tabs** (`सामान्य टैब`) and **Private Tabs** (`प्राइवेट टैब`) filter buttons (`default_tabs_button` and `private_tabs_button`).
   - By default, **Default Tabs** is active and displays all standard tabs; tapping **Private Tabs** switches the list to display all incognito/private tabs.
   - Shifted the **New Tab** (`नया टैब`) button (`new_tab_button`) to the left side of the **Close all** (`सब बंद करें`) button (`close_all_tabs_button`) in the Tabs sheet header row. Tapping **New Tab** opens a new standard tab when viewing Default Tabs or a new private tab when viewing Private Tabs.
2. **YouTube & Website Page Previews in Tab List (`TabWebsitePreviewCard`):**
   - Replaced plain domain text cards in the tab list with miniature **website page previews**:
     - **YouTube Tabs:** Renders a miniature YouTube web page preview featuring a dark-themed YouTube header with brand logo & search icon, 16:9 video player card with dark gradient, red center play badge, `"1080p"` quality badge, red progress bar, video title, and channel recommendation metadata (`"m.youtube.com • Recommended"`).
     - **Netra Home Tabs:** Displays a miniature start page preview with the Netra emblem, search capsule, and quick shortcut indicators.
     - **Standard Web Tabs:** Displays a miniature browser bar with domain and SSL lock indicator, hero card with site initial avatar and title, and structured content preview lines.
3. **Background Website Shield During Tab Switching:**
   - When opening the tab switcher sheet (`ActiveSheet.TABS`), the underlying live `WebView` is detached from the background so the live website (such as an active YouTube video player or webpage) does not bleed through behind the tab list.
4. **Ultra-Lightweight Production Build (`< 1 MB` APK, `< 2 MB` Installed):**
   - R8 full-mode minified, resource-shrunk, V1/V2/V3 signed release APK measuring `995,903` bytes (`972 KB`), strictly below the `1,000,000` byte budget.
   - 3-tier cumulative Ad-Blocker (`Minimal`: 58, `Pro`: 235, `Max`: 267 rules), direct MP4 range downloader, DevTools, Chrome-style per-site settings, and multi-tab browsing.
