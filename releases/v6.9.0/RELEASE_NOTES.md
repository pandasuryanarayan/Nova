# Netra v6.9.0 — Signed Release APK (`< 1 MB`)

- **Package ID:** `com.suryatech.browser.netra`
- **App Name:** `Netra` (English) / `नेत्र` (Hindi)
- **Version:** `6.9.0` (`versionCode = 15`)
- **Signed Release APK:** `releases/v6.9.0/Netra-v6.9.0-release.apk`
- **APK Size:** `990,211` bytes (`967.00 KB` / `0.944 MiB`)
- **Installed Payload Size (Uncompressed):** `1,972,947` bytes (`1.882 MiB`)
- **SHA-256:** `357c1385564383123da1882e3ad25e4508b953741c62f2e5d7eaa1f245622e3b`

## Highlights
1. **Complete Netra / नेत्र Rebrand & Single-Line Hindi Title Fix:**
   - Updated package ID to `com.suryatech.browser.netra`.
   - Renamed app title to **Netra** (English) and **नेत्र** (Hindi), removed "Browser" / "ब्राउज़र" labels across the app, and updated internal pages (`netra://home`, `netra://start`) and download folder (`Download/Netra/Videos/`).
   - Fixed Devanagari glyph shaping & single-line layout (`letterSpacing = 0.sp` in Hindi, generous `lineHeight`, `maxLines = 1`, `softWrap = false`) so **नेत्र** renders intact on one line without splitting into **ने** and **त्र** on Home or Splash screens.
2. **Self-Contained Eye+Globe Vector Branding & Splash Screen:**
   - Converted the custom black-and-white Netra eye+globe emblem into crisp self-contained XML vectors (`ic_netra_logo.xml`, `ic_launcher_foreground.xml`, `ic_launcher_background.xml`) plus optimized legacy mipmap PNGs across all 5 densities (`mdpi`–`xxxhdpi`).
   - Added native cold-start and Compose splash screen featuring the centered Netra vector emblem on pure `#000000` black.
3. **Search Bar, Bookmark Manager Icons, Menu & Desktop Site Settings Alignment:**
   - Enlarged search and address bars (`58.dp` home search bar, `54.dp` URL editor, `58.dp` top bar with `44.dp` address pill) for effortless tapping and clear visibility.
   - Separated bookmark icons by intent: stacked Bookmarks manager icon (`Bookmarks`) on the Home bottom bar, Control Center tile, and Bookmarks sheet header; single bookmark icon (`Bookmark` / `BookmarkBorder`) for saving individual websites.
   - Removed the menu sheet's header row ("Control center", shield blocked status line, and close `×` button) and added smooth swipe-down-to-dismiss support across bottom sheets.
   - Refined **Desktop site** and below contents (`Desktop site`, `Dark theme`, `Incognito`, `Text zoom`, `App Language`, `Bookmark`/`Inspect`/`Videos`, `Find in page`/`Clear data`) into card-grouped rows with uniform `36.dp` icon badges, consistent padding, `15.sp` row headings, centered `13.sp` language/tool tabs, and `48.dp` bottom action buttons.
4. **Full SSL/TLS Certificate Details, Complete Cookies Inspector & Chrome-Style Site Config:**
   - **Certificate (`Certificate` tab):** Displays protocol, validity status with exact days remaining, X.509 version, key usage & extended key usage, full Subject & Issuer DNs + Country, Subject Alternative Names, public key algorithm & bit size, signature algorithm, colon-formatted serial hex, SHA-256 & SHA-1 fingerprints (with API < 29 `SslCertificate.saveState` X.509 extraction fallback), all tap-to-copy.
   - **Cookies & Storage (`Cookies` tab):** Lists **all** domain cookies with host scope, byte sizes, active cookie policy indicator, `"Copy All"`, `"Clear All"`, individual copy, and individual delete (`clearSingleSiteCookie`), plus multi-domain cookie expiration, `WebStorage.deleteOrigin`, and `localStorage`/`sessionStorage` wipe.
   - **Chrome-Style Site Config (`Site Config` tab):** Per-domain persisted settings grouped cleanly (`Content & Controls`, `Permissions`, `Security & Storage`): Ad-Block Shield, Desktop Site, Dark Theme, JavaScript, 3-way Cookies (`Allow all` / `No 3rd-party` / `Block all`), Pop-ups & Redirects (`onCreateWindow` blocking for unrequested pop-ups), Images, Live Sound mute/unmute, Autoplay, 3-way `Ask`/`Allow`/`Block` permissions for Location, Camera, and Microphone (wired to in-app prompts + runtime Android permission launchers), Protected Content (DRM), DOM/Local Storage & Database, Insecure/Mixed Content, Safe Browsing, and `"Clear cookies & reset site config"`.
