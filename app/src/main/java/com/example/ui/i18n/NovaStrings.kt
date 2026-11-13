package com.example.ui.i18n

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.data.AppLanguage
import java.util.Locale

data class NovaStrings(
    val languageCode: String,
    val isHindi: Boolean,
    // Home Screen
    val appTitleStandard: String,
    val appTitlePrivate: String,
    val appSubtitleStandard: String,
    val appSubtitlePrivate: String,
    val homeSubtitleStandard: String,
    val homeSubtitleIncognito: String,
    val headerBadgeStandard: String,
    val headerBadgePrivate: String,
    val searchPlaceholder: String,
    val shieldActiveStatus: String,
    val shieldActivePrivateStatus: String,
    val shieldActiveNoTrackers: String,
    val adShieldOff: String,
    val blockedTodayLabel: String,
    val adsLabel: String,
    val incognitoOn: String,
    val incognitoOff: String,
    val incognitoOffBadge: String,
    val incognitoOnBadge: String,
    val incognitoOnPrivate: String,
    val incognitoLabel: String,
    val downloadsTitle: String,
    val filesCountSuffix: String,
    val bookmarksTitle: String,
    val savedCountSuffix: String,
    val historyTitle: String,
    val visitsCountSuffix: String,
    val quickShortcutsTitle: String,
    val addSiteButton: String,
    val shortcutLabel: String,
    val privateTabNotice: String,
    val trackerSavedFooterPrefix: String,
    val savedBandwidthSuffix: String,
    val editShortcutDialogTitle: String,
    val addShortcutDialogTitle: String,
    val siteTitleLabel: String,
    val websiteUrlLabel: String,
    val saveButton: String,
    val addButton: String,
    val cancelButton: String,
    val editAction: String,
    val doneAction: String,
    val deleteAction: String,
    // Bottom & Top Bar
    val navHome: String,
    val navSaved: String,
    val navTabs: String,
    val navSettings: String,
    val websiteLinkTitle: String,
    val searchOrTypeUrlPlaceholder: String,
    val copyAction: String,
    val shareAction: String,
    val verifiedSecureSsl: String,
    val unverifiedHttp: String,
    // Control Center / 3-Dot Menu
    val controlCenterTitle: String,
    val shieldPausedLabel: String,
    val copyLinkLabel: String,
    val quickActionBookmarks: String,
    val quickActionDownloads: String,
    val quickActionHistory: String,
    val quickActionShare: String,
    val quickActionCopyLink: String,
    val adBlockShieldMenuTitle: String,
    val modeMinimalTitle: String,
    val modeProTitle: String,
    val modeMaxTitle: String,
    val blockedCountSuffix: String,
    val rulesCountWord: String,
    val filtersCountSuffix: String,
    val fastSuffix: String,
    val blockedAdsFilterModesTitle: String,
    val filterModesTitle: String,
    val inSessionSuffix: String,
    val blockedAdsFilterModesSubtitle: String,
    val filterModesSubtitle: String,
    val configureButton: String,
    val desktopSiteModeActive: String,
    val desktopSiteMode: String,
    val darkModeTheme: String,
    val incognitoPrivateMode: String,
    val nothingIsSavedSubtitle: String,
    val languageSectionTitle: String,
    val languageSystemOption: String,
    val languageEnglishOption: String,
    val languageHindiOption: String,
    val textZoomLabel: String,
    val resetButton: String,
    val resetZoomButton: String,
    val findInPageButton: String,
    val clearDataButton: String,
    val bookmarkPageButton: String,
    val bookmarkButton: String,
    val savedPageButton: String,
    val savedButton: String,
    val inspectPageButton: String,
    val inspectButton: String,
    val videosPageButton: String,
    val videosButton: String,
    // Ad-Block Shield Sheet
    val activeRulesSuffix: String,
    val shieldDisabledSubtitle: String,
    val adShieldHeaderDisabled: String,
    val hostFiltersLabel: String,
    val pathFiltersLabel: String,
    val cosmeticFiltersLabel: String,
    val filterAll: String,
    val filterTab: String,
    val filterAds: String,
    val filterTrackers: String,
    val filterRules: String,
    val filterChipAll: String,
    val filterChipTab: String,
    val filterChipAds: String,
    val filterChipTrackers: String,
    val filterChipRules: String,
    val thisPageLabel: String,
    val cleanBadge: String,
    val protectedBadge: String,
    val siteCleanBadge: String,
    val siteProtectedBadge: String,
    val isActiveLabel: String,
    val adBlockerOffTitle: String,
    val shieldDisabledEmptyTitle: String,
    val minimalSummaryDesc: String,
    val proSummaryDesc: String,
    val maxSummaryDesc: String,
    val pauseOnSiteButton: String,
    val resumeOnSiteButton: String,
    val customRulesButton: String,
    val addCustomRuleButton: String,
    // Downloads Manager (3 Sections: Ongoing, Success, Failure)
    val openFolderButton: String,
    val downloadsFolderSubtitle: String,
    val downloadsFolderLabel: String,
    val downloadTabOngoing: String,
    val downloadTabSuccess: String,
    val downloadTabFailure: String,
    val sortNewest: String,
    val sortOldest: String,
    val noOngoingDownloadsTitle: String,
    val noOngoingDownloadsSubtitle: String,
    val noOngoingDownloadsSub: String,
    val noSuccessDownloadsTitle: String,
    val noSuccessDownloadsSubtitle: String,
    val noSuccessDownloadsSub: String,
    val noFailureDownloadsTitle: String,
    val noFailureDownloadsSubtitle: String,
    val noFailureDownloadsSub: String,
    val downloadStatusCompleted: String,
    val downloadStatusPaused: String,
    val downloadStatusFailed: String,
    val downloadStatusStarting: String,
    val unknownSize: String,
    // Bookmarks, History, Tabs, Clear Data & QR Scanner
    val syncedSuffix: String,
    val bookmarksHintPrefix: String,
    val bookmarksHintSuffix: String,
    val addBookmarkButton: String,
    val openAllButton: String,
    val openAllBookmarksButton: String,
    val noBookmarksYet: String,
    val clearAllButton: String,
    val browsingHistoryTitle: String,
    val todaySuffix: String,
    val yesterdayLabel: String,
    val historyTodayPrefix: String,
    val historyYesterdayPrefix: String,
    val searchHistoryPlaceholder: String,
    val noHistoryYet: String,
    val noHistoryTitle: String,
    val noHistorySubtitle: String,
    val noMatchingVisits: String,
    val noMatchingVisitsTitle: String,
    val openTabsTitle: String,
    val novaHomeTabTitle: String,
    val activeSuffix: String,
    val newTabButton: String,
    val privateTabButton: String,
    val stealthTabBadge: String,
    val standardTabBadge: String,
    val clearBrowsingDataTitle: String,
    val clearDataDialogTitle: String,
    val clearBrowsingDataPrompt: String,
    val clearDataDialogSubtitle: String,
    val browsingHistoryOption: String,
    val clearHistoryCheckbox: String,
    val cachedImagesFilesOption: String,
    val clearCacheCheckbox: String,
    val cookiesSiteDataOption: String,
    val clearCookiesCheckbox: String,
    val clearNowButton: String,
    val qrScannerTitle: String,
    val qrScannerSubtitle: String,
    val qrScanAgainButton: String,
    val qrOpenWebsiteButton: String,
    val qrSearchCodeButton: String,
    val qrCopyButton: String,
    val qrScanFromGallery: String,
    val qrManualInputHint: String
) {
    fun shieldActivePrivateTab(modeLabel: String): String =
        if (isHindi) "शील्ड सक्रिय ($modeLabel) • प्राइवेट टैब" else "SHIELD ACTIVE ($modeLabel) • PRIVATE TAB"

    fun shieldFooterSummary(totalAdsBlocked: Number, savedLabel: String): String =
        if (isHindi) "$totalAdsBlocked ट्रैकर्स रोके गए • ~$savedLabel डेटा बचत" else "$totalAdsBlocked TRACKERS BLOCKED • ~$savedLabel SAVED"

    fun tabsHeaderStats(totalTabs: Int, incognitoCount: Int): String =
        if (isHindi) {
            if (incognitoCount > 0) "$totalTabs सक्रिय • $incognitoCount गुप्त" else "$totalTabs सक्रिय"
        } else {
            if (incognitoCount > 0) "$totalTabs ACTIVE • $incognitoCount PRIVATE" else "$totalTabs ACTIVE"
        }

    fun downloadsHeaderStats(totalFiles: Int, totalMbFormatted: String): String =
        if (isHindi) "$totalFiles फ़ाइलें • $totalMbFormatted" else "$totalFiles FILES • $totalMbFormatted"

    fun bookmarksHeaderStats(count: Int): String =
        if (isHindi) "$count सहेजे गए • सिंक किया गया" else "$count SAVED • SYNCED"

    fun historyVisitsStats(totalVisits: Int, todayCount: Int): String =
        if (isHindi) "$totalVisits विज़िट • $todayCount आज" else "$totalVisits VISITS • $todayCount TODAY"

    fun historyVisitsLabel(count: Int): String =
        if (isHindi) "$count विज़िट" else "$count visits"

    fun shieldHeaderActive(modeLabel: String, blockedCount: Number): String =
        if (isHindi) "शील्ड $modeLabel • $blockedCount रोके गए" else "SHIELD $modeLabel • $blockedCount BLOCKED"

    fun shieldHeaderPaused(blockedCount: Number): String =
        if (isHindi) "शील्ड बंद है • $blockedCount रोके गए" else "SHIELD PAUSED • $blockedCount BLOCKED"

    fun rulesCountSuffix(count: Int): String =
        "$count $rulesCountWord"

    fun adShieldHeaderActive(totalAdsBlocked: Number, activeRulesCount: Int): String =
        if (isHindi) "$totalAdsBlocked रोके गए • $activeRulesCount सक्रिय नियम" else "$totalAdsBlocked BLOCKED • $activeRulesCount ACTIVE RULES"

    fun filtersCountFast(count: Int): String =
        if (isHindi) "$count फ़िल्टर • तेज़" else "$count FILTERS • FAST"

    fun filtersCountOnly(count: Int): String =
        if (isHindi) "$count फ़िल्टर" else "$count FILTERS"

    fun filterBreakdownLine(hostCount: Int, pathCount: Int, cosmeticCount: Int): String =
        if (isHindi) {
            "$hostCount होस्ट • $pathCount पाथ/स्क्रिप्ट • $cosmeticCount DOM फ़िल्टर"
        } else {
            "$hostCount host • $pathCount path/script • $cosmeticCount cosmetic DOM filters"
        }

    fun thisPageBlockedCount(count: Int): String =
        if (isHindi) "यह पेज ($count रोके गए)" else "This page ($count blocked)"

    fun shieldActiveEmptyTitle(modeLabel: String, activeRulesCount: Int): String =
        if (isHindi) "$modeLabel मोड सक्रिय है ($activeRulesCount नियम)" else "$modeLabel mode is active ($activeRulesCount rules)"

    fun shieldMinimalEmptyDesc(activeRulesCount: Int): String =
        if (isHindi) {
            "आवश्यक नियम ($activeRulesCount सक्रिय नियम): तेज़ और हल्का। सभी होस्ट, पाथ और DOM फ़िल्टर देखने के लिए नियम (Rules) खोलें।"
        } else {
            "Essential ruleset ($activeRulesCount active rules): fast and lightweight. Open Rules ($activeRulesCount) to inspect every host, path and cosmetic filter."
        }

    fun shieldProEmptyDesc(activeRulesCount: Int): String =
        if (isHindi) {
            "प्रो नियम ($activeRulesCount संचयी नियम): Minimal + 80+ विज्ञापन नेटवर्क और ट्रैकिंग पिक्सेल शामिल हैं।"
        } else {
            "Pro ruleset ($activeRulesCount cumulative rules): includes Minimal + 80+ ad networks, telemetry & tracking pixels. Open Rules ($activeRulesCount) to inspect."
        }

    fun shieldMaxEmptyDesc(activeRulesCount: Int): String =
        if (isHindi) {
            "मैक्स नियम ($activeRulesCount संचयी नियम): Minimal + Pro + YouTube विज्ञापन और ऑटो-स्किप फ़िल्टर शामिल हैं।"
        } else {
            "Max ruleset ($activeRulesCount cumulative rules): includes Minimal + Pro + YouTube ad hosts & cosmetic filters. Open Rules ($activeRulesCount) to inspect."
        }
}

