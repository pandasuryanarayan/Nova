# Netra v6.9.5 — Animated Closed-Eye to Internet Globe Splash Screen (Light & Dark Mode), Individual Vector Icon Paths & Signed Release APK

**Release Date:** November 15, 2026  
**Application ID:** `com.suryatech.browser.netra`  
**Version:** `6.9.5` (`versionCode = 20`)  
**Signed Release APK:** [`releases/v6.9.5/Netra-v6.9.5-release.apk`](Netra-v6.9.5-release.apk)  
**SHA-256 Checksum:** `75f4c253cbad4a4342a4f85181b3c346dfceaf89c1ff110bf10961bb18c1508d`  
**APK Size:** `1,028,519 bytes` (`1,004.4 KB` / `~0.98 MiB` R8-minified & resource-shrunk)

---

## ✨ What's New in v6.9.5

### 1. 👁️🌐 Animated Closed-Eye Opening to Internet Globe Splash Screen (`avd_netra_splash.xml`)
- **Closed Eye to Open Eye + Browser Globe Animation:** Clicking the Netra app icon now plays a smooth `AnimatedVectorDrawable` (`@drawable/avd_netra_splash`) where the closed eye (`eye_upper_lid` and `eye_lower_lid`) opens outward with an overshoot curve while the browser/internet globe (`internet_globe_group`) scales up, rotates into place, and draws its latitude and meridian lines (`trimPathEnd`).
- **Single Icon-Only Splash Screen (Zero Duplicate Screen & Zero Mid-Black Flash):** Removed the secondary text splash screen so only **1 icon-only animated splash screen** is shown, transitioning directly into the Home Screen with a smooth 180ms alpha fade and no intermediate black screen.

### 2. 🎨 Individual Named Vector Groups & Paths (Chrome-Style Vector Rigging)
- Refactored `ic_netra_logo.xml`, `ic_launcher_foreground.xml`, and `avd_netra_splash.xml` from a single combined path into modular, individually addressable `<group>` and `<path>` elements:
  - `eye_outer_group`: `eye_upper_lid`, `eye_lower_lid`
  - `internet_globe_group`: `globe_outer_ring`, `globe_equator`, `globe_lat_north`, `globe_lat_south`, `globe_meridian_left`, `globe_meridian_right`

### 3. ☀️🌙 Mobile White (Light) & Dark Mode Splash Support
- **Light Mode (`values/colors.xml`):** Warm white/alabaster splash canvas (`#FFF8F4`) with deep charcoal eyelids (`#1A1410`) and vibrant saffron-orange internet globe (`#FF6D00`).
- **Dark Mode (`values-night/colors.xml`):** Deep obsidian splash canvas (`#09090B`) with crisp white eyelids (`#FFFFFF`) and luminous orange internet globe (`#FF8A3D`).
- **Automatic Theme Synchronization:** Syncs `UiModeManager.setApplicationNightMode` with the in-app Dark Mode preference so cold launches always match the user's selected Light or Dark theme.
