# Netra v6.9.1 — Signed Release APK (`< 1 MB`)

- **Package ID:** `com.suryatech.browser.netra`
- **App Name:** `Netra` (English) / `नेत्र` (Hindi)
- **Version:** `6.9.1` (`versionCode = 16`)
- **Signed Release APK:** `releases/v6.9.1/Netra-v6.9.1-release.apk`
- **APK Size:** `990,867` bytes (`967.64 KB` / `0.945 MiB`)
- **Installed Payload Size (Uncompressed):** `1,974,108` bytes (`1.883 MiB`)
- **SHA-256:** `45a91672f4c97d533eaa96dd06446661c278376c93f149a54d15c1da37d8dbd8`

## Highlights
1. **One-Tap "Close All" Tabs Button (`close_all_tabs_button`):**
   - Added a dedicated "Close all" (`सब बंद करें`) button in the header row of `TabsSwitcherSheet`.
   - Safely releases and destroys all open `WebView` instances, clears active session state, resets to a fresh default Netra Home start page, and provides an immediate toast notification (`"All tabs closed"`).
   - Styled with standard M3 danger accents (`palette.dangerBg`, `palette.dangerBorder`, `palette.dangerRed`) and `NovaIcons.DeleteSweep` icon matching the history clearing action.
2. **Tabs Switcher Button Centering & Alignment:**
   - Fixed vertical and horizontal alignment for both the "Private" (`new_incognito_tab_button`) and "New Tab" (`new_tab_button`) action buttons inside `TabsSwitcherSheet`.
   - Content is cleanly centered via `Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center)` with `verticalAlignment = Alignment.CenterVertically` and `horizontalArrangement = Arrangement.Center`, ensuring text and icons align symmetrically inside their respective `48.dp` button surfaces.
3. **Gestural Sheet Dismissal (Close Button Removed):**
   - Removed the close (`×`) button from the Tabs switcher sheet header. Users can seamlessly swipe down to dismiss the bottom sheet, consistent with the main menu sheet and Android Material 3 design patterns.
4. **Smart Video Downloader & Range Header Probing:**
   - Preserves proven `GET bytes=0-1` probing pipeline: extracts exact content size from `Content-Range` headers without sending `HEAD` requests, avoids breaking `Accept-Encoding: identity` headers, and maintains `instanceFollowRedirects = true`.
   - JavaScript `lookupPerformanceResourceSize` immediately pulls buffered transfer size from `window.performance.getEntriesByType('resource')` upon long-press and range fetch.
   - Robust 3-stage download pipeline: DownloadManager with profile referer, direct HTTP streaming with real-time UI/notification progress, and authenticated in-WebView streaming fallback.
5. **Soft-Keyboard Auto-Dismissal:**
   - Instant keyboard dismissal on Enter, Go, Search, Done, or URL bar navigation across Home, Top Bar, and Find-in-Page search fields.
6. **Ultra-Lightweight Release Footprint (`< 1 MB` APK, `< 2 MB` Installed):**
   - R8 full-mode minified, resource-shrunk, V1/V2/V3 signed release APK measuring just `990,867` bytes (`967 KB`), strictly under the `1,000,000` byte threshold.
   - 3-tier cumulative Ad-Blocker (`Minimal`: 58, `Pro`: 235, `Max`: 267 rules), DevTools inspector, SSL certificate viewer, cookie manager, per-domain Chrome-style site configurations, and multi-tab browsing.
