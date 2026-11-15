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
- **70% Screen Height Tabs Page & Centered 45–50% Group Popup:**
  - Increased the Tabs Switcher sheet height to **70%** of the screen (`Modifier.fillMaxHeight(0.70f)`).
  - Sized the Group Popup to **~48%** of the screen height (`45–50%`) centered vertically so there is clear space above and below the popup.

## Signed Release APK Verification (`v6.9.4`)
- **File:** `releases/v6.9.4/Netra-v6.9.4-release.apk`
- **Package Name:** `com.suryatech.browser.netra`
- **Version:** `6.9.4` (`versionCode = 19`)
- **APK Size:** `1,017,155 bytes` (**993.3 KB** compressed; **1.93 MB** installed uncompressed payload)
- **SHA-256 Checksum:** `707caf9f2fb9d33137659e43646ff34d2f4ea6c2a6f72162fc1ca7b08ef9e4d2`
- **Signing Certificate (V1/V2/V3):** `CN=Nova Browser, OU=Mobile, O=Nova, L=SF, ST=CA, C=US` (`SHA-256: c585099e00ff5e42e727ae4f0cd2921799950805233b2914f0c63e8bb9362203`)
