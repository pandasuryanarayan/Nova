# Nova Browser v6.8.0 — Ultra-Light (<1.5 MB) Signed Release APK, 3-Section Downloads & Multi-Language Support

## Release Summary (`v6.8.0` • `versionCode = 14`)

- **APK File:** `releases/v6.8.0/NovaBrowser-v6.8.0-release.apk`
- **Package ID:** `com.suryatech.browser.nova`
- **APK Download Size:** `1,466,192 bytes` (**1.40 MB** — strictly below `1.5 MB`)
- **Installed Uncompressed Size:** `2,758,486 bytes` (**2.63 MB** — strictly below `5 MB`)
- **SHA-256 Checksum:** `7f9764564422d47285747d95ab213dba5539e6112dad35e60930343c4e756f38`

## Key Optimizations & Features in v6.8.0

1. **Ultra-Lightweight APK (< 1.5 MB) & Installed Footprint (< 5 MB) with Original Nova Icon**:
   - Restored the original `v6.7.0` Nova Browser logo across the adaptive launcher (`ic_launcher_foreground.xml` + `#091025` background), all 5 legacy mipmap densities (`mdpi`–`xxxhdpi`, square & round), and the Nova Home badge (`NovaHomeContent.kt`).
   - Replaced bundled `.ttf` font binaries with native Android system `SansSerif` and `Monospace` typography.
   - Replaced `OkHttp`, `Coil`, `DataStore Preferences`, and `material-icons-extended` with native Android `HttpURLConnection` / `HttpsURLConnection`, `BitmapFactory`, `SharedPreferences` + `StateFlow`, and compact SVG path vector icons.
   - Configured aggressive R8 full-mode shrinking, repackaging, and resource/metadata exclusions with zero feature loss.
2. **3-Section Downloads Manager (`Ongoing` • `Success` • `Failure`)**:
   - Real-time segmented filter tabs in the Downloads sheet automatically categorize active downloads under **Ongoing**, completed downloads under **Success**, and failed transfers under **Failure** with one-tap retry.
3. **Android Inbuilt Language & 3-Dot Language Switcher (`System` • `English` • `हिन्दी`)**:
   - Automatically follows device locale by default (`System Language`) and includes an instant Language switcher (`System`, `English`, `Hindi`) inside the 3-dot Control Center menu.
4. **3-Tier Cumulative Ad-Blocker (`Minimal` 58 • `Pro` 189 • `Max` 220 Rules)**:
   - Preserves all 220 network, path, cosmetic DOM, and YouTube ad-skipping rules, plus full DevTools (`Elements`, `Sources`, `Console`, `Network`), Video Sniffer, Background Audio Player, SSL/Cookie Inspector, and Incognito mode.
