# Nova Browser v6.7.0 — 3-Section Downloads Manager, Multi-Language Support & Signed Release APK

![Version](https://img.shields.io/badge/Version-v6.7.0%20(versionCode%2013)-FF6D00?style=flat-square&logo=android&logoColor=white)
![APK Size](https://img.shields.io/badge/Size-2.41%20MB%20(2%2C524%2C632%20bytes)-00B0FF?style=flat-square)
![Downloads](https://img.shields.io/badge/Downloads-Ongoing%20%7C%20Success%20%7C%20Failure-00897B?style=flat-square)
![Languages](https://img.shields.io/badge/Languages-System%20%7C%20English%20%7C%20Hindi-8E24AA?style=flat-square)
![Ad-Blocker](https://img.shields.io/badge/Ad--Shield-Minimal%20(58)%20%7C%20Pro%20(189)%20%7C%20Max%20(220)-00C853?style=flat-square)
![License](https://img.shields.io/badge/License-GNU%20GPL%20v3.0-D50000?style=flat-square)

---

## What's New in v6.7.0

### 1. 3-Section Downloads Manager (`Ongoing` • `Success` • `Failure`)
- **Ongoing**: All newly started, active (`RUNNING` / `PENDING`), and `PAUSED` downloads enter **Ongoing** immediately with live progress and pause/resume controls.
- **Success**: Completed downloads (`COMPLETED`) automatically transition into **Success** with instant 1-tap file opening.
- **Failure**: Interrupted or failed downloads (`FAILED`) automatically transition into **Failure** with error diagnostics and 1-tap **Retry** (which moves the download right back into **Ongoing**).
- **Auto-Routing Tabs**: Live section badges show file counts for each category and automatically focus active transitions.

### 2. Android Inbuilt System Language + Instant Language Switcher
- **System Language (Default)**: Uses Android's inbuilt device configuration locale (`Resources.getSystem().configuration` + `locales_config.xml`) so Nova Browser automatically matches the phone's system language on launch (`English` or `हिन्दी`).
- **3-Dot Menu Language Selector**: Added an **App Language** section inside the browser's 3-dot (`⋮`) menu with 3 selectable options:
  - **System** (tracks Android device language)
  - **English**
  - **हिन्दी (Hindi)**
- **Zero-Reload Instant Switching**: Changing language updates the Compose UI (`NovaStrings` + localized `Context` + `values-hi/strings.xml`) immediately without reloading open `WebView` tabs.

### 3. Cumulative 3-Tier Ad-Blocker Shield (`Minimal` • `Pro` • `Max`)
- Retains the full **58 / 189 / 220** cumulative filter architecture (`Minimal` default ON, `Pro` expanded trackers/patterns/DOM selectors, `Max` YouTube ad & tracking shield + auto-skipper).

---

## Signed Release APK Metadata

| Property | Value |
| :--- | :--- |
| **File** | `releases/v6.7.0/NovaBrowser-v6.7.0-release.apk` |
| **Application ID** | `com.suryatech.browser.nova` |
| **Version** | `6.7.0` (`versionCode = 13`) |
| **Size** | `2,524,632 bytes` (~2.41 MB) |
| **SHA-256** | `04af675fa55587b5bcc9139f863e190f3917da1f9d0893cb509ca276d5312f12` |
| **Signing** | V1 + V2 + V3 Signed (`CN=SuryaTech, OU=Mobile, O=SuryaTech, L=Bengaluru, ST=KA, C=IN`) |