5. **Verified Cumulative Ad-Block Filter Parity (`Minimal`, `Pro`, `Max`):**
   - Verified 100% rule parity against `v6.8.0` / `v6.7.0` with **zero removed rules**.
   - **Minimal:** `58` distinct rules (`40` hosts + `8` paths + `10` DOM selectors).
   - **Pro (Minimal + Pro):** `235` distinct cumulative rules (`141` hosts + `45` path/regex patterns + `49` DOM selectors).
   - **Max (Minimal + Pro + Max):** `267` distinct cumulative rules (`147` hosts + `55` path/regex patterns + `65` DOM selectors, including YouTube ad blocking & auto-skip).
6. **Multi-Player Window / Access Denied & Multi-`<video>` Ad-Aware Long-Press Video Detection Fixes:**
   - Aligned `setSupportMultipleWindows` with Site Config (`popupsAllowed` default `false`, matching `v6.6.0`/`v6.8.0`) and hardened `onCreateWindow` / `openNewTab` / `shouldOverrideUrlLoading` with `Referer` header preservation, original request headers, and `AdBlocker` filtering to prevent "Access Denied" errors on multi-player streaming pages.
   - Fixed multi-`<video>` long-press detection when pages embed ad `<video>` tags alongside the show `<video>`: fixed early `MutationObserver` registration when `document.documentElement` is initially null, added Shadow DOM and same-origin `iframe` traversal (`querySelectorAll('video')` instead of only the first `<video>`), stopped empty/ad-blocked `<video>` placeholders from shadowing active streams, filtered nearby `<a href>` links against ad URL patterns while prioritizing direct `<video>`/`<source>` URLs, and passed native CSS-pixel touch coordinates from `WebView` to `__novaInspectVideoAtLastTouch`.
7. **Proven Video Header Probing, Accurate MB Size Calculation & Robust Direct Video Downloader:**
   - Restored the exact, reliable header probing architecture from `v6.7.0`/`v6.8.0`:
     - Probing exclusively uses standard `GET` requests with `Range: bytes=0-1` instead of `HEAD` requests (which were rejected with `405 Method Not Allowed` or `403 Forbidden` on video CDNs and proxies).
     - Probes transfer only 2 bytes and read the exact total file size from `Content-Range: bytes 0-1/TOTAL_BYTES` (falling back to `Content-Length`).
     - Removed breaking `Accept-Encoding: identity` headers and enabled native `instanceFollowRedirects = true` on `HttpURLConnection`.
     - In JavaScript, `lookupPerformanceResourceSize` immediately extracts buffered video transfer sizes from `window.performance.getEntriesByType('resource')` upon long press and range fetch.
     - Flexible URL normalization matches probed sizes across encoding variants and trailing fragments, instantly updating the long-press sheet with `Estimated Size: X.X MB`.
   - Restored the clean 3-stage download pipeline without URL poisoning or combinatorial retry delays:
     - Stage 1: Android System `DownloadManager` attempt with winning profile referer (and single retry with `null` referer).
     - Stage 2: Direct HTTP streaming attempt writing chunks directly via `RandomAccessFile` with real-time UI & notification progress every 240ms, exporting to `Download/Netra/Videos` via MediaStore.
     - Stage 3: In-WebView authenticated streaming bridge fallback (`__novaDownloadVideoViaWebView`).
8. **Keyboard Auto-Dismissal Fixes:**
   - Updated `NovaEditText`, Home search bar, Top URL edit bar, and Find-in-Page bar so pressing Go/Search/Done/Next/Enter or tapping the URL bar arrow immediately clears focus and dismisses the soft keyboard (`InputMethodManager.hideSoftInputFromWindow`, `LocalSoftwareKeyboardController`, and `onDispose` cleanup).
9. **Full Feature Suite Preserved (`< 1 MB` APK, `< 5 MB` Installed):**
   - Ongoing / Success / Failure Downloads manager, System / English / Hindi language selector, 3-tier Ad-Block Shield (`Minimal`/`न्यूनतम`, `Pro`/`अधिक`, `Max`/`अधिकतम`), AMOLED black dark theme, Incognito mode, DevTools Inspector, and long-press MP4 Video Downloader.