val EnglishNovaStrings = NovaStrings(
    languageCode = "en",
    isHindi = false,
    appTitleStandard = "Nova Browser",
    appTitlePrivate = "Nova Private",
    appSubtitleStandard = "Fast, private & ad-free web browsing",
    appSubtitlePrivate = "Private Browsing • History & cookies disabled",
    homeSubtitleStandard = "PRIVATE • AD-FREE",
    homeSubtitleIncognito = "INCOGNITO • STEALTH",
    headerBadgeStandard = "PRIVATE • AD-FREE",
    headerBadgePrivate = "INCOGNITO • STEALTH",
    searchPlaceholder = "Search or type a URL",
    shieldActiveStatus = "SHIELD ACTIVE • NO TRACKERS ON THIS TAB",
    shieldActivePrivateStatus = "SHIELD ACTIVE • PRIVATE TAB",
    shieldActiveNoTrackers = "SHIELD ACTIVE • NO TRACKERS ON THIS TAB",
    adShieldOff = "Ad-Shield Off",
    blockedTodayLabel = "BLOCKED TODAY",
    adsLabel = "Ads",
    incognitoOn = "Incognito: ON",
    incognitoOff = "OFF",
    incognitoOffBadge = "OFF",
    incognitoOnBadge = "ON • PRIVATE",
    incognitoOnPrivate = "ON • PRIVATE",
    incognitoLabel = "Incognito",
    downloadsTitle = "Downloads",
    filesCountSuffix = "FILES",
    bookmarksTitle = "Bookmarks",
    savedCountSuffix = "SAVED",
    historyTitle = "History",
    visitsCountSuffix = "VISITS",
    quickShortcutsTitle = "Quick shortcuts",
    addSiteButton = "Add site",
    shortcutLabel = "Shortcut",
    privateTabNotice = "Private Tab Active: Pages you view in this tab won't stick around in your browser history, cookie store, or search history.",
    trackerSavedFooterPrefix = "TRACKERS BLOCKED",
    savedBandwidthSuffix = "SAVED",
    editShortcutDialogTitle = "Edit Quick Shortcut",
    addShortcutDialogTitle = "Add Quick Shortcut",
    siteTitleLabel = "Site Title",
    websiteUrlLabel = "Website URL",
    saveButton = "Save",
    addButton = "Add",
    cancelButton = "Cancel",
    editAction = "Edit",
    doneAction = "Done",
    deleteAction = "Delete",
    navHome = "HOME",
    navSaved = "SAVED",
    navTabs = "TABS",
    navSettings = "SETTINGS",
    websiteLinkTitle = "Website Link",
    searchOrTypeUrlPlaceholder = "Search or type website URL…",
    copyAction = "Copy",
    shareAction = "Share",
    verifiedSecureSsl = "HTTPS Secure Connection",
    unverifiedHttp = "Unencrypted HTTP Connection",
    controlCenterTitle = "Control center",
    shieldPausedLabel = "SHIELD PAUSED",
    copyLinkLabel = "COPY LINK",
    quickActionBookmarks = "BOOKMARKS",
    quickActionDownloads = "DOWNLOADS",
    quickActionHistory = "HISTORY",
    quickActionShare = "SHARE",
    quickActionCopyLink = "COPY LINK",
    adBlockShieldMenuTitle = "Ad-block shield",
    modeMinimalTitle = "Minimal",
    modeProTitle = "Pro",
    modeMaxTitle = "Max",
    blockedCountSuffix = "BLOCKED",
    rulesCountWord = "RULES",
    filtersCountSuffix = "FILTERS",
    fastSuffix = "FAST",
    blockedAdsFilterModesTitle = "Filter modes",
    filterModesTitle = "Filter modes",
    inSessionSuffix = "in session",
    blockedAdsFilterModesSubtitle = "Inspect every active rule",
    filterModesSubtitle = "Inspect every active rule",
    configureButton = "Configure",
    desktopSiteModeActive = "Desktop site (Active • 1280px)",
    desktopSiteMode = "Desktop site",
    darkModeTheme = "Dark theme",
    incognitoPrivateMode = "Incognito / Private Mode",
    nothingIsSavedSubtitle = "NOTHING IS SAVED",
    languageSectionTitle = "App Language",
    languageSystemOption = "System",
    languageEnglishOption = "English",
    languageHindiOption = "हिन्दी",
    textZoomLabel = "Text zoom",
    resetButton = "Reset",
    resetZoomButton = "Reset",
    findInPageButton = "Find in page",
    clearDataButton = "Clear data",
    bookmarkPageButton = "Bookmark",
    bookmarkButton = "Bookmark",
    savedPageButton = "Saved",
    savedButton = "Saved",
    inspectPageButton = "Inspect",
    inspectButton = "Inspect",
    videosPageButton = "Videos",
    videosButton = "Videos",
    activeRulesSuffix = "ACTIVE RULES",
    shieldDisabledSubtitle = "SHIELD DISABLED • TAP TO ENABLE",
    adShieldHeaderDisabled = "SHIELD DISABLED • TAP TO ENABLE",
    hostFiltersLabel = "host",
    pathFiltersLabel = "path/script",
    cosmeticFiltersLabel = "cosmetic DOM filters",
    filterAll = "All",
    filterTab = "Tab",
    filterAds = "Ads",
    filterTrackers = "Trackers",
    filterRules = "Rules",
    filterChipAll = "All",
    filterChipTab = "Tab",
    filterChipAds = "Ads",
    filterChipTrackers = "Trackers",
    filterChipRules = "Rules",
    thisPageLabel = "This page",
    cleanBadge = "Clean",
    protectedBadge = "Protected",
    siteCleanBadge = "Clean",
    siteProtectedBadge = "Protected",
    isActiveLabel = "is active",
    adBlockerOffTitle = "Ad-Blocker is currently turned off",
    shieldDisabledEmptyTitle = "Ad-Blocker is currently turned off",
    minimalSummaryDesc = "Essential ruleset: fast and lightweight. Open Rules to inspect every host, path and cosmetic filter.",
    proSummaryDesc = "Pro ruleset: includes Minimal + 80+ ad networks, telemetry & tracking pixels. Open Rules to inspect.",
    maxSummaryDesc = "Max ruleset: includes Minimal + Pro + YouTube ad hosts & cosmetic filters. Open Rules to inspect.",
    pauseOnSiteButton = "Pause on this site",
    resumeOnSiteButton = "Resume on this site",
    customRulesButton = "Custom rules",
    addCustomRuleButton = "+ Add custom rule",
    openFolderButton = "Open folder",
    downloadsFolderSubtitle = "FOLDER • Download/NovaBrowser/Videos/",
    downloadsFolderLabel = "FOLDER • Download/NovaBrowser/Videos/",
    downloadTabOngoing = "Ongoing",
    downloadTabSuccess = "Success",
    downloadTabFailure = "Failure",
    sortNewest = "Sort: Newest",
    sortOldest = "Sort: Oldest",
    noOngoingDownloadsTitle = "No ongoing downloads",
    noOngoingDownloadsSubtitle = "Active downloads in progress or paused will appear here in real time.",
    noOngoingDownloadsSub = "Active downloads in progress or paused will appear here in real time.",
    noSuccessDownloadsTitle = "No downloaded videos yet",
    noSuccessDownloadsSubtitle = "Long-press any HTML5 video player on a site to grab the MP4 straight into this folder.",
    noSuccessDownloadsSub = "Long-press any HTML5 video player on a site to grab the MP4 straight into this folder.",
    noFailureDownloadsTitle = "No failed downloads",
    noFailureDownloadsSubtitle = "Any interrupted or failed downloads will appear here so you can retry them.",
    noFailureDownloadsSub = "Any interrupted or failed downloads will appear here so you can retry them.",
    downloadStatusCompleted = "COMPLETED",
    downloadStatusPaused = "PAUSED",
    downloadStatusFailed = "FAILED • TAP RETRY",
    downloadStatusStarting = "QUEUED...",
    unknownSize = "Unknown size",
    syncedSuffix = "SYNCED",
    bookmarksHintPrefix = "Long-press any link, or tap ",
    bookmarksHintSuffix = " in the toolbar to save it here.",
    addBookmarkButton = "Add bookmark",
    openAllButton = "Open all",
    openAllBookmarksButton = "Open all",
    noBookmarksYet = "No bookmarks saved yet. Tap the bookmark icon in the browser bar to save your favorite websites.",
    clearAllButton = "Clear all",
    browsingHistoryTitle = "Browsing history",
    todaySuffix = "TODAY",
    yesterdayLabel = "YESTERDAY",
    historyTodayPrefix = "TODAY",
    historyYesterdayPrefix = "YESTERDAY",
    searchHistoryPlaceholder = "Search history",
    noHistoryYet = "No browsing history recorded yet.",
    noHistoryTitle = "No browsing history recorded yet",
    noHistorySubtitle = "Websites you visit in standard mode will appear here.",
    noMatchingVisits = "No matching visits",
    noMatchingVisitsTitle = "No matching visits",
    openTabsTitle = "Open Tabs",
    novaHomeTabTitle = "Nova Home",
    activeSuffix = "ACTIVE",
    newTabButton = "New Tab",
    privateTabButton = "Private",
    stealthTabBadge = "STEALTH TAB",
    standardTabBadge = "STANDARD TAB",
    clearBrowsingDataTitle = "Clear Browsing Data",
    clearDataDialogTitle = "Clear Browsing Data",
    clearBrowsingDataPrompt = "Choose which Nova Browser data to permanently remove from this device:",
    clearDataDialogSubtitle = "Choose which Nova Browser data to permanently remove from this device:",
    browsingHistoryOption = "Browsing History",
    clearHistoryCheckbox = "Browsing History",
    cachedImagesFilesOption = "Cached Images & Files",
    clearCacheCheckbox = "Cached Images & Files",
    cookiesSiteDataOption = "Cookies & Site Data",
    clearCookiesCheckbox = "Cookies & Site Data",
    clearNowButton = "Clear Now",
    qrScannerTitle = "QR & Barcode Scanner",
    qrScannerSubtitle = "LIVE CAMERA • GALLERY • MANUAL",
    qrScanAgainButton = "Scan Again",
    qrOpenWebsiteButton = "Open Website",
    qrSearchCodeButton = "Search Code",
    qrCopyButton = "Copy",
    qrScanFromGallery = "Scan from Gallery Photo",
    qrManualInputHint = "Or paste / type code or URL…"
)

