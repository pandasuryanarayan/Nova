# Netra v6.9.6 — Streamlined Settings Menu (No Redundant Bookmark / Inspect / Videos Row), Animated Closed-Eye to Globe Splash & Signed Release APK

**Release Date:** November 15, 2026  
**Application ID:** `com.suryatech.browser.netra`  
**Version:** `6.9.6` (`versionCode = 21`)  
**Signed Release APK:** [`releases/v6.9.6/Netra-v6.9.6-release.apk`](Netra-v6.9.6-release.apk)  
**SHA-256 Checksum:** `f98c5026edcbe90942502ef9e7d71e7e001b422e66ad2a856a54e7e13c374777`  
**APK Size:** `1,027,671 bytes` (`1,003.6 KB` / `~0.98 MiB` R8-minified & resource-shrunk)

---

## ✨ What's New in v6.9.6

### 1. 🧹 Streamlined Settings Page When a Website Is Open (`BrowserMenuSheet`)
- Removed the redundant **Bookmark**, **Inspect**, and **Videos** button row from the Settings (`BrowserMenuSheet`) sheet when a website is opened, since **Bookmark** is already directly accessible in the top website header bar and **Inspect** + **Get Video** are already directly accessible in the bottom browser bar.
- Also passes `isDarkMode` directly into `rememberSheetPalette` inside `BrowserMenuSheet` so Dark Mode colors stay completely uniform across the Settings sheet.

### 2. 👁️🌐 Animated Closed-Eye Opening to Internet Globe Splash Screen (`avd_netra_splash.xml`)
- Clicking the Netra app icon plays a single icon-only `AnimatedVectorDrawable` (`@drawable/avd_netra_splash`) where the closed eye (`eye_upper_lid` and `eye_lower_lid`) opens outward while the browser/internet globe (`internet_globe_group`) scales up, rotates into place, and animates its equator, latitude, and meridian lines in both Light (`#FFF8F4`) and Dark (`#09090B`) modes.
