# Nova Browser v3.0 — Release Notes

- **Version Name**: `3.0` (`v3.0`)
- **Version Code**: `3`
- **Package ID**: `com.aistudio.novabrowser.xkvpzr`
- **Min / Target SDK**: API 24 (Android 7.0) – API 36 (Android 16)
- **Signed Release APK**: `NovaBrowser-v3.0-release.apk`
- **Compressed Size**: `3.05 MB` (`3,198,508 bytes`)
- **SHA-256 Checksum**: `71ff01d9903218e3cb0077c0c0529d09fc26ebca8d606d53672dfd8a1acfe03a`

## What's New in v3.0
1. **Dual-Bar Website Layout (Top Website Header + Bottom Action Toolbar)**:
   - **Top Bar**: Shows the **Lock** button, the **Website Title** (set by the website), and a **Dropdown Link Button** that opens a smooth fall-down panel displaying the full website link (and vanishes when clicked again).
   - **Lock Inspector Sheet**: Clicking the Lock icon opens a 3-tab inspector to view the **Page SSL/TLS Certificate**, **Available Site Cookies**, and **Site Configuration** (showing all enabled capabilities & toggles).
   - **Bottom Toolbar**: Dedicated 7-button bar with **Back (Left Arrow)**, **Home**, **Inspect (DevTools)**, **Video Detector**, **Refresh/Stop**, **Tab View**, and **3-Dot Menu**.
2. **Clean, Streamlined Architecture (VPN-Free Build)**:
   - Removed all VPN services, background tunnels, and permissions for a faster, lighter ~3.0 MB R8-minified binary.
3. **True 1280px Desktop Site Mode**:
   - Full desktop User-Agent (`X11; Linux x86_64`), `navigator.platform` spoofing, and `width=1280` viewport emulation.
4. **Ad-Block Shield & Detailed Blocked Ads Log**:
   - Real-time ad/tracker blocking with an interactive Blocked Ads Inspector sheet displaying blocked URLs, domains, matched rules, and timestamps.
5. **Built-In Desktop DevTools (`F12` Inspector)**:
   - Live `Elements` DOM inspector, `Sources` viewer, interactive JavaScript `Console`, `Network` monitor, and `Storage` manager.
6. **Smart Long-Press MP4 Video Downloader**:
   - Automatic `<video>` stream detection with direct handoff to the Android System `DownloadManager`.