val HindiNovaStrings = NovaStrings(
    languageCode = "hi",
    isHindi = true,
    appTitleStandard = "नोवा ब्राउज़र",
    appTitlePrivate = "नोवा प्राइवेट",
    appSubtitleStandard = "तेज़, सुरक्षित और विज्ञापन-मुक्त वेब ब्राउज़िंग",
    appSubtitlePrivate = "गुप्त ब्राउज़िंग • इतिहास और कुकीज़ बंद हैं",
    homeSubtitleStandard = "सुरक्षित • विज्ञापन-मुक्त",
    homeSubtitleIncognito = "गुप्त • प्राइवेट मोड",
    headerBadgeStandard = "सुरक्षित • विज्ञापन-मुक्त",
    headerBadgePrivate = "गुप्त • प्राइवेट मोड",
    searchPlaceholder = "खोजें या वेबसाइट URL लिखें",
    shieldActiveStatus = "शील्ड सक्रिय • इस टैब पर कोई ट्रैकर नहीं",
    shieldActivePrivateStatus = "शील्ड सक्रिय • प्राइवेट टैब",
    shieldActiveNoTrackers = "शील्ड सक्रिय • इस टैब पर कोई ट्रैकर नहीं",
    adShieldOff = "ऐड-शील्ड बंद",
    blockedTodayLabel = "आज रोके गए",
    adsLabel = "विज्ञापन",
    incognitoOn = "गुप्त मोड: चालू",
    incognitoOff = "बंद",
    incognitoOffBadge = "बंद",
    incognitoOnBadge = "चालू • गुप्त",
    incognitoOnPrivate = "चालू • गुप्त",
    incognitoLabel = "गुप्त मोड",
    downloadsTitle = "डाउनलोड",
    filesCountSuffix = "फ़ाइलें",
    bookmarksTitle = "बुकमार्क",
    savedCountSuffix = "सहेजे गए",
    historyTitle = "इतिहास",
    visitsCountSuffix = "विज़िट",
    quickShortcutsTitle = "त्वरित शॉर्टकट",
    addSiteButton = "साइट जोड़ें",
    shortcutLabel = "शॉर्टकट",
    privateTabNotice = "प्राइवेट टैब सक्रिय: इस टैब में देखे गए पेज आपके ब्राउज़र इतिहास, कुकी स्टोर या खोज इतिहास में सहेजे नहीं जाएंगे।",
    trackerSavedFooterPrefix = "ट्रैकर्स रोके गए",
    savedBandwidthSuffix = "बचत",
    editShortcutDialogTitle = "शॉर्टकट संपादित करें",
    addShortcutDialogTitle = "नया शॉर्टकट जोड़ें",
    siteTitleLabel = "साइट का नाम",
    websiteUrlLabel = "वेबसाइट URL",
    saveButton = "सहेजें",
    addButton = "जोड़ें",
    cancelButton = "रद्द करें",
    editAction = "संपादित",
    doneAction = "पूर्ण",
    deleteAction = "हटाएं",
    navHome = "होम",
    navSaved = "बुकमार्क",
    navTabs = "टैब",
    navSettings = "मेनू",
    websiteLinkTitle = "वेबसाइट लिंक",
    searchOrTypeUrlPlaceholder = "वेबसाइट खोजें या URL लिखें…",
    copyAction = "कॉपी",
    shareAction = "शेयर",
    verifiedSecureSsl = "HTTPS सुरक्षित कनेक्शन",
    unverifiedHttp = "असुरक्षित HTTP कनेक्शन",
    controlCenterTitle = "कंट्रोल सेंटर",
    shieldPausedLabel = "शील्ड बंद है",
    copyLinkLabel = "लिंक कॉपी",
    quickActionBookmarks = "बुकमार्क",
    quickActionDownloads = "डाउनलोड",
    quickActionHistory = "इतिहास",
    quickActionShare = "शेयर",
    quickActionCopyLink = "लिंक कॉपी",
    adBlockShieldMenuTitle = "ऐड-ब्लॉक शील्ड",
    modeMinimalTitle = "Minimal",
    modeProTitle = "Pro",
    modeMaxTitle = "Max",
    blockedCountSuffix = "रोके गए",
    rulesCountWord = "नियम",
    filtersCountSuffix = "फ़िल्टर",
    fastSuffix = "तेज़",
    blockedAdsFilterModesTitle = "फ़िल्टर मोड",
    filterModesTitle = "फ़िल्टर मोड",
    inSessionSuffix = "सत्र में",
    blockedAdsFilterModesSubtitle = "सभी सक्रिय नियम और फ़िल्टर देखें",
    filterModesSubtitle = "सभी सक्रिय नियम और फ़िल्टर देखें",
    configureButton = "सेटिंग",
    desktopSiteModeActive = "डेस्कटॉप साइट (सक्रिय • 1280px)",
    desktopSiteMode = "डेस्कटॉप साइट",
    darkModeTheme = "डार्क थीम",
    incognitoPrivateMode = "गुप्त / प्राइवेट मोड",
    nothingIsSavedSubtitle = "कुछ भी सहेजा नहीं जाता",
    languageSectionTitle = "ऐप की भाषा (Language)",
    languageSystemOption = "सिस्टम भाषा",
    languageEnglishOption = "English",
    languageHindiOption = "हिन्दी",
    textZoomLabel = "टेक्स्ट ज़ूम",
    resetButton = "रीसेट",
    resetZoomButton = "रीसेट",
    findInPageButton = "पेज में खोजें",
    clearDataButton = "डेटा साफ़ करें",
    bookmarkPageButton = "बुकमार्क",
    bookmarkButton = "बुकमार्क",
    savedPageButton = "सहेजा गया",
    savedButton = "सहेजा गया",
    inspectPageButton = "इंस्पेक्ट",
    inspectButton = "इंस्पेक्ट",
    videosPageButton = "वीडियो",
    videosButton = "वीडियो",
    activeRulesSuffix = "सक्रिय नियम",
    shieldDisabledSubtitle = "शील्ड बंद है • चालू करने के लिए टैप करें",
    adShieldHeaderDisabled = "शील्ड बंद है • चालू करने के लिए टैप करें",
    hostFiltersLabel = "होस्ट",
    pathFiltersLabel = "पाथ/स्क्रिप्ट",
    cosmeticFiltersLabel = "DOM फ़िल्टर",
    filterAll = "सभी",
    filterTab = "टैब",
    filterAds = "विज्ञापन",
    filterTrackers = "ट्रैकर्स",
    filterRules = "नियम",
    filterChipAll = "सभी",
    filterChipTab = "टैब",
    filterChipAds = "विज्ञापन",
    filterChipTrackers = "ट्रैकर्स",
    filterChipRules = "नियम",
    thisPageLabel = "यह पेज",
    cleanBadge = "सुरक्षित",
    protectedBadge = "रक्षित",
    siteCleanBadge = "सुरक्षित",
    siteProtectedBadge = "रक्षित",
    isActiveLabel = "सक्रिय है",
    adBlockerOffTitle = "ऐड-ब्लॉकर वर्तमान में बंद है",
    shieldDisabledEmptyTitle = "ऐड-ब्लॉकर वर्तमान में बंद है",
    minimalSummaryDesc = "आवश्यक नियम: तेज़ और हल्का। सभी होस्ट, पाथ और DOM फ़िल्टर देखने के लिए नियम (Rules) खोलें।",
    proSummaryDesc = "प्रो नियम: Minimal + 80+ विज्ञापन नेटवर्क और ट्रैकिंग पिक्सेल शामिल हैं।",
    maxSummaryDesc = "मैक्स नियम: Minimal + Pro + YouTube विज्ञापन और ऑटो-स्किप फ़िल्टर शामिल हैं।",
    pauseOnSiteButton = "इस साइट पर रोकें",
    resumeOnSiteButton = "इस साइट पर चालू करें",
    customRulesButton = "कस्टम नियम",
    addCustomRuleButton = "+ नियम जोड़ें",
    openFolderButton = "फ़ोल्डर खोलें",
    downloadsFolderSubtitle = "फ़ोल्डर • Download/NovaBrowser/Videos/",
    downloadsFolderLabel = "फ़ोल्डर • Download/NovaBrowser/Videos/",
    downloadTabOngoing = "जारी (Ongoing)",
    downloadTabSuccess = "सफल (Success)",
    downloadTabFailure = "विफल (Failure)",
    sortNewest = "क्रम: नवीनतम",
    sortOldest = "क्रम: पुराना",
    noOngoingDownloadsTitle = "कोई जारी डाउनलोड नहीं",
    noOngoingDownloadsSubtitle = "चालू या रोके गए (Paused) डाउनलोड यहाँ वास्तविक समय में दिखाई देंगे।",
    noOngoingDownloadsSub = "चालू या रोके गए (Paused) डाउनलोड यहाँ वास्तविक समय में दिखाई देंगे।",
    noSuccessDownloadsTitle = "अभी तक कोई डाउनलोड वीडियो नहीं",
    noSuccessDownloadsSubtitle = "किसी भी वेबसाइट पर HTML5 वीडियो प्लेयर को दबाए रखें और सीधे MP4 डाउनलोड करें।",
    noSuccessDownloadsSub = "किसी भी वेबसाइट पर HTML5 वीडियो प्लेयर को दबाए रखें और सीधे MP4 डाउनलोड करें।",
    noFailureDownloadsTitle = "कोई विफल डाउनलोड नहीं",
    noFailureDownloadsSubtitle = "यदि कोई डाउनलोड विफल होता है, तो वह पुनः प्रयास करने के लिए यहाँ दिखाई देगा।",
    noFailureDownloadsSub = "यदि कोई डाउनलोड विफल होता है, तो वह पुनः प्रयास करने के लिए यहाँ दिखाई देगा।",
    downloadStatusCompleted = "पूर्ण (COMPLETED)",
    downloadStatusPaused = "रोका गया (PAUSED)",
    downloadStatusFailed = "विफल • पुनः प्रयास करें",
    downloadStatusStarting = "कतार में...",
    unknownSize = "अज्ञात आकार",
    syncedSuffix = "सिंक किया गया",
    bookmarksHintPrefix = "किसी भी लिंक को दबाए रखें, या यहाँ सहेजने के लिए टूलबार में ",
    bookmarksHintSuffix = " पर टैप करें।",
    addBookmarkButton = "बुकमार्क जोड़ें",
    openAllButton = "सभी खोलें",
    openAllBookmarksButton = "सभी खोलें",
    noBookmarksYet = "अभी तक कोई बुकमार्क सहेजा नहीं गया है। अपनी पसंदीदा वेबसाइट सहेजने के लिए बुकमार्क आइकन दबाएं।",
    clearAllButton = "सब साफ़ करें",
    browsingHistoryTitle = "ब्राउज़िंग इतिहास",
    todaySuffix = "आज",
    yesterdayLabel = "कल",
    historyTodayPrefix = "आज",
    historyYesterdayPrefix = "कल",
    searchHistoryPlaceholder = "इतिहास खोजें",
    noHistoryYet = "अभी तक कोई ब्राउज़िंग इतिहास दर्ज नहीं है।",
    noHistoryTitle = "अभी तक कोई ब्राउज़िंग इतिहास दर्ज नहीं है",
    noHistorySubtitle = "सामान्य मोड में देखी गई वेबसाइटें यहाँ दिखाई देंगी।",
    noMatchingVisits = "कोई परिणाम नहीं मिला",
    noMatchingVisitsTitle = "कोई परिणाम नहीं मिला",
    openTabsTitle = "खुले टैब",
    novaHomeTabTitle = "नोवा होम",
    activeSuffix = "सक्रिय",
    newTabButton = "नया टैब",
    privateTabButton = "प्राइवेट",
    stealthTabBadge = "गुप्त टैब",
    standardTabBadge = "सामान्य टैब",
    clearBrowsingDataTitle = "ब्राउज़िंग डेटा साफ़ करें",
    clearDataDialogTitle = "ब्राउज़िंग डेटा साफ़ करें",
    clearBrowsingDataPrompt = "चुनें कि इस डिवाइस से कौन सा ब्राउज़र डेटा स्थायी रूप से हटाना है:",
    clearDataDialogSubtitle = "चुनें कि इस डिवाइस से कौन सा ब्राउज़र डेटा स्थायी रूप से हटाना है:",
    browsingHistoryOption = "ब्राउज़िंग इतिहास",
    clearHistoryCheckbox = "ब्राउज़िंग इतिहास",
    cachedImagesFilesOption = "कैश की गई छवियां और फ़ाइलें",
    clearCacheCheckbox = "कैश की गई छवियां और फ़ाइलें",
    cookiesSiteDataOption = "कुकीज़ और साइट डेटा",
    clearCookiesCheckbox = "कुकीज़ और साइट डेटा",
    clearNowButton = "अभी साफ़ करें",
    qrScannerTitle = "QR और बारकोड स्कैनर",
    qrScannerSubtitle = "लाइव कैमरा • गैलरी • मैनुअल",
    qrScanAgainButton = "फिर से स्कैन करें",
    qrOpenWebsiteButton = "वेबसाइट खोलें",
    qrSearchCodeButton = "कोड खोजें",
    qrCopyButton = "कॉपी",
    qrScanFromGallery = "गैलरी फ़ोटो से स्कैन करें",
    qrManualInputHint = "या कोड / URL यहाँ लिखें…"
)

