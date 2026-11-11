# Nova Browser v3.0 — Release Notes

- **Version Name**: `3.0` (`v3.0`)
- **Version Code**: `3`
- **Package ID**: `com.aistudio.novabrowser.xkvpzr`
- **Min / Target SDK**: API 24 (Android 7.0) – API 36 (Android 16)
- **Signed Release APK**: `NovaBrowser-v3.0-release.apk`
- **Compressed Size**: `3.02 MB` (`3,165,740 bytes`)
- **SHA-256 Checksum**: `ad26508a82480ad6bacc39f7de70aeb5d5d79f7ddd8917c659b58515179acada`

## What's New in v3.0
1. **Clean, Streamlined Architecture (VPN-Free Build)**:
   - Removed all VPN services, background tunnels, and permissions for a faster, lighter ~3.0 MB R8-minified binary.
2. **Expanded Wide Bottom Search & Address Bar**:
   - Reclaimed horizontal space in the bottom navigation bar to provide a wider, more comfortable search/URL pill (`46dp` height) with `40dp` navigation buttons.
3. **True 1280px Desktop Site Mode**:
   - Full desktop User-Agent (`X11; Linux x86_64`), `navigator.platform` spoofing, and `width=1280` viewport emulation.
4. **Ad-Block Shield & Detailed Blocked Ads Log**:
   - Real-time ad/tracker blocking with an interactive Blocked Ads Inspector sheet displaying blocked URLs, domains, matched rules, and timestamps.
5. **Built-In Desktop DevTools (`F12` Inspector)**:
   - Live `Elements` DOM inspector, `Sources` viewer, interactive JavaScript `Console`, `Network` monitor, and `Storage` manager.
6. **Smart Long-Press MP4 Video Downloader**:
   - Automatic `<video>` stream detection with direct handoff to the Android System `DownloadManager`.
