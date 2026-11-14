package com.example.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.browser.BrowserUrlUtils
import com.example.data.ActiveSheet
import com.example.data.WebPermissionKind
import com.example.ui.components.AddShortcutDialog
import com.example.ui.components.AlertDialog
import com.example.ui.components.BackgroundMediaMiniBar
import com.example.ui.components.BlockedAdsSheet
import com.example.ui.components.BookmarksSheet
import com.example.ui.components.BottomBrowserBar
import com.example.ui.components.BrowserMenuSheet
import com.example.ui.components.Button
import com.example.ui.components.ClearBrowsingDataDialog
import com.example.ui.components.DevToolsInspectorPanel
import com.example.ui.components.DownloadsManagerSheet
import com.example.ui.components.FindInPageTopBar
import com.example.ui.components.HistorySheet
import com.example.ui.components.NovaHomeContent
import com.example.ui.components.SslSecurityInfoDialog
import com.example.ui.components.TabsSwitcherSheet
import com.example.ui.components.Text
import com.example.ui.components.TextButton
import com.example.ui.components.TopWebsiteHeaderBar
import com.example.ui.components.VideoLongPressBottomSheet
import com.example.ui.i18n.ProvideNovaStrings
import com.example.ui.i18n.novaStrings
import com.example.ui.theme.MaterialTheme
import com.example.ui.theme.NovaBrowserTheme
import com.example.viewmodel.BrowserViewModel
import kotlinx.coroutines.delay

