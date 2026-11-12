# Nova Browser v6.4.0 — Official Signed Release

- **Package ID (`applicationId`)**: `com.suryatech.browser.nova`
- **Version**: `v6.4.0` (`versionCode = 10`, `versionName = "6.4.0"`)
- **License**: GNU General Public License v3.0 (`GPL-3.0`)
- **Build Type**: Signed Release APK (`R8` Code Minification + Resource Shrinking + PNG Crunching Enabled)
- **File Size**: `2.38 MB` (`2,491,468 bytes`)
- **SHA-256**: `ed2276f3608564c8fcfe5a45615c615f72958477094d6c6be1cc6146d5e1dc0a`

## What's New in v6.4.0
1. **Standard End-of-URL Cursor Placement on Edit**:
   - Clicking the **Edit** icon inside the Top Bar Fall-Down Website Link panel now automatically focuses the text input and positions the blinking cursor at the **very end of the URL** (`TextRange(url.length)`), allowing immediate editing or backspacing from the end of the address.
2. **Warm Light Orange Default Theme & Cosmic Dark Stealth Incognito Theme**:
   - **Default Mode (`Incognito: OFF`)**: Now uses the **Warm Light Orange Theme** (`#FFF8F3` warm cream background, `#E65100` vibrant burnt orange primary, `#FFF0E5` surface containers) by default across the entire browser.
   - **Incognito Mode (`Incognito: ON`)**: Toggling Incognito sweeps a smooth full-screen radial wave transition into the **Cosmic Dark Obsidian + Electric Cyan/Violet Theme** (`#060A12` deep obsidian background, `#00E5FF` electric cyan accents, `#8C62FF` violet secondary) with matching text and badge mappings.
3. **Centered Home Page Hub Action Cards (`Downloads`, `Bookmarks`, `History`)**:
   - The **Downloads**, **Bookmarks**, and **History** cards on the Home screen now center their circular icon badge, title, and item count horizontally and vertically within each card's own space (`Alignment.CenterHorizontally` + `TextAlign.Center`).
4. **Compact Icon-Only Top Link Bar Actions & Disk-Cached Website Icons**:
   - Retains the aligned `"Website Link"` fall-down panel with compact **Copy**, **Edit**, and **Share** icon buttons on the right, on-demand disk-cached real website favicons (`cacheDir/shortcut_icons`), and 3-dot **Edit** / **Delete** options on every Quick Shortcut.
