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
import com.example.data.AdBlockMode
import com.example.data.AppLanguage
import java.util.Locale

data class NovaStrings(
    val isHindi: Boolean,
    // Home Screen
    val appTitleStandard: String,
    val appTitlePrivate: String,
    val appSubtitleStandard: String,
    val appSubtitlePrivate: String,
    val searchPlaceholder: String,
    val adShieldOff: String,
    val adsLabel: String,
    val incognitoOn: String,
    val incognitoLabel: String,
    val downloadsTitle: String,
    val filesCountSuffix: String,
    val bookmarksTitle: String,
    val savedCountSuffix: String,
    val historyTitle: String,
    val visitsCountSuffix: String,
    val quickShortcutsTitle: String,
    val addSiteButton: String,
    val privateTabNotice: String,
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
    // Control Center / 3-Dot Menu
    val controlCenterTitle: String,
    val quickActionBookmarks: String,
    val quickActionDownloads: String,
    val quickActionHistory: String,
    val quickActionShare: String,
    val quickActionCopyLink: String,
    val adBlockShieldMenuTitle: String,
    val modeMinimalTitle: String,
    val modeProTitle: String,
    val modeMaxTitle: String,
    val rulesCountWord: String,
    val filterModesTitle: String,
    val filterModesSubtitle: String,
    val configureButton: String,
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
    val findInPageButton: String,
    val clearDataButton: String,
    val bookmarkButton: String,
    val savedButton: String,
    val inspectButton: String,
    val videosButton: String,
    // Ad-Block Shield Sheet
    val adShieldHeaderDisabled: String,
    val filterChipAll: String,
    val filterChipTab: String,
    val filterChipAds: String,
    val filterChipTrackers: String,
    val filterChipRules: String,
    val cleanBadge: String,
    val protectedBadge: String,
    val shieldDisabledEmptyTitle: String,
    val pauseOnSiteButton: String,
    val resumeOnSiteButton: String,
    val addCustomRuleButton: String,
    // Downloads Manager (Ongoing, Success, Failure)
    val openFolderButton: String,
    val downloadTabOngoing: String,
    val downloadTabSuccess: String,
    val downloadTabFailure: String,
    val sortNewest: String,
    val sortOldest: String,
    val noOngoingDownloadsTitle: String,
    val noOngoingDownloadsSubtitle: String,
    val noSuccessDownloadsTitle: String,
    val noSuccessDownloadsSubtitle: String,
    val noFailureDownloadsTitle: String,
    val noFailureDownloadsSubtitle: String,
    val downloadStatusCompleted: String,
    val downloadStatusPaused: String,
    val downloadStatusFailed: String,
    val downloadStatusStarting: String,
    val unknownSize: String,
    // Bookmarks, History, Tabs, Clear Data
    val addBookmarkButton: String,
    val openAllButton: String,
    val clearAllButton: String,
    val browsingHistoryTitle: String,
    val todaySuffix: String,
    val yesterdayLabel: String,
    val searchHistoryPlaceholder: String,
    val noHistoryTitle: String,
    val noHistorySubtitle: String,
    val noMatchingVisitsTitle: String,
    val openTabsTitle: String,
    val novaHomeTabTitle: String,
    val newTabButton: String,
    val privateTabButton: String,
    val stealthTabBadge: String,
    val standardTabBadge: String,
    val clearDataDialogTitle: String,
    val clearDataDialogSubtitle: String,
    val clearHistoryCheckbox: String,
    val clearCacheCheckbox: String,
    val clearCookiesCheckbox: String,
    val clearNowButton: String
) {
    fun adBlockModeLabel(mode: AdBlockMode): String = when (mode) {
        AdBlockMode.MINIMAL -> modeMinimalTitle
        AdBlockMode.PRO -> modeProTitle
        AdBlockMode.MAX -> modeMaxTitle
    }

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

    fun rulesCountSuffix(count: Int): String = "$count $rulesCountWord"

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
            "न्यूनतम नियम ($activeRulesCount सक्रिय नियम): तेज़ और हल्का। सभी होस्ट, पाथ और DOM फ़िल्टर देखने के लिए नियम खोलें।"
        } else {
            "Essential ruleset ($activeRulesCount active rules): fast and lightweight. Open Rules ($activeRulesCount) to inspect every host, path and cosmetic filter."
        }

    fun shieldProEmptyDesc(activeRulesCount: Int): String =
        if (isHindi) {
            "अधिक नियम ($activeRulesCount संचयी नियम): न्यूनतम + 80+ विज्ञापन नेटवर्क और ट्रैकिंग पिक्सेल शामिल हैं।"
        } else {
            "Pro ruleset ($activeRulesCount cumulative rules): includes Minimal + 80+ ad networks, telemetry & tracking pixels. Open Rules ($activeRulesCount) to inspect."
        }

    fun shieldMaxEmptyDesc(activeRulesCount: Int): String =
        if (isHindi) {
            "अधिकतम नियम ($activeRulesCount संचयी नियम): न्यूनतम + अधिक + YouTube विज्ञापन और ऑटो-स्किप फ़िल्टर शामिल हैं।"
        } else {
            "Max ruleset ($activeRulesCount cumulative rules): includes Minimal + Pro + YouTube ad hosts & cosmetic filters. Open Rules ($activeRulesCount) to inspect."
        }
}

