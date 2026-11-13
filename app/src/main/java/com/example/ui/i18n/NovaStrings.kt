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
    val cancelButton: String,
    val editAction: String,
    val deleteAction: String,
    // Top & Bottom Bar
    val searchOrTypeUrlPlaceholder: String,
    val copyAction: String,
    val shareAction: String,
    val verifiedSecureSsl: String,
    val unverifiedHttp: String,
    // 3-Dot Browser Menu
    val adBlockShieldMenuTitle: String,
    val blockedCountSuffix: String,
    val rulesCountSuffix: String,
    val blockedAdsFilterModesTitle: String,
    val inSessionSuffix: String,
    val blockedAdsFilterModesSubtitle: String,
    val configureButton: String,
    val desktopSiteModeActive: String,
    val desktopSiteMode: String,
    val darkModeTheme: String,
    val incognitoPrivateMode: String,
    val languageSectionTitle: String,
    val languageSystemOption: String,
    val languageEnglishOption: String,
    val languageHindiOption: String,
    val textZoomLabel: String,
    val resetButton: String,
    val findInPageButton: String,
    val clearDataButton: String,
    // Downloads Manager (3 Sections: Ongoing, Success, Failure)
    val downloadsFolderSubtitle: String,
    val downloadTabOngoing: String,
    val downloadTabSuccess: String,
    val downloadTabFailure: String,
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
    // Bookmarks, History, Tabs & Clear Data
    val noBookmarksYet: String,
    val clearAllButton: String,
    val noHistoryYet: String,
    val openTabsTitle: String,
    val newTabButton: String,
    val privateTabButton: String,
    val clearBrowsingDataTitle: String,
    val clearBrowsingDataPrompt: String,
    val browsingHistoryOption: String,
    val cachedImagesFilesOption: String,
    val cookiesSiteDataOption: String
)

val EnglishNovaStrings = NovaStrings(
    languageCode = "en",
    isHindi = false,
    appTitleStandard = "Nova Browser",
    appTitlePrivate = "Nova Private",
    appSubtitleStandard = "Fast, private & ad-free web browsing",
    appSubtitlePrivate = "Private Browsing • History & cookies disabled",
    searchPlaceholder = "Search Google or type URL…",
    adShieldOff = "Ad-Shield Off",
    adsLabel = "Ads",
    incognitoOn = "Incognito: ON",
    incognitoLabel = "Incognito",
    downloadsTitle = "Downloads",
    filesCountSuffix = "files",
    bookmarksTitle = "Bookmarks",
    savedCountSuffix = "saved",
    historyTitle = "History",
    visitsCountSuffix = "visits",
    quickShortcutsTitle = "Quick Shortcuts",
    addSiteButton = "Add Site",
    privateTabNotice = "Private Tab Active: Pages you view in this tab won't stick around in your browser history, cookie store, or search history.",
    editShortcutDialogTitle = "Edit Quick Shortcut",
    addShortcutDialogTitle = "Add Quick Shortcut",
    siteTitleLabel = "Site Title",
    websiteUrlLabel = "Website URL",
    saveButton = "Save",
    cancelButton = "Cancel",
    editAction = "Edit",
    deleteAction = "Delete",
    searchOrTypeUrlPlaceholder = "Search or type website URL…",
    copyAction = "Copy",
    shareAction = "Share",
    verifiedSecureSsl = "HTTPS Secure Connection",
    unverifiedHttp = "Unencrypted HTTP Connection",
    adBlockShieldMenuTitle = "Ad-Block Shield",
    blockedCountSuffix = "blocked",
    rulesCountSuffix = "rules",
    blockedAdsFilterModesTitle = "Blocked Ads & Filter Modes",
    inSessionSuffix = "in session",
    blockedAdsFilterModesSubtitle = "Switch Minimal / Pro / Max (YouTube) & inspect all active filter rules",
    configureButton = "Configure",
    desktopSiteModeActive = "Desktop Site Mode (Active • 1280px)",
    desktopSiteMode = "Desktop Site Mode",
    darkModeTheme = "Dark Mode Theme",
    incognitoPrivateMode = "Incognito / Private Mode",
    languageSectionTitle = "App Language",
    languageSystemOption = "System",
    languageEnglishOption = "English",
    languageHindiOption = "Hindi",
    textZoomLabel = "Text Zoom",
    resetButton = "Reset",
    findInPageButton = "Find in Page",
    clearDataButton = "Clear Data",
    downloadsFolderSubtitle = "Folder: Download/NovaBrowser/Videos/",
    downloadTabOngoing = "Ongoing",
    downloadTabSuccess = "Success",
    downloadTabFailure = "Failure",
    noOngoingDownloadsTitle = "No Ongoing Downloads",
    noOngoingDownloadsSubtitle = "Active downloads in progress or paused will appear here in real time.",
    noSuccessDownloadsTitle = "No Completed Downloads Yet",
    noSuccessDownloadsSubtitle = "Successfully downloaded MP4 videos and files will appear in this section.",
    noFailureDownloadsTitle = "No Failed Downloads",
    noFailureDownloadsSubtitle = "Any interrupted or failed downloads will appear here so you can retry them.",
    downloadStatusCompleted = "Completed",
    downloadStatusPaused = "Paused",
    downloadStatusFailed = "Failed • Tap Play to retry",
    downloadStatusStarting = "Starting download…",
    unknownSize = "Unknown size",
    noBookmarksYet = "No bookmarks saved yet. Tap the bookmark icon in the browser bar to save your favorite websites.",
    clearAllButton = "Clear All",
    noHistoryYet = "No browsing history recorded yet.",
    openTabsTitle = "Open Tabs",
    newTabButton = "New Tab",
    privateTabButton = "Private",
    clearBrowsingDataTitle = "Clear Browsing Data",
    clearBrowsingDataPrompt = "Choose which Nova Browser data to permanently remove from this device:",
    browsingHistoryOption = "Browsing History",
    cachedImagesFilesOption = "Cached Images & Files",
    cookiesSiteDataOption = "Cookies & Site Data"
)