val LocalNovaStrings = staticCompositionLocalOf { EnglishNovaStrings }

/**
 * Resolves the effective language ("hi" or "en") based on [AppLanguage] and the Android device's
 * inbuilt system configuration locale.
 */
fun resolveEffectiveLanguageCode(
    appLanguage: AppLanguage,
    configuration: Configuration? = null
): String {
    return when (appLanguage) {
        AppLanguage.ENGLISH -> "en"
        AppLanguage.HINDI -> "hi"
        AppLanguage.SYSTEM -> {
            val systemLocale: Locale = try {
                val sysConfig = Resources.getSystem().configuration
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !sysConfig.locales.isEmpty) {
                    sysConfig.locales[0]
                } else if (configuration != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !configuration.locales.isEmpty) {
                    configuration.locales[0]
                } else {
                    @Suppress("DEPRECATION")
                    sysConfig.locale ?: Locale.getDefault()
                }
            } catch (_: Exception) {
                Locale.getDefault()
            }
            if (systemLocale.language.lowercase(Locale.ROOT).startsWith("hi")) {
                "hi"
            } else {
                "en"
            }
        }
    }
}

@Composable
fun ProvideNovaStrings(
    appLanguage: AppLanguage,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val baseContext = LocalContext.current

    val effectiveLangCode = remember(appLanguage, configuration) {
        resolveEffectiveLanguageCode(appLanguage, configuration)
    }

    val localizedContext = remember(baseContext, effectiveLangCode) {
        try {
            val targetLocale = if (effectiveLangCode == "hi") {
                Locale.forLanguageTag("hi-IN")
            } else {
                Locale.ENGLISH
            }
            val newConfig = Configuration(baseContext.resources.configuration)
            newConfig.setLocale(targetLocale)
            newConfig.setLayoutDirection(targetLocale)
            baseContext.createConfigurationContext(newConfig)
        } catch (_: Exception) {
            baseContext
        }
    }

    val strings = if (effectiveLangCode == "hi") HindiNovaStrings else EnglishNovaStrings

    CompositionLocalProvider(
        LocalNovaStrings provides strings,
        LocalContext provides localizedContext,
        content = content
    )
}

val novaStrings: NovaStrings
    @Composable
    @ReadOnlyComposable
    get() = LocalNovaStrings.current