@Composable
fun NovaBrowserApp(
    viewModel: BrowserViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val siteConfigs by viewModel.siteConfigs.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val shortcuts by viewModel.shortcuts.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()

    val activeTab = uiState.activeTab
    val isBookmarked = remember(bookmarks, activeTab.url) {
        bookmarks.any { it.url == activeTab.url }
    }
    var cookieRefreshTick by remember { mutableIntStateOf(0) }
    var previousIncognito by remember { mutableStateOf(activeTab.isIncognito) }
    var incognitoWaveProgress by remember { mutableFloatStateOf(0f) }
    var showSplashOverlay by rememberSaveable { mutableStateOf(true) }
    var splashAlpha by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(Unit) {
        if (showSplashOverlay) {
            delay(480L)
            val fadeStart = withFrameMillis { it }
            var elapsed = 0L
            while (elapsed < 220L && showSplashOverlay) {
                elapsed = withFrameMillis { it } - fadeStart
                splashAlpha = (1f - (elapsed / 220f)).coerceIn(0f, 1f)
            }
            showSplashOverlay = false
        }
    }

    LaunchedEffect(activeTab.isIncognito) {
        if (activeTab.isIncognito != previousIncognito) {
            previousIncognito = activeTab.isIncognito
            val start = withFrameMillis { it }
            var elapsed = 0L
            while (elapsed < 420L) {
                elapsed = withFrameMillis { it } - start
                val t = (elapsed / 420f).coerceIn(0f, 1f)
                incognitoWaveProgress = 1f - (1f - t) * (1f - t)
            }
            incognitoWaveProgress = 0f
        }
    }

    LaunchedEffect(Unit) {
        viewModel.toastEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val legacyStoragePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.downloadSelectedVideo()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.downloadSelectedVideo()
    }

    val webRuntimePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val anyGranted = results.values.any { it }
        viewModel.resolvePendingWebPermission(userAllowed = true, osGranted = anyGranted)
    }

    val requestOsPermissionsForWebKind: (WebPermissionKind) -> Unit = { kind ->
        val needed = when (kind) {
            WebPermissionKind.LOCATION -> arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            WebPermissionKind.CAMERA -> arrayOf(Manifest.permission.CAMERA)
            WebPermissionKind.MICROPHONE -> arrayOf(Manifest.permission.RECORD_AUDIO)
            WebPermissionKind.CAMERA_AND_MICROPHONE -> arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        }
        val allGranted = when (kind) {
            WebPermissionKind.LOCATION -> needed.any {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
            else -> needed.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        }
        if (allGranted) {
            viewModel.resolvePendingWebPermission(userAllowed = true, osGranted = true)
        } else {
            webRuntimePermissionLauncher.launch(needed)
        }
    }

    LaunchedEffect(uiState.pendingWebPermission?.id) {
        val pending = uiState.pendingWebPermission
        if (pending != null && pending.isPreAllowed) {
            requestOsPermissionsForWebKind(pending.kind)
        }
    }

    val triggerVideoDownloadWithPermissionCheck = {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                legacyStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                viewModel.downloadSelectedVideo()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notifGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!notifGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.downloadSelectedVideo()
            }
        } else {
            viewModel.downloadSelectedVideo()
        }
    }

    BackHandler(
        enabled = uiState.fullscreenCustomView != null ||
            uiState.isFindInPageVisible ||
            uiState.activeSheet != ActiveSheet.NONE ||
            uiState.devToolsState.isVisible ||
            !activeTab.isHomePage
    ) {
        viewModel.goBack()
    }

    ProvideNovaStrings(appLanguage = preferences.appLanguage) {
        NovaBrowserTheme(
            darkTheme = preferences.isDarkMode,
            isIncognito = activeTab.isIncognito
        ) {
            if (uiState.fullscreenCustomView != null) {
                AndroidView(
                    factory = { ctx ->
                        FrameLayout(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setBackgroundColor(android.graphics.Color.BLACK)
                        }
                    },
                    update = { container ->
                        val customView = uiState.fullscreenCustomView
                        if (customView != null && customView.parent != container) {
                            (customView.parent as? ViewGroup)?.removeView(customView)
                            container.removeAllViews()
                            container.addView(
                                customView,
                                FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .testTag("fullscreen_video_container")
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (!activeTab.isHomePage) {
                            TopWebsiteHeaderBar(
                                activeTab = activeTab,
                                isBookmarked = isBookmarked,
                                onSslIconClick = {
                                    viewModel.setActiveSheet(ActiveSheet.SSL_INFO_DIALOG)
                                },
                                onToggleBookmarkClick = viewModel::toggleBookmarkForActiveTab,
                                onSubmitUrlOrSearch = viewModel::loadUrlOrSearch,
                                onCopyUrlClick = { url ->
                                    copyTextToClipboard(context, "Website Link", url)
                                    viewModel.emitToast("Website link copied to clipboard")
                                },
                                onShareUrlClick = { title, url ->
                                    shareUrl(context, title, url)
                                }
                            )
                        }

                        if (uiState.isFindInPageVisible) {
                            FindInPageTopBar(
                                query = uiState.findQuery,
                                activeOrdinal = uiState.findActiveOrdinal,
                                totalMatches = uiState.findTotalMatches,
                                onQueryChange = viewModel::updateFindQuery,
                                onFindNext = { viewModel.findNext(true) },
                                onFindPrev = { viewModel.findNext(false) },
                                onClose = viewModel::closeFindInPage,
                                modifier = if (activeTab.isHomePage) Modifier.statusBarsPadding() else Modifier
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            if (activeTab.isHomePage) {
                                NovaHomeContent(
                                    isIncognito = activeTab.isIncognito,
                                    isAdBlockEnabled = preferences.isAdBlockEnabled,
                                    adBlockMode = preferences.adBlockMode,
                                    totalAdsBlocked = preferences.totalAdsBlocked,
                                    downloadsCount = downloads.size,
                                    bookmarksCount = bookmarks.size,
                                    historyCount = history.size,
                                    shortcuts = shortcuts,
                                    onSearchOrLoadUrl = viewModel::loadUrlOrSearch,
                                    onOpenDownloads = { viewModel.setActiveSheet(ActiveSheet.DOWNLOADS) },
                                    onOpenBookmarks = { viewModel.setActiveSheet(ActiveSheet.BOOKMARKS) },
                                    onOpenHistory = { viewModel.setActiveSheet(ActiveSheet.HISTORY) },
                                    onOpenBlockedAds = { viewModel.setActiveSheet(ActiveSheet.BLOCKED_ADS) },
                                    onToggleIncognito = viewModel::toggleIncognitoMode,
                                    onAddShortcutClick = { viewModel.setActiveSheet(ActiveSheet.ADD_SHORTCUT_DIALOG) },
                                    onDeleteShortcut = viewModel::deleteQuickShortcut,
                                    onEditShortcut = viewModel::updateQuickShortcut,
                                    modifier = if (!uiState.isFindInPageVisible) Modifier.statusBarsPadding() else Modifier
                                )
                            } else {
                                AndroidView(
                                    factory = { ctx ->
                                        FrameLayout(ctx).apply {
                                            layoutParams = ViewGroup.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.MATCH_PARENT
                                            )
                                        }
                                    },
                                    update = { container ->
                                        val webView = viewModel.webViewPool.getOrCreateWebView(activeTab)
                                        if (webView.parent != container) {
                                            (webView.parent as? ViewGroup)?.removeView(webView)
                                            container.removeAllViews()
                                            container.addView(
                                                webView,
                                                FrameLayout.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.MATCH_PARENT
                                                )
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("browser_webview_container")
                                )
                            }
                        }

                        if (uiState.devToolsState.isVisible && !activeTab.isHomePage) {
                            DevToolsInspectorPanel(
                                devToolsState = uiState.devToolsState,
                                onSelectTab = viewModel::selectDevToolsTab,
                                onToggleInspectElementPicker = viewModel::toggleInspectElementPicker,
                                onRefreshDevTools = viewModel::refreshDevTools,
                                onSelectDomNode = viewModel::selectDomNode,
                                onApplyElementEdit = viewModel::applyElementEdit,
                                onDeleteDomNode = viewModel::deleteDomNode,
                                onSelectSourceFile = viewModel::selectSourceFile,
                                onFetchRemoteSource = viewModel::fetchRemoteSource,
                                onCopyText = { label, text ->
                                    copyTextToClipboard(context, label, text)
                                    viewModel.emitToast("$label copied to clipboard")
                                },
                                onExecuteConsoleJs = viewModel::executeConsoleJs,
                                onClearConsole = viewModel::clearConsoleLogs,
                                onClearNetwork = viewModel::clearNetworkEntries,
                                onCloseDevTools = viewModel::closeDevTools
                            )
                        }

                        if (uiState.backgroundMedia.isActive) {
                            BackgroundMediaMiniBar(
                                mediaState = uiState.backgroundMedia,
                                onTogglePlayPause = viewModel::toggleBackgroundPlayPause,
                                onClose = viewModel::stopBackgroundPlayer
                            )
                        }

                        BottomBrowserBar(
                            activeTab = activeTab,
                            tabsCount = uiState.tabs.size,
                            isDevToolsVisible = uiState.devToolsState.isVisible,
                            onBackClick = { viewModel.goBack() },
                            onForwardClick = viewModel::goForward,
                            onHomeClick = viewModel::goHome,
                            onReloadOrStopClick = viewModel::reloadOrStop,
                            onSubmitUrlOrSearch = viewModel::loadUrlOrSearch,
                            onSslIconClick = {
                                if (!activeTab.isHomePage) {
                                    viewModel.setActiveSheet(ActiveSheet.SSL_INFO_DIALOG)
                                }
                            },
                            onToggleDevToolsClick = viewModel::toggleDevTools,
                            onScanPageVideosClick = viewModel::scanVideosOnCurrentPage,
                            onOpenBookmarksClick = { viewModel.setActiveSheet(ActiveSheet.BOOKMARKS) },
                            onOpenTabsClick = { viewModel.setActiveSheet(ActiveSheet.TABS) },
                            onOpenMenuClick = { viewModel.setActiveSheet(ActiveSheet.MENU) }
                        )
                    }

                    val waveProgress = incognitoWaveProgress
                    if (waveProgress > 0f) {
                        val waveColor = if (activeTab.isIncognito) Color(0xFF00E5FF) else Color(0xFFFF8A3D)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val maxRadius = size.maxDimension * 1.35f
                            val currentRadius = (maxRadius * waveProgress).coerceAtLeast(1f)
                            val alpha = (1f - waveProgress) * 0.42f
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        waveColor.copy(alpha = alpha * 0.45f),
                                        waveColor.copy(alpha = alpha),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.72f, size.height * 0.36f),
                                    radius = currentRadius
                                ),
                                radius = currentRadius,
                                center = Offset(size.width * 0.72f, size.height * 0.36f)
                            )
                        }
                    }

                    when (uiState.activeSheet) {
                        ActiveSheet.VIDEO_LONG_PRESS -> {
                            uiState.detectedVideo?.let { videoInfo ->
                                VideoLongPressBottomSheet(
                                    videoInfo = videoInfo,
                                    onSelectQuality = viewModel::selectVideoQuality,
                                    onDownloadMp4Click = triggerVideoDownloadWithPermissionCheck,
                                    onCopyVideoLinkClick = { link ->
                                        copyTextToClipboard(context, "Video URL", link)
                                        viewModel.emitToast("Video link copied to clipboard")
                                        viewModel.dismissSheet()
                                    },
                                    onOpenInNewTabClick = { link ->
                                        viewModel.openNewTab(
                                            url = link,
                                            isIncognito = activeTab.isIncognito,
                                            referer = activeTab.url
                                        )
                                    },
                                    onPlayInBackgroundClick = {
                                        viewModel.playVideoInBackground(videoInfo)
                                    },
                                    onDismiss = viewModel::dismissSheet
                                )
                            }
                        }

                        ActiveSheet.TABS -> {
                            TabsSwitcherSheet(
                                tabs = uiState.tabs,
                                activeTabId = uiState.activeTabId,
                                onSelectTab = viewModel::switchTab,
                                onCloseTab = viewModel::closeTab,
                                onDuplicateTab = viewModel::duplicateTab,
                                onNewNormalTab = { viewModel.openNewTab(isIncognito = false) },
                                onNewIncognitoTab = { viewModel.openNewTab(isIncognito = true) },
                                onDismiss = viewModel::dismissSheet
                            )
                        }

                        ActiveSheet.DOWNLOADS -> {
                            DownloadsManagerSheet(
                                downloads = downloads,
                                onPauseDownload = viewModel.downloadHelper::pauseDownload,
                                onResumeDownload = viewModel.downloadHelper::resumeDownload,
                                onOpenFile = { item ->
                                    viewModel.downloadHelper.openDownloadedFile(item).onFailure { err ->
                                        viewModel.emitToast(err.localizedMessage ?: "Unable to open file")
                                    }
                                },
                                onDeleteDownload = viewModel.downloadHelper::deleteDownload,
                                onDismiss = viewModel::dismissSheet
                            )
                        }

                        ActiveSheet.BOOKMARKS -> {
                            BookmarksSheet(
                                bookmarks = bookmarks,
                                onOpenUrl = { url ->
                                    viewModel.dismissSheet()
                                    viewModel.loadUrlOrSearch(url)
                                },
                                onDeleteBookmark = viewModel::deleteBookmark,
                                onDismiss = viewModel::dismissSheet,
                                onAddBookmark = { title, url ->
                                    viewModel.addCustomBookmark(title, url)
                                },
                                onOpenAllBookmarks = { urls ->
                                    viewModel.dismissSheet()
                                    urls.forEachIndexed { idx, url ->
                                        if (idx == 0) {
                                            viewModel.loadUrlOrSearch(url)
                                        } else {
                                            viewModel.openNewTab(url = url, isIncognito = activeTab.isIncognito)
                                        }
                                    }
                                }
                            )
                        }

                        ActiveSheet.HISTORY -> {
                            HistorySheet(
                                history = history,
                                onOpenUrl = { url ->
                                    viewModel.dismissSheet()
                                    viewModel.loadUrlOrSearch(url)
                                },
                                onDeleteHistoryItem = viewModel::deleteHistoryItem,
                                onOpenClearDataDialog = { viewModel.setActiveSheet(ActiveSheet.CLEAR_DATA_DIALOG) },
                                onDismiss = viewModel::dismissSheet
                            )
                        }

                        ActiveSheet.BLOCKED_ADS -> {
                            BlockedAdsSheet(
                                blockedAds = uiState.blockedAdsLog,
                                isAdBlockEnabled = preferences.isAdBlockEnabled,
                                adBlockMode = preferences.adBlockMode,
                                totalAdsBlocked = preferences.totalAdsBlocked,
                                activeTabId = uiState.activeTabId,
                                onToggleAdBlock = viewModel::toggleAdBlock,
                                onSelectAdBlockMode = viewModel::selectAdBlockMode,
                                onCopyAdUrl = { adUrl ->
                                    copyTextToClipboard(context, "Blocked Ad URL", adUrl)
                                    viewModel.emitToast("Copied to clipboard")
                                },
                                onClearLog = viewModel::clearBlockedAdsLog,
                                onDismiss = viewModel::dismissSheet,
                                currentSiteUrl = activeTab.url
                            )
                        }

                        ActiveSheet.QR_SCANNER -> viewModel.dismissSheet()

                        ActiveSheet.MENU -> {
                            BrowserMenuSheet(
                                activeTab = activeTab,
                                isBookmarked = isBookmarked,
                                isAdBlockEnabled = preferences.isAdBlockEnabled,
                                adBlockMode = preferences.adBlockMode,
                                appLanguage = preferences.appLanguage,
                                isDarkMode = preferences.isDarkMode,
                                totalAdsBlocked = preferences.totalAdsBlocked,
                                onToggleBookmark = viewModel::toggleBookmarkForActiveTab,
                                onToggleAdBlock = viewModel::toggleAdBlock,
                                onSelectAdBlockMode = viewModel::selectAdBlockMode,
                                onSelectAppLanguage = viewModel::selectAppLanguage,
                                onOpenBlockedAdsList = { viewModel.setActiveSheet(ActiveSheet.BLOCKED_ADS) },
                                onToggleDesktopMode = viewModel::toggleDesktopMode,
                                onToggleDarkMode = viewModel::toggleDarkMode,
                                onToggleIncognito = {
                                    viewModel.dismissSheet()
                                    viewModel.toggleIncognitoMode()
                                },
                                onUpdateTextZoom = viewModel::updateTextZoom,
                                onOpenFindInPage = viewModel::openFindInPage,
                                onOpenDevTools = {
                                    viewModel.dismissSheet()
                                    viewModel.toggleDevTools()
                                },
                                onScanPageVideos = {
                                    viewModel.dismissSheet()
                                    viewModel.scanVideosOnCurrentPage()
                                },
                                onOpenDownloads = { viewModel.setActiveSheet(ActiveSheet.DOWNLOADS) },
                                onOpenBookmarks = { viewModel.setActiveSheet(ActiveSheet.BOOKMARKS) },
                                onOpenHistory = { viewModel.setActiveSheet(ActiveSheet.HISTORY) },
                                onSharePage = {
                                    viewModel.dismissSheet()
                                    shareUrl(context, activeTab.title, activeTab.url)
                                },
                                onCopyPageLink = {
                                    copyTextToClipboard(context, "Page Link", activeTab.url)
                                    viewModel.emitToast("Link copied to clipboard")
                                    viewModel.dismissSheet()
                                },
                                onOpenClearData = { viewModel.setActiveSheet(ActiveSheet.CLEAR_DATA_DIALOG) },
                                onDismiss = viewModel::dismissSheet
                            )
                        }

                        ActiveSheet.CLEAR_DATA_DIALOG -> {
                            ClearBrowsingDataDialog(
                                onConfirmClear = viewModel::clearBrowsingData,
                                onDismiss = viewModel::dismissSheet
                            )
                        }

                        ActiveSheet.ADD_SHORTCUT_DIALOG -> {
                            AddShortcutDialog(
                                onConfirmAdd = viewModel::addQuickShortcut,
                                onDismiss = viewModel::dismissSheet
                            )
                        }

                        ActiveSheet.SSL_INFO_DIALOG -> {
                            val certDetails = remember(activeTab.id, activeTab.url, activeTab.isLoading, activeTab.sslSummary) {
                                viewModel.webViewPool.getSslCertificateDetails(activeTab)
                            }
                            val siteCookies = remember(activeTab.id, activeTab.url, activeTab.isLoading, cookieRefreshTick) {
                                viewModel.webViewPool.getSiteCookieDetails(activeTab.url)
                            }
                            val currentSiteDomain = remember(activeTab.url) {
                                BrowserUrlUtils.extractDomain(activeTab.url)
                            }
                            val currentSiteConfig = remember(siteConfigs, currentSiteDomain) {
                                viewModel.getSiteConfigForUrl(activeTab.url)
                            }
                            SslSecurityInfoDialog(
                                tab = activeTab,
                                certificateDetails = certDetails,
                                siteCookies = siteCookies,
                                siteConfig = currentSiteConfig,
                                isAdBlockEnabled = preferences.isAdBlockEnabled,
                                isDarkMode = preferences.isDarkMode,
                                onToggleAdBlock = viewModel::toggleAdBlock,
                                onToggleDesktopMode = viewModel::toggleDesktopMode,
                                onToggleDarkMode = viewModel::toggleDarkMode,
                                onUpdateSiteConfig = { reloadPage, transform ->
                                    viewModel.updateSiteConfig(activeTab.url, reloadPage, transform)
                                },
                                onResetSiteConfigAndData = {
                                    viewModel.resetSiteConfigAndClearData(activeTab.url)
                                    cookieRefreshTick++
                                },
                                onClearSingleCookie = { cookieName ->
                                    viewModel.clearSingleSiteCookie(activeTab.url, cookieName)
                                    cookieRefreshTick++
                                    viewModel.emitToast("Deleted cookie $cookieName")
                                },
                                onCopyText = { label, text ->
                                    copyTextToClipboard(context, label, text)
                                    viewModel.emitToast("$label copied to clipboard")
                                },
                                onClearSiteCookies = {
                                    viewModel.clearAllSiteCookiesAndStorage(activeTab.url)
                                    cookieRefreshTick++
                                    viewModel.emitToast("Site cookies & storage cleared")
                                },
                                onDismiss = viewModel::dismissSheet
                            )
                        }

                        ActiveSheet.NONE -> Unit
                    }

                    val pendingPerm = uiState.pendingWebPermission
                    if (pendingPerm != null && !pendingPerm.isPreAllowed) {
                        val resourceLabel = when (pendingPerm.kind) {
                            WebPermissionKind.LOCATION -> if (novaStrings.isHindi) "आपकी लोकेशन" else "your location"
                            WebPermissionKind.CAMERA -> if (novaStrings.isHindi) "आपका कैमरा" else "your camera"
                            WebPermissionKind.MICROPHONE -> if (novaStrings.isHindi) "आपका माइक्रोफ़ोन" else "your microphone"
                            WebPermissionKind.CAMERA_AND_MICROPHONE -> if (novaStrings.isHindi) "आपका कैमरा और माइक्रोफ़ोन" else "your camera and microphone"
                        }
                        val promptTitle = if (novaStrings.isHindi) {
                            "${pendingPerm.domain} $resourceLabel का उपयोग करना चाहता है"
                        } else {
                            "${pendingPerm.domain} wants to use $resourceLabel"
                        }
                        AlertDialog(
                            onDismissRequest = {
                                viewModel.resolvePendingWebPermission(userAllowed = false, osGranted = false)
                            },
                            title = { Text(promptTitle, fontWeight = FontWeight.Bold) },
                            text = {
                                Text(
                                    text = if (novaStrings.isHindi) {
                                        "आप साइट सेटिंग में जाकर कभी भी यह अनुमति बदल सकते हैं।"
                                    } else {
                                        "You can change this anytime in Site Config."
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = { requestOsPermissionsForWebKind(pendingPerm.kind) },
                                    modifier = Modifier.testTag("web_perm_allow_button")
                                ) { Text(novaStrings.sitePermAllow) }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.resolvePendingWebPermission(userAllowed = false, osGranted = false)
                                    },
                                    modifier = Modifier.testTag("web_perm_block_button")
                                ) { Text(novaStrings.sitePermBlock) }
                            }
                        )
                    }

                    if (showSplashOverlay) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(splashAlpha)
                                .background(Color.Black)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { showSplashOverlay = false }
                                .testTag("netra_splash_screen")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_netra_logo),
                                    contentDescription = "Netra Logo",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(112.dp)
                                        .clip(RoundedCornerShape(28.dp))
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = novaStrings.appTitleStandard,
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontSize = 26.sp,
                                        lineHeight = 36.sp,
                                        letterSpacing = if (novaStrings.isHindi) 0.sp else 0.6.sp
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun copyTextToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}

private fun shareUrl(context: Context, title: String, url: String) {
    try {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, url)
        }
        context.startActivity(
            Intent.createChooser(sendIntent, "Share via Netra").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    } catch (_: Exception) {
    }
}