val HindiNovaStrings = NovaStrings(
    languageCode = "hi",
    isHindi = true,
    appTitleStandard = "नोवा ब्राउज़र",
    appTitlePrivate = "नोवा प्राइवेट",
    appSubtitleStandard = "तेज़, सुरक्षित और विज्ञापन-मुक्त वेब ब्राउज़िंग",
    appSubtitlePrivate = "गुप्त ब्राउज़िंग • इतिहास और कुकीज़ बंद हैं",
    searchPlaceholder = "Google पर खोजें या URL लिखें…",
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
    cancelButton = "रद्द करें",
    editAction = "संपादित करें",
    deleteAction = "हटाएं",
    searchOrTypeUrlPlaceholder = "वेबसाइट खोजें या URL लिखें…",
    copyAction = "कॉपी",
    shareAction = "शेयर",
    verifiedSecureSsl = "HTTPS सुरक्षित कनेक्शन",
    unverifiedHttp = "असुरक्षित HTTP कनेक्शन",
    adBlockShieldMenuTitle = "ऐड-ब्लॉक शील्ड",
    blockedCountSuffix = "रोके गए",
    rulesCountSuffix = "नियम",
    blockedAdsFilterModesTitle = "रोके गए विज्ञापन और फ़िल्टर मोड",
    inSessionSuffix = "सत्र में",
    blockedAdsFilterModesSubtitle = "Minimal / Pro / Max (YouTube) बदलें और सक्रिय नियम देखें",
    configureButton = "सेटिंग",
    desktopSiteModeActive = "डेस्कटॉप साइट मोड (सक्रिय • 1280px)",
    desktopSiteMode = "डेस्कटॉप साइट मोड",
    darkModeTheme = "डार्क मोड थीम",
    incognitoPrivateMode = "गुप्त / प्राइवेट मोड",
    languageSectionTitle = "ऐप की भाषा (Language)",
    languageSystemOption = "सिस्टम भाषा",
    languageEnglishOption = "English",
    languageHindiOption = "हिन्दी",
    textZoomLabel = "टेक्स्ट ज़ूम",
    resetButton = "रीसेट",
    findInPageButton = "पेज में खोजें",
    clearDataButton = "डेटा साफ़ करें",
    downloadsFolderSubtitle = "फ़ोल्डर: Download/NovaBrowser/Videos/",
    downloadTabOngoing = "जारी (Ongoing)",
    downloadTabSuccess = "सफल (Success)",
    downloadTabFailure = "विफल (Failure)",
    noOngoingDownloadsTitle = "कोई जारी डाउनलोड नहीं",
    noOngoingDownloadsSubtitle = "चालू या रोके गए (Paused) डाउनलोड यहाँ वास्तविक समय में दिखाई देंगे।",
    noSuccessDownloadsTitle = "कोई सफल डाउनलोड नहीं",
    noSuccessDownloadsSubtitle = "सफलतापूर्वक डाउनलोड किए गए MP4 वीडियो और फ़ाइलें यहाँ दिखाई देंगी।",
    noFailureDownloadsTitle = "कोई विफल डाउनलोड नहीं",
    noFailureDownloadsSubtitle = "यदि कोई डाउनलोड विफल होता है, तो वह पुनः प्रयास करने के लिए यहाँ दिखाई देगा।",
    downloadStatusCompleted = "पूर्ण (Completed)",
    downloadStatusPaused = "रोका गया (Paused)",
    downloadStatusFailed = "विफल • पुनः प्रयास के लिए Play दबाएं",
    downloadStatusStarting = "डाउनलोड शुरू हो रहा है…",
    unknownSize = "अज्ञात आकार",
    noBookmarksYet = "अभी तक कोई बुकमार्क सहेजा नहीं गया है। अपनी पसंदीदा वेबसाइट सहेजने के लिए बुकमार्क आइकन दबाएं।",
    clearAllButton = "सब साफ़ करें",
    noHistoryYet = "अभी तक कोई ब्राउज़िंग इतिहास दर्ज नहीं है।",
    openTabsTitle = "खुले टैब",
    newTabButton = "नया टैब",
    privateTabButton = "प्राइवेट",
    clearBrowsingDataTitle = "ब्राउज़िंग डेटा साफ़ करें",
    clearBrowsingDataPrompt = "चुनें कि इस डिवाइस से कौन सा ब्राउज़र डेटा स्थायी रूप से हटाना है:",
    browsingHistoryOption = "ब्राउज़िंग इतिहास",
    cachedImagesFilesOption = "कैश की गई छवियां और फ़ाइलें",
    cookiesSiteDataOption = "कुकीज़ और साइट डेटा"
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
                Locale("hi", "IN")
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