val EnglishNovaStrings = NovaStrings(
    isHindi = false,
    appTitleStandard = "Nova Browser",
    appTitlePrivate = "Nova Private",
    appSubtitleStandard = "Fast, private & ad-free web browsing",
    appSubtitlePrivate = "Private Browsing • History & cookies disabled",
    searchPlaceholder = "Search or type a URL",
    adShieldOff = "Ad-Shield Off",
    adsLabel = "Ads",
    incognitoOn = "Incognito: ON",
    incognitoLabel = "Incognito",
    downloadsTitle = "Downloads",
    filesCountSuffix = "FILES",
    bookmarksTitle = "Bookmarks",
    savedCountSuffix = "SAVED",
    historyTitle = "History",
    visitsCountSuffix = "VISITS",
    quickShortcutsTitle = "Quick shortcuts",
    addSiteButton = "Add site",
    privateTabNotice = "Private Tab Active: Pages you view in this tab won't stick around in your browser history, cookie store, or search history.",
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
    navSaved = "BOOKMARK",
    navTabs = "TABS",
    navSettings = "SETTINGS",
    websiteLinkTitle = "Website Link",
    searchOrTypeUrlPlaceholder = "Search or type website URL…",
    controlCenterTitle = "Control center",
    quickActionBookmarks = "BOOKMARKS",
    quickActionDownloads = "DOWNLOADS",
    quickActionHistory = "HISTORY",
    quickActionShare = "SHARE",
    quickActionCopyLink = "COPY LINK",
    adBlockShieldMenuTitle = "Ad-block shield",
    modeMinimalTitle = "Minimal",
    modeProTitle = "Pro",
    modeMaxTitle = "Max",
    rulesCountWord = "RULES",
    filterModesTitle = "Filter modes",
    filterModesSubtitle = "Inspect every active rule",
    configureButton = "Configure",
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
    findInPageButton = "Find in page",
    clearDataButton = "Clear data",
    bookmarkButton = "Bookmark",
    savedButton = "Saved",
    inspectButton = "Inspect",
    videosButton = "Videos",
    adShieldHeaderDisabled = "SHIELD DISABLED • TAP TO ENABLE",
    filterChipAll = "All",
    filterChipTab = "Tab",
    filterChipAds = "Ads",
    filterChipTrackers = "Trackers",
    filterChipRules = "Rules",
    cleanBadge = "Clean",
    protectedBadge = "Protected",
    shieldDisabledEmptyTitle = "Ad-Blocker is currently turned off",
    pauseOnSiteButton = "Pause on this site",
    resumeOnSiteButton = "Resume on this site",
    addCustomRuleButton = "+ Add custom rule",
    openFolderButton = "Open folder",
    downloadTabOngoing = "Ongoing",
    downloadTabSuccess = "Success",
    downloadTabFailure = "Failure",
    sortNewest = "Sort: Newest",
    sortOldest = "Sort: Oldest",
    noOngoingDownloadsTitle = "No ongoing downloads",
    noOngoingDownloadsSubtitle = "Active downloads in progress or paused will appear here in real time.",
    noSuccessDownloadsTitle = "No downloaded videos yet",
    noSuccessDownloadsSubtitle = "Long-press any HTML5 video player on a site to grab the MP4 straight into this folder.",
    noFailureDownloadsTitle = "No failed downloads",
    noFailureDownloadsSubtitle = "Any interrupted or failed downloads will appear here so you can retry them.",
    downloadStatusCompleted = "COMPLETED",
    downloadStatusPaused = "PAUSED",
    downloadStatusFailed = "FAILED • TAP RETRY",
    downloadStatusStarting = "QUEUED...",
    unknownSize = "Unknown size",
    addBookmarkButton = "Add bookmark",
    openAllButton = "Open all",
    clearAllButton = "Clear all",
    browsingHistoryTitle = "Browsing history",
    todaySuffix = "TODAY",
    yesterdayLabel = "YESTERDAY",
    searchHistoryPlaceholder = "Search history",
    noHistoryTitle = "No browsing history recorded yet",
    noHistorySubtitle = "Websites you visit in standard mode will appear here.",
    noMatchingVisitsTitle = "No matching visits",
    openTabsTitle = "Open Tabs",
    novaHomeTabTitle = "Nova Home",
    newTabButton = "New Tab",
    privateTabButton = "Private",
    stealthTabBadge = "STEALTH TAB",
    standardTabBadge = "STANDARD TAB",
    clearDataDialogTitle = "Clear Browsing Data",
    clearDataDialogSubtitle = "Choose which Nova Browser data to permanently remove from this device:",
    clearHistoryCheckbox = "Browsing History",
    clearCacheCheckbox = "Cached Images & Files",
    clearCookiesCheckbox = "Cookies & Site Data",
    clearNowButton = "Clear Now"
)

