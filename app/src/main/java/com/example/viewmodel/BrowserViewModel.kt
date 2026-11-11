package com.example.viewmodel

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.view.View
import android.webkit.WebChromeClient
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.browser.BrowserUrlUtils
import com.example.browser.NovaDownloadHelper
import com.example.browser.NovaWebViewPool
import com.example.data.ActiveSheet
import com.example.data.BackgroundMediaState
import com.example.data.BlockedAdEntry
import com.example.data.BookmarkEntity
import com.example.data.BrowserPreferences
import com.example.data.BrowserRepository
import com.example.data.BrowserTab
import com.example.data.DetectedVideoInfo
import com.example.data.DetectedVideoQuality
import com.example.data.DevToolsConsoleEntry
import com.example.data.DevToolsNetworkEntry
import com.example.data.DevToolsSourceFile
import com.example.data.DevToolsState
import com.example.data.DevToolsTab
import com.example.data.DomSelectedElementDetail
import com.example.data.DomTreeNodeItem
import com.example.data.DownloadItemEntity
import com.example.data.HistoryEntity
import com.example.data.NOVA_HOME_URL
import com.example.data.NovaDatabase
import com.example.data.QuickShortcutEntity
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

data class BrowserUiState(
    val tabs: List<BrowserTab> = listOf(BrowserTab(title = "Nova Home", url = NOVA_HOME_URL)),
    val activeTabId: String = "",
    val activeSheet: ActiveSheet = ActiveSheet.NONE,
    val detectedVideo: DetectedVideoInfo? = null,
    val isFindInPageVisible: Boolean = false,
    val findQuery: String = "",
    val findActiveOrdinal: Int = 0,
    val findTotalMatches: Int = 0,
    val fullscreenCustomView: View? = null,
    val fullscreenCallback: WebChromeClient.CustomViewCallback? = null,
    val backgroundMedia: BackgroundMediaState = BackgroundMediaState(),
    val devToolsState: DevToolsState = DevToolsState(),
    val blockedAdsLog: List<BlockedAdEntry> = emptyList()
) {
    val activeTab: BrowserTab
        get() = tabs.find { it.id == activeTabId } ?: tabs.first()
}

class BrowserViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NovaDatabase.getInstance(application)
    private val repository = BrowserRepository(application, database.browserDao())
    val downloadHelper = NovaDownloadHelper(application, repository, viewModelScope)

    private val sourceHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val initialTab = BrowserTab(title = "Nova Home", url = NOVA_HOME_URL)

    private val _uiState = MutableStateFlow(
        BrowserUiState(
            tabs = listOf(initialTab),
            activeTabId = initialTab.id
        )
    )
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    val preferences: StateFlow<BrowserPreferences> = repository.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BrowserPreferences()
    )

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarksFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val history: StateFlow<List<HistoryEntity>> = repository.historyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val shortcuts: StateFlow<List<QuickShortcutEntity>> = repository.shortcutsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NovaDatabase.defaultShortcuts()
    )

    val downloads: StateFlow<List<DownloadItemEntity>> = repository.downloadsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toastEvents = _toastEvents.asSharedFlow()

    private var backgroundMediaPlayer: MediaPlayer? = null

    val webViewPool = NovaWebViewPool(
        context = application.applicationContext,
        onTabStateUpdated = { tabId, url, title, isLoading, progress, canGoBack, canGoForward, isSecureSsl, sslSummary ->
            updateTabById(tabId) { current ->
                current.copy(
                    url = url ?: current.url,
                    title = title ?: current.title,
                    isLoading = isLoading ?: current.isLoading,
                    progress = progress ?: current.progress,
                    canGoBack = canGoBack ?: current.canGoBack,
                    canGoForward = canGoForward ?: current.canGoForward,
                    isSecureSsl = isSecureSsl ?: current.isSecureSsl,
                    sslSummary = sslSummary ?: current.sslSummary
                )
            }
        },
        onHistoryVisited = { tabId, title, url ->
            val tab = _uiState.value.tabs.find { it.id == tabId }
            if (tab != null && !tab.isIncognito && !url.startsWith("nova://")) {
                viewModelScope.launch {
                    repository.recordHistory(title, url, BrowserUrlUtils.extractDomain(url))
                }
            }
        },
        onAdBlocked = { entry ->
            updateTabById(entry.tabId) { it.copy(adsBlockedInTab = it.adsBlockedInTab + 1) }
            _uiState.update { state ->
                val recentMatch = state.blockedAdsLog.take(5).any {
                    it.adUrl == entry.adUrl && it.tabId == entry.tabId
                }
                if (recentMatch) {
                    state
                } else {
                    state.copy(
                        blockedAdsLog = (listOf(entry) + state.blockedAdsLog).take(250)
                    )
                }
            }
            viewModelScope.launch { repository.incrementAdsBlocked() }
        },
        onVideoLongPressed = { detectedInfo ->
            handleVideoLongPressed(detectedInfo)
        },
        onDirectDownloadRequested = { url, _, _, mimeType, contentLength ->
            val active = _uiState.value.activeTab
            val isStreaming = BrowserUrlUtils.isProtectedOrStreamingUrl(url, mimeType)
            val info = DetectedVideoInfo(
                pageTitle = active.title,
                pageUrl = active.url,
                domain = BrowserUrlUtils.extractDomain(active.url.ifBlank { url }),
                primaryUrl = url,
                qualities = listOf(
                    DetectedVideoQuality(
                        url = url,
                        label = "Direct MP4 File",
                        mimeType = mimeType ?: "video/mp4",
                        isDirectMp4 = !isStreaming,
                        isStreaming = isStreaming,
                        estimatedBytes = contentLength,
                        isProbing = contentLength <= 0L
                    )
                )
            )
            handleVideoLongPressed(info)
        },
        onEnterFullscreenVideo = { view, callback ->
            _uiState.update {
                it.copy(fullscreenCustomView = view, fullscreenCallback = callback)
            }
        },
        onExitFullscreenVideo = {
            _uiState.value.fullscreenCallback?.onCustomViewHidden()
            _uiState.update {
                it.copy(fullscreenCustomView = null, fullscreenCallback = null)
            }
        },
        onFindResultUpdated = { activeOrdinal, totalMatches ->
            _uiState.update {
                it.copy(
                    findActiveOrdinal = if (totalMatches > 0) activeOrdinal + 1 else 0,
                    findTotalMatches = totalMatches
                )
            }
        },
        onDevToolsElementSelectedJson = { tabId, jsonStr ->
            if (tabId == _uiState.value.activeTabId) {
                handleElementSelectedFromJs(jsonStr)
            }
        },
        onDevToolsConsoleLogJson = { tabId, jsonStr ->
            if (tabId == _uiState.value.activeTabId) {
                handleConsoleLogFromJs(jsonStr)
            }
        },
        onDevToolsNetworkEntryJson = { tabId, jsonStr ->
            if (tabId == _uiState.value.activeTabId) {
                handleNetworkEntryFromJs(jsonStr)
            }
        },
        onPageFinishedForDevTools = { tabId ->
            if (tabId == _uiState.value.activeTabId && _uiState.value.devToolsState.isVisible) {
                refreshDevTools()
            }
        },
        onVideoSizeProbedInWebView = { videoUrl, contentLength, contentType ->
            updateDetectedVideoSize(videoUrl, contentLength, contentType)
        },
        onWebViewDownloadChunk = { downloadId, base64Chunk, loadedBytes, totalBytes ->
            downloadHelper.onWebViewDownloadChunk(downloadId, base64Chunk, loadedBytes, totalBytes)
        },
        onWebViewDownloadFinished = { downloadId, totalLoadedBytes ->
            downloadHelper.onWebViewDownloadFinished(downloadId, totalLoadedBytes)
        },
        onWebViewDownloadError = { downloadId, errorMessage ->
            downloadHelper.onWebViewDownloadError(downloadId, errorMessage)
        },
        isAdBlockEnabledProvider = { preferences.value.isAdBlockEnabled },
        isDarkModeEnabledProvider = { preferences.value.isDarkMode }
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultShortcutsSeeded()
        }
        downloadHelper.webViewStreamTrigger = { downloadId, videoUrl ->
            val wv = webViewPool.getWebView(_uiState.value.activeTab.id)
            if (wv != null) {
                val qUrl = JSONObject.quote(videoUrl)
                wv.post {
                    wv.evaluateJavascript(
                        "(function(){ if(window.__novaDownloadVideoViaWebView) { window.__novaDownloadVideoViaWebView($downloadId, $qUrl); } else if(window.NovaVideoBridge && window.NovaVideoBridge.onWebViewDownloadError) { window.NovaVideoBridge.onWebViewDownloadError($downloadId, 'WebView stream bridge unavailable'); } })();",
                        null
                    )
                }
                true
            } else {
                false
            }
        }
    }

    private fun updateTabById(tabId: String, transform: (BrowserTab) -> BrowserTab) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { tab ->
                    if (tab.id == tabId) transform(tab) else tab
                }
            )
        }
    }

    fun emitToast(message: String) {
        _toastEvents.tryEmit(message)
    }

    fun setActiveSheet(sheet: ActiveSheet) {
        _uiState.update { it.copy(activeSheet = sheet) }
    }

    fun dismissSheet() {
        _uiState.update { it.copy(activeSheet = ActiveSheet.NONE) }
    }

    fun clearBlockedAdsLog() {
        _uiState.update { it.copy(blockedAdsLog = emptyList()) }
        emitToast("Cleared blocked ads log")
    }

    // --- Desktop-Style Browser Inspect (DevTools: Elements, Sources, Console, Network ONLY) ---

    fun toggleDevTools() {
        val active = _uiState.value.activeTab
        if (active.isHomePage) {
            loadUrlOrSearch("https://www.w3schools.com/html/html5_video.asp")
        }
        val nextVisible = !_uiState.value.devToolsState.isVisible
        _uiState.update { state ->
            state.copy(
                activeSheet = ActiveSheet.NONE,
                devToolsState = state.devToolsState.copy(
                    isVisible = nextVisible,
                    isInspectElementPickerActive = if (!nextVisible) false else state.devToolsState.isInspectElementPickerActive
                )
            )
        }
        if (nextVisible) {
            refreshDevTools()
        } else {
            disableInspectPickerInWebView()
        }
    }

    fun closeDevTools() {
        disableInspectPickerInWebView()
        _uiState.update { state ->
            state.copy(
                devToolsState = state.devToolsState.copy(
                    isVisible = false,
                    isInspectElementPickerActive = false
                )
            )
        }
    }

    fun selectDevToolsTab(tab: DevToolsTab) {
        _uiState.update { state ->
            state.copy(
                devToolsState = state.devToolsState.copy(selectedTab = tab)
            )
        }
        refreshDevTools()
    }

    fun toggleInspectElementPicker() {
        val nextMode = !_uiState.value.devToolsState.isInspectElementPickerActive
        _uiState.update { state ->
            state.copy(
                devToolsState = state.devToolsState.copy(
                    isInspectElementPickerActive = nextMode,
                    selectedTab = DevToolsTab.ELEMENTS
                )
            )
        }
        val wv = webViewPool.getWebView(_uiState.value.activeTab.id) ?: return
        wv.evaluateJavascript(
            "(function(){ if(window.__novaDevToolsSetInspectMode) window.__novaDevToolsSetInspectMode($nextMode); })();",
            null
        )
        if (nextMode) {
            emitToast("Tap any element on the webpage above to inspect it")
        }
    }

    private fun disableInspectPickerInWebView() {
        val wv = webViewPool.getWebView(_uiState.value.activeTab.id) ?: return
        wv.evaluateJavascript(
            "(function(){ if(window.__novaDevToolsSetInspectMode) window.__novaDevToolsSetInspectMode(false); })();",
            null
        )
    }

    fun refreshDevTools() {
        val wv = webViewPool.getWebView(_uiState.value.activeTab.id) ?: return
        wv.evaluateJavascript(
            "(function(){ return window.__novaDevToolsRefreshAll ? window.__novaDevToolsRefreshAll() : (window.__novaDevToolsGetDomTree ? window.__novaDevToolsGetDomTree() : '[]'); })();"
        ) { rawJson ->
            val unquoted = unquoteEvaluateJsResult(rawJson)
            val parsedNodes = parseDomTreeJson(unquoted)
            _uiState.update { state ->
                state.copy(
                    devToolsState = state.devToolsState.copy(domTree = parsedNodes)
                )
            }
            if (_uiState.value.devToolsState.selectedElement == null && parsedNodes.isNotEmpty()) {
                val bodyOrFirst = parsedNodes.firstOrNull { it.tagName == "body" } ?: parsedNodes.first()
                selectDomNode(bodyOrFirst.nodeId)
            }
        }

        wv.evaluateJavascript(
            "(function(){ return window.__novaDevToolsGetSources ? window.__novaDevToolsGetSources() : '[]'; })();"
        ) { rawJson ->
            val unquoted = unquoteEvaluateJsResult(rawJson)
            val parsedSources = parseSourcesJson(unquoted)
            _uiState.update { state ->
                val currentSrcId = state.devToolsState.selectedSourceId
                val nextSrcId = if (parsedSources.any { it.id == currentSrcId }) {
                    currentSrcId
                } else {
                    parsedSources.firstOrNull()?.id ?: ""
                }
                state.copy(
                    devToolsState = state.devToolsState.copy(
                        sources = parsedSources,
                        selectedSourceId = nextSrcId
                    )
                )
            }
        }
    }

    fun selectDomNode(nodeId: String) {
        val wv = webViewPool.getWebView(_uiState.value.activeTab.id) ?: return
        val qNodeId = JSONObject.quote(nodeId)
        wv.evaluateJavascript(
            "(function(){ return window.__novaDevToolsSelectNodeById ? window.__novaDevToolsSelectNodeById($qNodeId) : ''; })();"
        ) { rawJson ->
            val unquoted = unquoteEvaluateJsResult(rawJson)
            parseSelectedElementJson(unquoted)?.let { detail ->
                _uiState.update { state ->
                    state.copy(
                        devToolsState = state.devToolsState.copy(selectedElement = detail)
                    )
                }
            }
        }
    }

    fun applyElementEdit(
        nodeId: String,
        inlineStyle: String,
        textContent: String,
        attrName: String,
        attrValue: String
    ) {
        val wv = webViewPool.getWebView(_uiState.value.activeTab.id) ?: return
        val qNodeId = JSONObject.quote(nodeId)
        val qStyle = JSONObject.quote(inlineStyle)
        val qText = JSONObject.quote(textContent)
        val qAttrName = JSONObject.quote(attrName)
        val qAttrVal = JSONObject.quote(attrValue)

        wv.evaluateJavascript(
            "(function(){ return window.__novaDevToolsApplyElementEdit ? window.__novaDevToolsApplyElementEdit($qNodeId, $qStyle, $qText, $qAttrName, $qAttrVal) : ''; })();"
        ) { rawJson ->
            val unquoted = unquoteEvaluateJsResult(rawJson)
            parseSelectedElementJson(unquoted)?.let { updatedDetail ->
                _uiState.update { state ->
                    state.copy(
                        devToolsState = state.devToolsState.copy(selectedElement = updatedDetail)
                    )
                }
                emitToast("Applied live DOM / CSS changes to <${updatedDetail.tagName}>")
                refreshDevTools()
            }
        }
    }

    fun deleteDomNode(nodeId: String) {
        val wv = webViewPool.getWebView(_uiState.value.activeTab.id) ?: return
        val qNodeId = JSONObject.quote(nodeId)
        wv.evaluateJavascript(
            "(function(){ return window.__novaDevToolsDeleteNode ? window.__novaDevToolsDeleteNode($qNodeId) : false; })();"
        ) {
            emitToast("Element removed from DOM")
            _uiState.update { state ->
                state.copy(devToolsState = state.devToolsState.copy(selectedElement = null))
            }
            refreshDevTools()
        }
    }

    fun selectSourceFile(sourceId: String) {
        _uiState.update { state ->
            state.copy(
                devToolsState = state.devToolsState.copy(selectedSourceId = sourceId)
            )
        }
    }

    fun fetchRemoteSource(sourceFile: DevToolsSourceFile) {
        viewModelScope.launch(Dispatchers.IO) {
            val content = try {
                val req = Request.Builder()
                    .url(sourceFile.url)
                    .header("User-Agent", "Mozilla/5.0 (Android 14; Mobile)")
                    .get()
                    .build()
                sourceHttpClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        resp.body?.string()?.take(45000) ?: "// Empty response"
                    } else {
                        "// HTTP ${resp.code} ${resp.message}"
                    }
                }
            } catch (e: Exception) {
                "// Failed to load remote source: ${e.localizedMessage ?: "Network error"}"
            }
            _uiState.update { state ->
                val updatedFiles = state.devToolsState.sources.map { file ->
                    if (file.id == sourceFile.id) {
                        file.copy(content = content, sizeBytes = content.length)
                    } else {
                        file
                    }
                }
                state.copy(
                    devToolsState = state.devToolsState.copy(sources = updatedFiles)
                )
            }
        }
    }

    fun executeConsoleJs(expression: String) {
        val trimmed = expression.trim()
        if (trimmed.isBlank()) return
        val wv = webViewPool.getWebView(_uiState.value.activeTab.id) ?: return

        val commandEntry = DevToolsConsoleEntry(
            level = "CMD",
            message = "> $trimmed",
            source = "Console REPL"
        )
        _uiState.update { state ->
            state.copy(
                devToolsState = state.devToolsState.copy(
                    consoleLogs = state.devToolsState.consoleLogs + commandEntry
                )
            )
        }

        val qExpr = JSONObject.quote(trimmed)
        wv.evaluateJavascript(
            """
            (function(){
              try {
                var r = eval($qExpr);
                var str = (typeof r === 'object' && r !== null) ? JSON.stringify(r, null, 2) : String(r);
                return JSON.stringify({ ok: true, result: str });
              } catch (e) {
                return JSON.stringify({ ok: false, result: (e && e.message) ? e.message : String(e) });
              }
            })();
            """.trimIndent()
        ) { rawJson ->
            val unquoted = unquoteEvaluateJsResult(rawJson)
            try {
                val obj = JSONObject(unquoted)
                val ok = obj.optBoolean("ok", true)
                val resultText = obj.optString("result", "undefined")
                val outputEntry = DevToolsConsoleEntry(
                    level = if (ok) "RESULT" else "ERROR",
                    message = if (ok) "<- $resultText" else "Uncaught $resultText",
                    source = "eval"
                )
                _uiState.update { state ->
                    state.copy(
                        devToolsState = state.devToolsState.copy(
                            consoleLogs = state.devToolsState.consoleLogs + outputEntry
                        )
                    )
                }
                refreshDevTools()
            } catch (_: Exception) {
            }
        }
    }

    fun clearConsoleLogs() {
        _uiState.update { state ->
            state.copy(devToolsState = state.devToolsState.copy(consoleLogs = emptyList()))
        }
    }

    fun clearNetworkEntries() {
        _uiState.update { state ->
            state.copy(devToolsState = state.devToolsState.copy(networkEntries = emptyList()))
        }
    }

    private fun handleElementSelectedFromJs(jsonStr: String) {
        parseSelectedElementJson(jsonStr)?.let { detail ->
            _uiState.update { state ->
                state.copy(
                    devToolsState = state.devToolsState.copy(
                        isVisible = true,
                        selectedTab = DevToolsTab.ELEMENTS,
                        isInspectElementPickerActive = false,
                        selectedElement = detail
                    )
                )
            }
        }
    }

    private fun handleConsoleLogFromJs(jsonStr: String) {
        try {
            val obj = JSONObject(jsonStr)
            val level = obj.optString("level", "LOG")
            val message = obj.optString("message", "")
            if (message.isBlank()) return
            val source = obj.optString("source", "console")
            val ts = obj.optLong("timestamp", System.currentTimeMillis())
            val entry = DevToolsConsoleEntry(
                level = level,
                message = message,
                source = source,
                timestamp = ts
            )
            _uiState.update { state ->
                val currentLogs = state.devToolsState.consoleLogs
                if (currentLogs.lastOrNull()?.let { it.message == entry.message && it.level == entry.level } == true) {
                    state
                } else {
                    state.copy(
                        devToolsState = state.devToolsState.copy(
                            consoleLogs = (currentLogs + entry).takeLast(200)
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun handleNetworkEntryFromJs(jsonStr: String) {
        try {
            val obj = JSONObject(jsonStr)
            val entry = parseSingleNetworkJson(obj) ?: return
            _uiState.update { state ->
                val updated = (listOf(entry) + state.devToolsState.networkEntries)
                    .distinctBy { "${it.method}_${it.url}_${it.status}" }
                    .take(150)
                state.copy(
                    devToolsState = state.devToolsState.copy(networkEntries = updated)
                )
            }
        } catch (_: Exception) {
        }
    }

    private fun unquoteEvaluateJsResult(raw: String?): String {
        if (raw.isNullOrBlank() || raw == "null") return ""
        return try {
            if (raw.startsWith("\"") && raw.endsWith("\"")) {
                JSONTokener(raw).nextValue().toString()
            } else {
                raw
            }
        } catch (_: Exception) {
            raw
        }
    }

    private fun parseDomTreeJson(jsonStr: String): List<DomTreeNodeItem> {
        if (jsonStr.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<DomTreeNodeItem>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                list.add(
                    DomTreeNodeItem(
                        nodeId = o.optString("nodeId", "node_$i"),
                        depth = o.optInt("depth", 0),
                        tagName = o.optString("tagName", "div"),
                        selector = o.optString("selector", "div"),
                        childCount = o.optInt("childCount", 0),
                        textPreview = o.optString("textPreview", "")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseSelectedElementJson(jsonStr: String): DomSelectedElementDetail? {
        if (jsonStr.isBlank()) return null
        return try {
            val o = JSONObject(jsonStr)
            val nodeId = o.optString("nodeId", "")
            if (nodeId.isBlank()) return null

            val attrs = mutableListOf<Pair<String, String>>()
            val attrsArr = o.optJSONArray("attributes")
            if (attrsArr != null) {
                for (i in 0 until attrsArr.length()) {
                    val a = attrsArr.optJSONObject(i) ?: continue
                    val name = a.optString("name", "")
                    if (name.isNotBlank()) {
                        attrs.add(name to a.optString("value", ""))
                    }
                }
            }

            val computed = mutableListOf<Pair<String, String>>()
            val compArr = o.optJSONArray("computedStyles")
            if (compArr != null) {
                for (i in 0 until compArr.length()) {
                    val c = compArr.optJSONObject(i) ?: continue
                    val prop = c.optString("property", "")
                    if (prop.isNotBlank()) {
                        computed.add(prop to c.optString("value", ""))
                    }
                }
            }

            val boxObj = o.optJSONObject("boxMetrics")
            DomSelectedElementDetail(
                nodeId = nodeId,
                tagName = o.optString("tagName", "div"),
                selector = o.optString("selector", "div"),
                breadcrumb = o.optString("breadcrumb", "html > body"),
                id = o.optString("id", ""),
                className = o.optString("className", ""),
                inlineStyle = o.optString("inlineStyle", ""),
                innerText = o.optString("innerText", ""),
                outerHtml = o.optString("outerHtml", ""),
                widthPx = boxObj?.optInt("width", 0) ?: 0,
                heightPx = boxObj?.optInt("height", 0) ?: 0,
                topPx = boxObj?.optInt("top", 0) ?: 0,
                leftPx = boxObj?.optInt("left", 0) ?: 0,
                attributes = attrs,
                computedStyles = computed
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseSourcesJson(jsonStr: String): List<DevToolsSourceFile> {
        if (jsonStr.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<DevToolsSourceFile>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                list.add(
                    DevToolsSourceFile(
                        id = o.optString("id", "src_$i"),
                        name = o.optString("name", "Source #$i"),
                        url = o.optString("url", ""),
                        type = o.optString("type", "HTML"),
                        sizeBytes = o.optInt("sizeBytes", 0),
                        content = o.optString("content", "")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseSingleNetworkJson(o: JSONObject): DevToolsNetworkEntry? {
        val url = o.optString("url", "")
        if (url.isBlank()) return null
        return DevToolsNetworkEntry(
            url = url,
            method = o.optString("method", "GET"),
            status = o.optInt("status", 200),
            initiatorType = o.optString("initiatorType", "resource"),
            mimeType = o.optString("mimeType", "application/octet-stream"),
            sizeBytes = o.optLong("sizeBytes", 0L),
            durationMs = o.optLong("durationMs", 0L),
            responsePreview = o.optString("responsePreview", ""),
            timestamp = o.optLong("timestamp", System.currentTimeMillis())
        )
    }

    // --- Tab Management ---

    fun openNewTab(url: String = NOVA_HOME_URL, isIncognito: Boolean = false) {
        val defaultZoom = preferences.value.defaultTextZoom
        val normalizedUrl = if (url == NOVA_HOME_URL) NOVA_HOME_URL else BrowserUrlUtils.normalizeInputToUrl(url)
        val newTab = BrowserTab(
            url = normalizedUrl,
            title = if (normalizedUrl == NOVA_HOME_URL) {
                if (isIncognito) "Private Home" else "Nova Home"
            } else {
                BrowserUrlUtils.extractDomain(normalizedUrl)
            },
            isIncognito = isIncognito,
            textZoom = defaultZoom
        )
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs + newTab,
                activeTabId = newTab.id,
                activeSheet = ActiveSheet.NONE
            )
        }
        if (!newTab.isHomePage) {
            webViewPool.loadUrl(newTab, normalizedUrl)
        }
    }

    fun switchTab(tabId: String) {
        if (_uiState.value.tabs.any { it.id == tabId }) {
            _uiState.update {
                it.copy(
                    activeTabId = tabId,
                    activeSheet = ActiveSheet.NONE,
                    isFindInPageVisible = false
                )
            }
            if (_uiState.value.devToolsState.isVisible) {
                refreshDevTools()
            }
        }
    }

    fun duplicateTab(tabId: String) {
        val sourceTab = _uiState.value.tabs.find { it.id == tabId } ?: return
        val duplicated = sourceTab.copy(
            id = java.util.UUID.randomUUID().toString(),
            title = "${sourceTab.title} (Copy)",
            canGoBack = false,
            canGoForward = false
        )
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs + duplicated,
                activeTabId = duplicated.id,
                activeSheet = ActiveSheet.NONE
            )
        }
        if (!duplicated.isHomePage) {
            webViewPool.loadUrl(duplicated, duplicated.url)
        }
        emitToast("Tab duplicated")
    }

    fun closeTab(tabId: String) {
        webViewPool.removeAndDestroyTab(tabId)
        _uiState.update { state ->
            val remaining = state.tabs.filterNot { it.id == tabId }
            if (remaining.isEmpty()) {
                val fresh = BrowserTab(
                    title = "Nova Home",
                    url = NOVA_HOME_URL,
                    textZoom = preferences.value.defaultTextZoom
                )
                state.copy(tabs = listOf(fresh), activeTabId = fresh.id)
            } else {
                val nextActiveId = if (state.activeTabId == tabId) {
                    remaining.last().id
                } else {
                    state.activeTabId
                }
                state.copy(tabs = remaining, activeTabId = nextActiveId)
            }
        }
    }

    fun toggleIncognitoMode() {
        val current = _uiState.value.activeTab
        if (current.isIncognito) {
            val normalTab = _uiState.value.tabs.firstOrNull { !it.isIncognito }
            if (normalTab != null) {
                switchTab(normalTab.id)
            } else {
                openNewTab(url = NOVA_HOME_URL, isIncognito = false)
            }
            emitToast("Switched to Standard Browsing")
        } else {
            openNewTab(url = NOVA_HOME_URL, isIncognito = true)
            emitToast("Incognito Mode Activated")
        }
    }

    // --- Navigation & Address Bar ---

    fun loadUrlOrSearch(rawInput: String) {
        val targetUrl = BrowserUrlUtils.normalizeInputToUrl(rawInput)
        val active = _uiState.value.activeTab
        updateTabById(active.id) {
            it.copy(
                url = targetUrl,
                title = if (targetUrl == NOVA_HOME_URL) "Nova Home" else BrowserUrlUtils.extractDomain(targetUrl),
                isLoading = targetUrl != NOVA_HOME_URL,
                progress = if (targetUrl == NOVA_HOME_URL) 0 else 15
            )
        }
        webViewPool.loadUrl(active.copy(url = targetUrl), targetUrl)
    }

    fun reloadOrStop() {
        val active = _uiState.value.activeTab
        if (active.isHomePage) return
        val wv = webViewPool.getWebView(active.id) ?: return
        if (active.isLoading) {
            wv.stopLoading()
            updateTabById(active.id) { it.copy(isLoading = false, progress = 100) }
        } else {
            wv.reload()
        }
    }

    fun goBack(): Boolean {
        val active = _uiState.value.activeTab
        if (_uiState.value.fullscreenCustomView != null) {
            exitFullscreenVideo()
            return true
        }
        if (_uiState.value.isFindInPageVisible) {
            closeFindInPage()
            return true
        }
        if (_uiState.value.activeSheet != ActiveSheet.NONE) {
            dismissSheet()
            return true
        }
        if (_uiState.value.devToolsState.isVisible) {
            closeDevTools()
            return true
        }
        if (!active.isHomePage) {
            val wv = webViewPool.getWebView(active.id)
            if (wv != null && wv.canGoBack()) {
                wv.goBack()
                return true
            } else {
                goHome()
                return true
            }
        }
        return false
    }

    fun goForward() {
        val active = _uiState.value.activeTab
        val wv = webViewPool.getWebView(active.id) ?: return
        if (wv.canGoForward()) {
            wv.goForward()
        }
    }

    fun goHome() {
        val active = _uiState.value.activeTab
        webViewPool.getWebView(active.id)?.onPause()
        closeDevTools()
        updateTabById(active.id) {
            it.copy(
                url = NOVA_HOME_URL,
                title = if (it.isIncognito) "Private Home" else "Nova Home",
                isLoading = false,
                progress = 0
            )
        }
    }

    fun exitFullscreenVideo() {
        _uiState.value.fullscreenCallback?.onCustomViewHidden()
        _uiState.update {
            it.copy(fullscreenCustomView = null, fullscreenCallback = null)
        }
    }

    // --- Browser Toggles & Tools ---

    fun toggleDesktopMode() {
        val active = _uiState.value.activeTab
        val nextDesktop = !active.isDesktopMode
        val convertedUrl = if (active.isHomePage) {
            active.url
        } else if (nextDesktop) {
            BrowserUrlUtils.toDesktopUrl(active.url)
        } else {
            BrowserUrlUtils.toMobileUrl(active.url)
        }
        val updated = active.copy(
            isDesktopMode = nextDesktop,
            url = convertedUrl
        )
        updateTabById(active.id) { updated }
        _uiState.update { it.copy(activeSheet = ActiveSheet.NONE) }
        webViewPool.reloadForDesktopModeChange(updated)
        emitToast(
            if (nextDesktop) {
                "Desktop Site Mode Enabled (1280px PC Layout)"
            } else {
                "Mobile Site Mode Enabled"
            }
        )
    }

    fun toggleAdBlock() {
        val next = !preferences.value.isAdBlockEnabled
        viewModelScope.launch {
            repository.setAdBlockEnabled(next)
            emitToast(if (next) "Ad-Block Shield Enabled" else "Ad-Block Shield Paused")
        }
    }

    fun toggleDarkMode() {
        val next = !preferences.value.isDarkMode
        viewModelScope.launch {
            repository.setDarkMode(next)
        }
    }

    fun updateTextZoom(newZoom: Int) {
        val clamped = newZoom.coerceIn(50, 200)
        val active = _uiState.value.activeTab
        val updated = active.copy(textZoom = clamped)
        updateTabById(active.id) { updated }
        webViewPool.getWebView(active.id)?.let { wv ->
            webViewPool.applyTabSettings(wv, updated)
        }
        viewModelScope.launch {
            repository.setDefaultTextZoom(clamped)
        }
    }

    fun openFindInPage() {
        _uiState.update {
            it.copy(
                activeSheet = ActiveSheet.NONE,
                isFindInPageVisible = true,
                findQuery = "",
                findActiveOrdinal = 0,
                findTotalMatches = 0
            )
        }
    }

    fun updateFindQuery(query: String) {
        _uiState.update { it.copy(findQuery = query) }
        val wv = webViewPool.getWebView(_uiState.value.activeTab.id) ?: return
        if (query.isBlank()) {
            wv.clearMatches()
            _uiState.update { it.copy(findActiveOrdinal = 0, findTotalMatches = 0) }
        } else {
            wv.findAllAsync(query)
        }
    }

    fun findNext(forward: Boolean) {
        val wv = webViewPool.getWebView(_uiState.value.activeTab.id) ?: return
        wv.findNext(forward)
    }

    fun closeFindInPage() {
        webViewPool.getWebView(_uiState.value.activeTab.id)?.clearMatches()
        _uiState.update {
            it.copy(
                isFindInPageVisible = false,
                findQuery = "",
                findActiveOrdinal = 0,
                findTotalMatches = 0
            )
        }
    }

    // --- Bookmarks, History & Shortcuts ---

    fun toggleBookmarkForActiveTab() {
        val active = _uiState.value.activeTab
        if (active.isHomePage) return
        val existing = bookmarks.value.any { it.url == active.url }
        viewModelScope.launch {
            if (existing) {
                repository.removeBookmarkByUrl(active.url)
                emitToast("Removed from Bookmarks")
            } else {
                repository.addBookmark(
                    title = active.title.ifBlank { BrowserUrlUtils.extractDomain(active.url) },
                    url = active.url,
                    domain = BrowserUrlUtils.extractDomain(active.url)
                )
                emitToast("Saved to Bookmarks")
            }
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch { repository.removeBookmarkById(id) }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch { repository.deleteHistoryItem(id) }
    }

    fun clearBrowsingData(
        clearHistory: Boolean,
        clearCache: Boolean,
        clearCookies: Boolean
    ) {
        viewModelScope.launch {
            if (clearHistory) {
                repository.clearAllHistory()
            }
            webViewPool.clearAllBrowsingData(clearCache = clearCache, clearCookies = clearCookies)
            _uiState.update { it.copy(activeSheet = ActiveSheet.NONE) }
            emitToast("Browsing data cleared")
        }
    }

    fun addQuickShortcut(title: String, rawUrl: String) {
        val normalized = BrowserUrlUtils.normalizeInputToUrl(rawUrl)
        val domain = BrowserUrlUtils.extractDomain(normalized)
        val badge = title.trim().take(3).uppercase()
        viewModelScope.launch {
            repository.addShortcut(
                title = title.ifBlank { domain },
                url = normalized,
                subtitle = domain,
                badgeText = badge.ifBlank { "WEB" },
                accentColorHex = 0xFF00E5FF
            )
            _uiState.update { it.copy(activeSheet = ActiveSheet.NONE) }
            emitToast("Added $title to Quick Shortcuts")
        }
    }

    fun deleteQuickShortcut(id: Long) {
        viewModelScope.launch { repository.deleteShortcut(id) }
    }

    // --- Long-Press Video Detection, Header Probing & Direct MP4 Download ---

    private fun updateDetectedVideoSize(
        videoUrl: String,
        contentLength: Long,
        contentType: String?
    ) {
        if (contentLength <= 0L) return
        _uiState.update { currentState ->
            val currentVideo = currentState.detectedVideo ?: return@update currentState
            val updatedQualities = currentVideo.qualities.map { qItem ->
                if (qItem.url == videoUrl) {
                    qItem.copy(
                        estimatedBytes = maxOf(qItem.estimatedBytes, contentLength),
                        probedContentType = contentType?.takeIf { it.isNotBlank() } ?: qItem.probedContentType ?: qItem.mimeType,
                        isProbing = false,
                        probeError = null
                    )
                } else {
                    qItem
                }
            }
            currentState.copy(
                detectedVideo = currentVideo.copy(qualities = updatedQualities)
            )
        }
    }

    fun handleVideoLongPressed(info: DetectedVideoInfo) {
        val allStreaming = info.qualities.isNotEmpty() && info.qualities.all {
            it.isStreaming || BrowserUrlUtils.isProtectedOrStreamingUrl(it.url, it.mimeType)
        }
        if (allStreaming || BrowserUrlUtils.isProtectedOrStreamingUrl(info.primaryUrl)) {
            emitToast("This video is streaming / protected and cannot be downloaded")
        }

        _uiState.update {
            it.copy(
                detectedVideo = info,
                activeSheet = ActiveSheet.VIDEO_LONG_PRESS
            )
        }

        val activeTab = _uiState.value.activeTab
        val userAgent = webViewPool.getEffectiveUserAgent(activeTab)
        val wv = webViewPool.getWebView(activeTab.id)

        info.qualities.forEachIndexed { index, quality ->
            if (wv != null && !quality.isStreaming) {
                val qUrl = JSONObject.quote(quality.url)
                wv.post {
                    wv.evaluateJavascript(
                        "(function(){ if(window.__novaProbeVideoSizeInWebView) window.__novaProbeVideoSizeInWebView($qUrl); })();",
                        null
                    )
                }
            }
            viewModelScope.launch {
                val probe = downloadHelper.probeVideoHeaders(
                    videoUrl = quality.url,
                    pageUrl = info.pageUrl.ifBlank { activeTab.url },
                    userAgent = userAgent
                )
                _uiState.update { currentState ->
                    val currentVideo = currentState.detectedVideo ?: return@update currentState
                    if (currentVideo.primaryUrl != info.primaryUrl) return@update currentState

                    val updatedQualities = currentVideo.qualities.mapIndexed { qIdx, qItem ->
                        if (qIdx == index) {
                            val bestSize = if (probe.contentLength > 0L) probe.contentLength else qItem.estimatedBytes
                            qItem.copy(
                                estimatedBytes = bestSize,
                                probedContentType = probe.contentType ?: qItem.probedContentType ?: qItem.mimeType,
                                isDirectMp4 = probe.isDirectMp4,
                                isStreaming = probe.isStreamingOrProtected,
                                isProbing = false,
                                probeError = if (bestSize > 0L) null else probe.error
                            )
                        } else {
                            qItem
                        }
                    }
                    currentState.copy(
                        detectedVideo = currentVideo.copy(qualities = updatedQualities)
                    )
                }
            }
        }
    }

    fun selectVideoQuality(index: Int) {
        _uiState.update { state ->
            val current = state.detectedVideo ?: return@update state
            if (index in current.qualities.indices) {
                state.copy(detectedVideo = current.copy(selectedQualityIndex = index))
            } else {
                state
            }
        }
    }

    fun downloadSelectedVideo() {
        val videoInfo = _uiState.value.detectedVideo ?: return
        val quality = videoInfo.selectedQuality ?: return

        if (quality.isStreaming ||
            BrowserUrlUtils.isProtectedOrStreamingUrl(quality.url, quality.probedContentType ?: quality.mimeType)
        ) {
            emitToast("This video is streaming / protected and cannot be downloaded")
            return
        }

        val activeTab = _uiState.value.activeTab
        val userAgent = webViewPool.getEffectiveUserAgent(activeTab)

        viewModelScope.launch {
            val result = downloadHelper.enqueueDirectVideoDownload(
                videoUrl = quality.url,
                websiteTitle = videoInfo.pageTitle.ifBlank { activeTab.title },
                pageUrl = videoInfo.pageUrl.ifBlank { activeTab.url },
                userAgent = userAgent,
                estimatedBytes = quality.estimatedBytes
            )
            result.onSuccess { savedItem ->
                _uiState.update { it.copy(activeSheet = ActiveSheet.DOWNLOADS) }
                emitToast("Downloading ${savedItem.fileName}…")
            }.onFailure { err ->
                emitToast(err.localizedMessage ?: "Download failed to start")
            }
        }
    }

    fun scanVideosOnCurrentPage() {
        val active = _uiState.value.activeTab
        if (active.isHomePage) {
            emitToast("Open a website with video first")
            return
        }
        val wv = webViewPool.getWebView(active.id) ?: return
        wv.evaluateJavascript("(function(){ return window.__novaScanAllPageVideos ? window.__novaScanAllPageVideos() : '[]'; })();") { rawResult ->
            try {
                val unquoted = unquoteEvaluateJsResult(rawResult)
                val arr = JSONArray(unquoted.ifBlank { "[]" })
                if (arr.length() > 0) {
                    val firstObj = arr.getJSONObject(0)
                    val primaryUrl = firstObj.optString("primaryUrl", "")
                    if (primaryUrl.isNotBlank()) {
                        val qualities = mutableListOf<DetectedVideoQuality>()
                        val qArr = firstObj.optJSONArray("qualities")
                        if (qArr != null) {
                            for (i in 0 until qArr.length()) {
                                val q = qArr.getJSONObject(i)
                                val u = q.optString("url", "")
                                if (u.isNotBlank()) {
                                    val isStream = q.optBoolean("isStreaming", false) ||
                                        BrowserUrlUtils.isProtectedOrStreamingUrl(u)
                                    val estBytes = q.optLong("estimatedBytes", -1L)
                                    qualities.add(
                                        DetectedVideoQuality(
                                            url = u,
                                            label = q.optString("label", "Quality #${i + 1}"),
                                            mimeType = q.optString("mimeType", "video/mp4"),
                                            isDirectMp4 = !isStream,
                                            isStreaming = isStream,
                                            estimatedBytes = estBytes,
                                            isProbing = !isStream && estBytes <= 0L
                                        )
                                    )
                                }
                            }
                        }
                        handleVideoLongPressed(
                            DetectedVideoInfo(
                                pageTitle = firstObj.optString("pageTitle", active.title),
                                pageUrl = active.url,
                                domain = BrowserUrlUtils.extractDomain(active.url),
                                primaryUrl = primaryUrl,
                                posterUrl = firstObj.optString("posterUrl", ""),
                                qualities = qualities,
                                dimensions = firstObj.optString("dimensions", ""),
                                durationSeconds = firstObj.optInt("durationSeconds", 0)
                            )
                        )
                        return@evaluateJavascript
                    }
                }
                emitToast("No HTML5 <video> players detected on this page")
            } catch (_: Exception) {
                emitToast("Long-press directly on any video player to download")
            }
        }
    }

    // --- Play in Background Feature ---

    fun playVideoInBackground(info: DetectedVideoInfo) {
        val selected = info.selectedQuality ?: return
        if (selected.isStreaming || BrowserUrlUtils.isProtectedOrStreamingUrl(selected.url)) {
            emitToast("This video is streaming / protected and cannot be played in background")
            return
        }

        stopBackgroundPlayer()
        _uiState.update {
            it.copy(
                activeSheet = ActiveSheet.NONE,
                backgroundMedia = BackgroundMediaState(
                    isActive = true,
                    isPlaying = true,
                    title = info.pageTitle.ifBlank { "Background Video Audio" },
                    url = selected.url,
                    domain = info.domain
                )
            )
        }

        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(selected.url)
                setOnPreparedListener { player ->
                    player.start()
                    _uiState.update { state ->
                        state.copy(backgroundMedia = state.backgroundMedia.copy(isPlaying = true))
                    }
                }
                setOnCompletionListener {
                    _uiState.update { state ->
                        state.copy(backgroundMedia = state.backgroundMedia.copy(isPlaying = false))
                    }
                }
                setOnErrorListener { _, _, _ ->
                    emitToast("Unable to stream background audio for this video")
                    stopBackgroundPlayer()
                    true
                }
                prepareAsync()
            }
            backgroundMediaPlayer = mp
            emitToast("Playing in background • Keep browsing freely")
        } catch (e: Exception) {
            emitToast("Background playback error: ${e.localizedMessage ?: "Unsupported format"}")
            stopBackgroundPlayer()
        }
    }

    fun toggleBackgroundPlayPause() {
        val mp = backgroundMediaPlayer ?: return
        try {
            if (mp.isPlaying) {
                mp.pause()
                _uiState.update { it.copy(backgroundMedia = it.backgroundMedia.copy(isPlaying = false)) }
            } else {
                mp.start()
                _uiState.update { it.copy(backgroundMedia = it.backgroundMedia.copy(isPlaying = true)) }
            }
        } catch (_: Exception) {
        }
    }

    fun stopBackgroundPlayer() {
        try {
            backgroundMediaPlayer?.stop()
            backgroundMediaPlayer?.release()
        } catch (_: Exception) {
        }
        backgroundMediaPlayer = null
        _uiState.update {
            it.copy(backgroundMedia = BackgroundMediaState(isActive = false))
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopBackgroundPlayer()
    }
}
