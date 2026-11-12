# Nova Browser v6.5.0 — Official Signed Release

- **Package ID (`applicationId`)**: `com.suryatech.browser.nova`
- **Version**: `v6.5.0` (`versionCode = 11`, `versionName = "6.5.0"`)
- **License**: GNU General Public License v3.0 (`GPL-3.0`)
- **Build Type**: Signed Release APK (`R8` Code Minification + Resource Shrinking + PNG Crunching Enabled)
- **File Size**: `2.38 MB` (`2,491,464 bytes`)
- **SHA-256**: `9b799176ef77955686bd6db27de1428e5c01211fc962d8438912718f8bc0a399`

## What's New in v6.5.0
1. **Perfect Theme & Icon Consistency in Both Incognito & Standard Modes**:
   - The **Lock Icon Button** now uses `MaterialTheme.colorScheme.primary` with a high-contrast container tint in both **Standard Mode** and **Incognito Mode**, rendering cleanly and clearly visible against both light and dark backgrounds.
   - All toolbar navigation and fall-down action icons (**Back**, **Home**, **Inspect**, **Video Detector**, **Refresh/Stop**, **Tab View**, **3-Dot Menu**, **Copy**, **Edit**, **Share**) now consistently derive from `MaterialTheme.colorScheme.onSurface` and `MaterialTheme.colorScheme.primary`.
2. **Normal Video Detector Button**:
   - The **Video Detector (Camera) button** in the bottom website toolbar now renders normally like all other bottom bar icons, removing the highlighted rounded container box.
3. **Enhanced Google Trust & Anti-Bot Protection (No CAPTCHA Loop)**:
   - Sanitized user agent to a genuine full Chrome mobile browser signature (eliminating the `Version/4.0` WebView token that triggers anti-bot CAPTCHA challenges on Google search).
   - Added search engine & reCAPTCHA whitelisting to `AdBlocker` so verification scripts and core search assets are never blocked or broken by ad-block filters.
   - Enabled `safeBrowsingEnabled` and `setGeolocationEnabled` support to match native Chrome device behavior.
