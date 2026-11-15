# Netra v6.9.4 — Tab Grouping (Long-Press Drag & Drop), Tap-to-Rename Group Popup, Reversible Ungrouping, "All Tabs" Icon Header & 70% Sheet Height (`versionCode = 19`)

## What's New in v6.9.4
- **Tab Grouping via Long-Press Drag & Drop (Both Default & Private Modes):**
  - Long-press any tab in the Tab Switcher and drag/drop it onto another tab (or existing group) to create or expand a grouped tab card.
  - Dropping a tab onto another tab immediately creates a group (named **`Group 1`** by default) and opens the Group Popup displaying all grouped tabs with live webpage previews.
  - Works seamlessly in both **Default Tabs** and **Private Tabs (Incognito)** modes.
- **Tap-to-Rename Group Name (`Group 1` Default):**
  - Inside the Group Popup, tap the **`Group 1`** title (`group_name_title`) at any time to rename the group (`group_name_input` + `save_group_name_button`).
- **Reversible Ungrouping Process:**
  - Long-press any tab inside the Group Popup and swipe/drop it onto the ungroup drop zone (`ungroup_drop_zone`) or outside the popup grid (or tap the ungroup button on the card) to remove it from the group and place it back into the normal tab list.
  - When a 2-tab group has a tab removed, both tabs automatically return to the normal tab list and the group dissolves cleanly.
- **"All Tabs" Header with Non-Truncating Default & Private Icon Counts:**
  - Renamed **`Open Tabs`** to **`All Tabs`** (`सभी टैब` in Hindi).
  - Replaced the text subtitle (`4 ACTIVE • 1 PRIVATE`) with a compact, non-truncating icon-and-count row displaying the Default Tabs count with the world icon (`Public`), a separator dot (`•`), and the Private Tabs count with the private icon (`Incognito`).
- **75% Screen Height Tabs Page, 50% Group Popup, Blurred Tab Screen & Matching Incognito Top Border:**
  - Increased the Tabs Switcher sheet height to **75%** of the screen (`Modifier.fillMaxHeight(0.75f)`), with a crisp `1.5.dp` accent border (`palette.activePillBorder`) along the top and sides above `"All Tabs"` in both Default and Incognito modes (matching the Group Popup border).
  - Sized the Group Popup (`tab_group_popup`) to **50%** of the screen height (`0.67f` of the 75% sheet), centered vertically with clear space above and below it.
  - Applied a `10.dp` blur (`Modifier.blur(10.dp)`) and translucent frosted tint (`palette.sheetBg.copy(alpha = 0.42f)`) to the 75% Tab Page when the Group Popup opens so the entire 75% Tab Page boundary and content remain clearly visible behind the popup.

## Signed Release APK Verification (`v6.9.4`)
- **File:** `releases/v6.9.4/Netra-v6.9.4-release.apk`
- **Package Name:** `com.suryatech.browser.netra`
- **Version:** `6.9.4` (`versionCode = 19`)
- **APK Size:** `1,019,343 bytes` (**995.5 KB** compressed; **1.93 MB** installed uncompressed payload)
- **SHA-256 Checksum:** `8a2ebafdea027243ac59de79a6e967e88c22282b643d28c1cb6b759e3a25d6a4`
- **Signing Certificate (V1/V2/V3):** `CN=Nova Browser, OU=Mobile, O=Nova, L=SF, ST=CA, C=US` (`SHA-256: c585099e00ff5e42e727ae4f0cd2921799950805233b2914f0c63e8bb9362203`)