val HindiNovaStrings = NovaStrings(
    isHindi = true,
    appTitleStandard = "नोवा ब्राउज़र",
    appTitlePrivate = "नोवा प्राइवेट",
    appSubtitleStandard = "तेज़, सुरक्षित और विज्ञापन-मुक्त वेब ब्राउज़िंग",
    appSubtitlePrivate = "गुप्त ब्राउज़िंग • इतिहास और कुकीज़ बंद हैं",
    searchPlaceholder = "खोजें या वेबसाइट URL लिखें",
    adShieldOff = "ऐड-शील्ड बंद",
    adsLabel = "विज्ञापन",
    incognitoOn = "गुप्त मोड: चालू",
    incognitoLabel = "गुप्त मोड",
    downloadsTitle = "डाउनलोड",
    filesCountSuffix = "फ़ाइलें",
    bookmarksTitle = "बुकमार्क",
    savedCountSuffix = "सहेजे गए",
    historyTitle = "इतिहास",
    visitsCountSuffix = "विज़िट",
    quickShortcutsTitle = "त्वरित शॉर्टकट",
    addSiteButton = "साइट जोड़ें",
    privateTabNotice = "प्राइवेट टैब सक्रिय: इस टैब में देखे गए पेज आपके ब्राउज़र इतिहास, कुकी स्टोर या खोज इतिहास में सहेजे नहीं जाएंगे।",
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
    navSettings = "सेटिंग्स",
    websiteLinkTitle = "वेबसाइट लिंक",
    searchOrTypeUrlPlaceholder = "वेबसाइट खोजें या URL लिखें…",
    controlCenterTitle = "कंट्रोल सेंटर",
    quickActionBookmarks = "बुकमार्क",
    quickActionDownloads = "डाउनलोड",
    quickActionHistory = "इतिहास",
    quickActionShare = "शेयर",
    quickActionCopyLink = "लिंक कॉपी",
    adBlockShieldMenuTitle = "ऐड-ब्लॉक शील्ड",
    modeMinimalTitle = "न्यूनतम",
    modeProTitle = "अधिक",
    modeMaxTitle = "अधिकतम",
    rulesCountWord = "नियम",
    filterModesTitle = "फ़िल्टर मोड",
    filterModesSubtitle = "सभी सक्रिय नियम और फ़िल्टर देखें",
    configureButton = "सेटिंग",
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
    findInPageButton = "पेज में खोजें",
    clearDataButton = "डेटा साफ़ करें",
    bookmarkButton = "बुकमार्क",
    savedButton = "सहेजा गया",
    inspectButton = "इंस्पेक्ट",
    videosButton = "वीडियो",
    adShieldHeaderDisabled = "शील्ड बंद है • चालू करने के लिए टैप करें",
    filterChipAll = "सभी",
    filterChipTab = "टैब",
    filterChipAds = "विज्ञापन",
    filterChipTrackers = "ट्रैकर्स",
    filterChipRules = "नियम",
    cleanBadge = "सुरक्षित",
    protectedBadge = "रक्षित",
    shieldDisabledEmptyTitle = "ऐड-ब्लॉकर वर्तमान में बंद है",
    pauseOnSiteButton = "इस साइट पर रोकें",
    resumeOnSiteButton = "इस साइट पर चालू करें",
    addCustomRuleButton = "+ नियम जोड़ें",
    openFolderButton = "फ़ोल्डर खोलें",
    downloadTabOngoing = "जारी (Ongoing)",
    downloadTabSuccess = "सफल (Success)",
    downloadTabFailure = "विफल (Failure)",
    sortNewest = "क्रम: नवीनतम",
    sortOldest = "क्रम: पुराना",
    noOngoingDownloadsTitle = "कोई जारी डाउनलोड नहीं",
    noOngoingDownloadsSubtitle = "चालू या रोके गए (Paused) डाउनलोड यहाँ वास्तविक समय में दिखाई देंगे।",
    noSuccessDownloadsTitle = "अभी तक कोई डाउनलोड वीडियो नहीं",
    noSuccessDownloadsSubtitle = "किसी भी वेबसाइट पर HTML5 वीडियो प्लेयर को दबाए रखें और सीधे MP4 डाउनलोड करें।",
    noFailureDownloadsTitle = "कोई विफल डाउनलोड नहीं",
    noFailureDownloadsSubtitle = "यदि कोई डाउनलोड विफल होता है, तो वह पुनः प्रयास करने के लिए यहाँ दिखाई देगा।",
    downloadStatusCompleted = "पूर्ण (COMPLETED)",
    downloadStatusPaused = "रोका गया (PAUSED)",
    downloadStatusFailed = "विफल • पुनः प्रयास करें",
    downloadStatusStarting = "कतार में...",
    unknownSize = "अज्ञात आकार",
    addBookmarkButton = "बुकमार्क जोड़ें",
    openAllButton = "सभी खोलें",
    clearAllButton = "सब साफ़ करें",
    browsingHistoryTitle = "ब्राउज़िंग इतिहास",
    todaySuffix = "आज",
    yesterdayLabel = "कल",
    searchHistoryPlaceholder = "इतिहास खोजें",
    noHistoryTitle = "अभी तक कोई ब्राउज़िंग इतिहास दर्ज नहीं है",
    noHistorySubtitle = "सामान्य मोड में देखी गई वेबसाइटें यहाँ दिखाई देंगी।",
    noMatchingVisitsTitle = "कोई परिणाम नहीं मिला",
    openTabsTitle = "खुले टैब",
    novaHomeTabTitle = "नोवा होम",
    newTabButton = "नया टैब",
    privateTabButton = "प्राइवेट",
    stealthTabBadge = "गुप्त टैब",
    standardTabBadge = "सामान्य टैब",
    clearDataDialogTitle = "ब्राउज़िंग डेटा साफ़ करें",
    clearDataDialogSubtitle = "चुनें कि इस डिवाइस से कौन सा ब्राउज़र डेटा स्थायी रूप से हटाना है:",
    clearHistoryCheckbox = "ब्राउज़िंग इतिहास",
    clearCacheCheckbox = "कैश की गई छवियां और फ़ाइलें",
    clearCookiesCheckbox = "कुकीज़ और साइट डेटा",
    clearNowButton = "अभी साफ़ करें"
)

val LocalNovaStrings = staticCompositionLocalOf { EnglishNovaStrings }

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
            if (systemLocale.language.lowercase(Locale.ROOT).startsWith("hi")) "hi" else "en"
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
            val targetLocale = if (effectiveLangCode == "hi") Locale.forLanguageTag("hi-IN") else Locale.ENGLISH
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
