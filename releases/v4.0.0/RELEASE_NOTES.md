# Nova Browser v4.0.0 — Release Notes

- **Version Name**: `4.0.0` (`v4.0.0`)
- **Version Code**: `4`
- **Package ID**: `com.aistudio.novabrowser.xkvpzr`
- **License**: GNU General Public License v3.0 (`GPL-3.0`)
- **Min / Target SDK**: API 24 (Android 7.0) – API 36 (Android 16)
- **Signed Release APK**: `NovaBrowser-v4.0.0-release.apk`
- **Compressed Size**: `3.05 MB` (`3,198,508 bytes`)
- **SHA-256 Checksum**: `01a881a17f99d3a61762051cefe0897b5346701d7d1cf91b47b74ac8428bcbfe`

## What's New in v4.0.0
1. **Dual-Bar Website Layout (Top Website Header + Bottom Action Toolbar)**:
   - **Top Bar**: Displays ONLY the **Lock** button, the **Website Title** (set by the website), and a **Dropdown Link Button** (`▼`/`▲`) that expands a smooth fall-down panel showing the full website URL (and vanishes when clicked again).
   - **Lock Inspector Sheet**: Clicking the Lock icon opens a 3-tab inspector to view the **Page SSL/TLS Certificate** (X.509 Subject, Issuer, Validity, SHA-256 Fingerprint), **Available Site Cookies** (with Copy & Clear controls), and **Site Configuration** (showing all enabled capabilities & interactive toggles).
   - **Bottom Toolbar**: Dedicated 7-button bar with **Back (Left Arrow)**, **Home**, **Inspect (DevTools)**, **Video Detector**, **Refresh/Stop**, **Tab View**, and **3-Dot Menu**.
2. **True 1280px Desktop Site Mode**:
   - Full desktop User-Agent (`Windows NT 10.0; Win64; x64`), `navigator.platform` spoofing, and `width=1280` viewport emulation.
3. **Ad-Block Shield & Detailed Blocked Ads Log**:
   - Real-time ad/tracker blocking with an interactive Blocked Ads Inspector sheet displaying blocked URLs, domains, matched rules, and timestamps.
4. **Built-In Desktop DevTools (`F12` Inspector)**:
   - Live `Elements` DOM inspector, `Sources` viewer, interactive JavaScript `Console`, and `Network` monitor.
5. **Smart Long-Press MP4 Video Downloader**:
   - Automatic `<video>` stream detection with direct handoff to the Android System `DownloadManager`.
