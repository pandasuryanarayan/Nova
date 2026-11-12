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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ActiveSheet
import com.example.ui.components.AddShortcutDialog
import com.example.ui.components.BackgroundMediaMiniBar
import com.example.ui.components.BlockedAdsSheet
import com.example.ui.components.BookmarksSheet
import com.example.ui.components.BottomBrowserBar
import com.example.ui.components.BrowserMenuSheet
import com.example.ui.components.ClearBrowsingDataDialog
import com.example.ui.components.DevToolsInspectorPanel
import com.example.ui.components.DownloadsManagerSheet
import com.example.ui.components.FindInPageTopBar
import com.example.ui.components.HistorySheet
import com.example.ui.components.NovaHomeContent
import com.example.ui.components.SslSecurityInfoDialog
import com.example.ui.components.TabsSwitcherSheet
import com.example.ui.components.VideoLongPressBottomSheet
import com.example.ui.theme.NovaBrowserTheme
import com.example.viewmodel.BrowserViewModel

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.example.ui.components.TopWebsiteHeaderBar

@Composable
fun NovaBrowserApp(
    viewModel: BrowserViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
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
    val incognitoWaveAnim = remember { Animatable(0f) }

    LaunchedEffect(activeTab.isIncognito) {
        if (activeTab.isIncognito != previousIncognito) {
            previousIncognito = activeTab.isIncognito
            incognitoWaveAnim.snapTo(0.01f)
            incognitoWaveAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 580, easing = FastOutSlowInEasing)
            )
            incognitoWaveAnim.snapTo(0f)
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
            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Top Bar shown whenever a website is opened: Lock + Website Title + 1-Tap Bookmark + Falldown Link Button
                            AnimatedVisibility(
                                visible = !activeTab.isHomePage,
                                enter = expandVertically(animationSpec = tween(160)) + fadeIn(animationSpec = tween(140)),
                                exit = shrinkVertically(animationSpec = tween(150)) + fadeOut(animationSpec = tween(120))
                            ) {
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

                            AnimatedVisibility(
                                visible = uiState.isFindInPageVisible,
                                enter = expandVertically(animationSpec = tween(150)) + fadeIn(animationSpec = tween(120)),
                                exit = shrinkVertically(animationSpec = tween(140)) + fadeOut(animationSpec = tween(120))
                            ) {
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
                        }
                    },
                    bottomBar = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            AnimatedVisibility(
                                visible = uiState.backgroundMedia.isActive,
                                enter = expandVertically(animationSpec = tween(150)) + fadeIn(animationSpec = tween(120)),
                                exit = shrinkVertically(animationSpec = tween(140)) + fadeOut(animationSpec = tween(120))
                            ) {
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
                                onOpenTabsClick = { viewModel.setActiveSheet(ActiveSheet.TABS) },
                                onOpenMenuClick = { viewModel.setActiveSheet(ActiveSheet.MENU) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        Crossfade(
                            targetState = activeTab.isHomePage,
                            animationSpec = tween(durationMillis = 150),
                            label = "home_webview_crossfade",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) { isHome ->
                            if (isHome) {
                                NovaHomeContent(
                                    isIncognito = activeTab.isIncognito,
                                    isAdBlockEnabled = preferences.isAdBlockEnabled,
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

                        // Desktop-Grade Split-Screen DevTools Panel (Elements, Sources, Console, Network ONLY)
                        AnimatedVisibility(visible = uiState.devToolsState.isVisible && !activeTab.isHomePage) {
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
                    }
                }

                // Smooth App-Wide Radial Wave Overlay when Incognito is Toggled ON (Dark Cyan) or OFF (Default Light Orange)
                val waveProgress = incognitoWaveAnim.value
                if (waveProgress > 0f) {
                    val waveColor = if (activeTab.isIncognito) {
                        Color(0xFF00E5FF)
                    } else {
                        Color(0xFFFF8A3D)
                    }
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
            }
        }

        // --- Bottom Sheets & Dialogs ---

        when (uiState.activeSheet) {
            ActiveSheet.VIDEO_LONG_PRESS -> {
                val videoInfo = uiState.detectedVideo
                if (videoInfo != null) {
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
                            viewModel.openNewTab(url = link, isIncognito = activeTab.isIncognito)
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
                        val res = viewModel.downloadHelper.openDownloadedFile(item)
                        res.onFailure { err ->
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
                    onDismiss = viewModel::dismissSheet
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
                    totalAdsBlocked = preferences.totalAdsBlocked,
                    activeTabId = uiState.activeTabId,
                    onToggleAdBlock = viewModel::toggleAdBlock,
                    onCopyAdUrl = { adUrl ->
                        copyTextToClipboard(context, "Blocked Ad URL", adUrl)
                        viewModel.emitToast("Blocked ad URL copied to clipboard")
                    },
                    onClearLog = viewModel::clearBlockedAdsLog,
                    onDismiss = viewModel::dismissSheet
                )
            }

            ActiveSheet.MENU -> {
                BrowserMenuSheet(
                    activeTab = activeTab,
                    isBookmarked = isBookmarked,
                    isAdBlockEnabled = preferences.isAdBlockEnabled,
                    isDarkMode = preferences.isDarkMode,
                    totalAdsBlocked = preferences.totalAdsBlocked,
                    blockedAdsInSessionCount = uiState.blockedAdsLog.size,
                    onToggleBookmark = viewModel::toggleBookmarkForActiveTab,
                    onToggleAdBlock = viewModel::toggleAdBlock,
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
                val certDetails = remember(activeTab.id, activeTab.url, activeTab.isLoading) {
                    viewModel.webViewPool.getSslCertificateDetails(activeTab)
                }
                val siteCookies = remember(activeTab.id, activeTab.url, activeTab.isLoading, cookieRefreshTick) {
                    viewModel.webViewPool.getSiteCookies(activeTab.url)
                }
                SslSecurityInfoDialog(
                    tab = activeTab,
                    certificateDetails = certDetails,
                    siteCookies = siteCookies,
                    isAdBlockEnabled = preferences.isAdBlockEnabled,
                    isDarkMode = preferences.isDarkMode,
                    onToggleAdBlock = viewModel::toggleAdBlock,
                    onToggleDesktopMode = viewModel::toggleDesktopMode,
                    onToggleDarkMode = viewModel::toggleDarkMode,
                    onCopyText = { label, text ->
                        copyTextToClipboard(context, label, text)
                        viewModel.emitToast("$label copied to clipboard")
                    },
                    onClearSiteCookies = {
                        viewModel.webViewPool.clearSiteCookies(activeTab.url)
                        cookieRefreshTick++
                        viewModel.emitToast("Site cookies cleared")
                    },
                    onDismiss = viewModel::dismissSheet
                )
            }

            ActiveSheet.NONE -> Unit
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
            Intent.createChooser(sendIntent, "Share via Nova Browser").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    } catch (_: Exception) {
    }
}
