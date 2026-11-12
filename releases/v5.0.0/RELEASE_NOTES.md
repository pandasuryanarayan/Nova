# Nova Browser v5.0.0 — Release Notes

- **Version Name**: `5.0.0` (`v5.0.0`)
- **Version Code**: `5`
- **Package ID**: `com.suryatech.browser.nova`
- **License**: GNU General Public License v3.0 (`GPL-3.0`)
- **Min / Target SDK**: API 24 (Android 7.0) – API 36 (Android 16)
- **Signed Release APK**: `NovaBrowser-v5.0.0-release.apk`
- **Compressed Size**: `3.05 MB` (`3,198,500 bytes`)
- **SHA-256 Checksum**: `ba98a4fb75fec3e68da204dca3e06a219100fdb950d5cbfdb3e05e45e8b9f094`

## What's New in v5.0.0
1. **New Package ID (`com.suryatech.browser.nova`)**:
   - Updated official application ID to `com.suryatech.browser.nova`.
2. **1-Tap Bookmark Toggle in Top Website Bar & Dedicated Bookmarks Manager in 3-Dot Menu**:
   - Placed the **1-Tap Bookmark Toggle Button** right next to the dropdown button on the right side of the Top Website Bar so any open website can be bookmarked or unbookmarked in a single tap.
   - Changed the first quick-action button in the 3-Dot Menu to **Bookmarks** (opens the Bookmarks Manager sheet to browse/manage all saved bookmarks) and removed redundant `Detect Videos` / `Inspect` menu buttons since they are always accessible on the bottom website toolbar.
3. **Streamlined Home Screen & Lightweight 60/120fps Budget-Phone Optimization**:
   - Removed the "Long-Press Video Downloader • Instant Test Sites" banner from the Home screen.
   - Replaced heavy nested elevation surfaces with lightweight flat tonal containers, keyed `LazyColumn` items, and 150ms hardware-friendly transitions for smooth performance on budget Android devices.
4. **Dual-Bar Website Layout & Lock Inspector**:
   - **Top Bar**: Shows the **Lock** button, the **Website Title**, the **1-Tap Bookmark** button, and the **Dropdown Link Button** (`▼`/`▲`) that expands a smooth fall-down panel displaying the full website URL (and vanishes when clicked again).
   - **Lock Inspector Sheet**: Clicking the Lock icon opens a 3-tab inspector to view the **Page SSL/TLS Certificate**, **Available Site Cookies**, and **Site Configuration**.
   - **Bottom Toolbar**: Dedicated 7-button bar with **Back (Left Arrow)**, **Home**, **Inspect (DevTools)**, **Video Detector**, **Refresh/Stop**, **Tab View**, and **3-Dot Menu**.
