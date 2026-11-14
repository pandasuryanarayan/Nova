# Netra v6.9.2 — Signed Release APK (`< 1 MB`)

- **Package ID:** `com.suryatech.browser.netra`
- **App Name:** `Netra` (English) / `नेत्र` (Hindi)
- **Version:** `6.9.2` (`versionCode = 17`)
- **Signed Release APK:** `releases/v6.9.2/Netra-v6.9.2-release.apk`
- **APK Size:** `993,875` bytes (`970.58 KB` / `0.948 MiB`)
- **Installed Payload Size (Uncompressed):** `1,983,013` bytes (`1.891 MiB`)
- **SHA-256:** `f8edbbe332e6b630220eae251bf201a687fe672d53eab8fefcd5e583b08527c5`

## Highlights
1. **YouTube & Website Page Previews in Tab List (`TabWebsitePreviewCard`):**
   - In the tab list / tab switcher screen, replaced the plain text URL cards with rich, authentic **website page previews**:
     - **YouTube Tabs:** Renders a realistic miniature YouTube web page preview featuring a dark-themed YouTube header with brand logo & search affordance, 16:9 video player container with dark gradient, red center play badge, `"1080p"` quality badge, red seek bar, video title, and channel recommendation metadata (`"m.youtube.com • Recommended"`).
     - **Netra Home Tabs:** Displays a clean start page preview with the Netra eye+globe emblem, search capsule, and colorful quick shortcut indicators.
     - **Standard Web Tabs:** Features an address bar with host and SSL lock indicator, hero card with site initial avatar and title, and structured content preview skeleton lines.
2. **Background Website Shield During Tab Switching:**
   - When opening the tab switcher sheet (`ActiveSheet.TABS`), the underlying live `WebView` is detached from the background so the live whole website (such as an active YouTube video player or webpage) no longer bleeds through or dominates the screen behind the tab list.
   - Expanded the tab switcher container (`heightIn(max = 520.dp)`) providing ample vertical space to scan and manage open tabs comfortably.
3. **Dedicated "Close All Tabs" Button (`close_all_tabs_button`):**
   - Header action button allows one-tap dismissal and cleanup of all open tabs, resetting directly to a fresh Netra Home start page with `"All tabs closed"` feedback toast.
4. **Ultra-Lightweight Production Build (`< 1 MB` APK, `< 2 MB` Installed):**
   - R8 full-mode minified, resource-shrunk, V1/V2/V3 signed release APK measuring `993,875` bytes (`970 KB`), strictly below the `1,000,000` byte budget.
   - 3-tier cumulative Ad-Blocker (Minimal: 58, Pro: 235, Max: 267 rules), direct MP4 range downloader, DevTools, Chrome-style per-site settings, and multi-tab browsing.
