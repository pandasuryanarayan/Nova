package com.example.ui.components

import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.browser.BrowserUrlUtils
import com.example.data.DevToolsConsoleEntry
import com.example.data.DevToolsNetworkEntry
import com.example.data.DevToolsSourceFile
import com.example.data.DevToolsState
import com.example.data.DevToolsTab
import com.example.data.DomSelectedElementDetail
import com.example.data.DomTreeNodeItem
import com.example.ui.icons.NovaIcons
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MaterialTheme

@Composable
fun DevToolsInspectorPanel(
    devToolsState: DevToolsState,
    onSelectTab: (DevToolsTab) -> Unit,
    onToggleInspectElementPicker: () -> Unit,
    onRefreshDevTools: () -> Unit,
    onSelectDomNode: (String) -> Unit,
    onApplyElementEdit: (nodeId: String, inlineStyle: String, textContent: String, attrName: String, attrValue: String) -> Unit,
    onDeleteDomNode: (String) -> Unit,
    onSelectSourceFile: (String) -> Unit,
    onFetchRemoteSource: (DevToolsSourceFile) -> Unit,
    onCopyText: (String, String) -> Unit,
    onExecuteConsoleJs: (String) -> Unit,
    onClearConsole: () -> Unit,
    onClearNetwork: () -> Unit,
    onCloseDevTools: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0A0F1E),
        contentColor = Color(0xFFF3F6FF),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.54f)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .testTag("devtools_inspector_panel")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Toolbar: Element Picker + 4 Tabs + Refresh + Close
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF11182E))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (devToolsState.isInspectElementPickerActive) Color(0xFF00E5FF) else Color(0xFF1C2747),
                    onClick = onToggleInspectElementPicker,
                    modifier = Modifier.testTag("devtools_pick_element_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = NovaIcons.AdsClick,
                            contentDescription = "Pick Element on Page",
                            tint = if (devToolsState.isInspectElementPickerActive) Color(0xFF04151F) else Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (devToolsState.isInspectElementPickerActive) "Tap…" else "Pick",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (devToolsState.isInspectElementPickerActive) Color(0xFF04151F) else Color(0xFF00E5FF)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DevToolsTab.entries.forEach { tab ->
                        val isSelected = devToolsState.selectedTab == tab
                        val countBadge = when (tab) {
                            DevToolsTab.CONSOLE -> if (devToolsState.consoleLogs.isNotEmpty()) " (${devToolsState.consoleLogs.size})" else ""
                            DevToolsTab.NETWORK -> if (devToolsState.networkEntries.isNotEmpty()) " (${devToolsState.networkEntries.size})" else ""
                            else -> ""
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectTab(tab) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("devtools_tab_${tab.name.lowercase()}")
                        ) {
                            Text(
                                text = "${tab.title}$countBadge",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF9EABC9),
                                maxLines = 1
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(2.dp)
                                        .background(Color(0xFF00E5FF))
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onRefreshDevTools,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = NovaIcons.Refresh,
                        contentDescription = "Refresh Inspector",
                        tint = Color(0xFF9EABC9),
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onCloseDevTools,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("devtools_close_button")
                ) {
                    Icon(
                        imageVector = NovaIcons.Close,
                        contentDescription = "Close DevTools",
                        tint = Color(0xFF9EABC9),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF26345E))

            Box(modifier = Modifier.fillMaxSize()) {
                when (devToolsState.selectedTab) {
                    DevToolsTab.ELEMENTS -> ElementsTabContent(
                        domTree = devToolsState.domTree,
                        selectedElement = devToolsState.selectedElement,
                        isPickerActive = devToolsState.isInspectElementPickerActive,
                        onSelectDomNode = onSelectDomNode,
                        onApplyElementEdit = onApplyElementEdit,
                        onDeleteDomNode = onDeleteDomNode,
                        onCopyText = onCopyText
                    )

                    DevToolsTab.SOURCES -> SourcesTabContent(
                        sources = devToolsState.sources,
                        selectedSourceId = devToolsState.selectedSourceId,
                        onSelectSource = onSelectSourceFile,
                        onFetchRemoteSource = onFetchRemoteSource,
                        onCopyText = onCopyText
                    )

                    DevToolsTab.CONSOLE -> ConsoleTabContent(
                        logs = devToolsState.consoleLogs,
                        onExecuteJs = onExecuteConsoleJs,
                        onClearConsole = onClearConsole,
                        onCopyText = onCopyText
                    )

                    DevToolsTab.NETWORK -> NetworkTabContent(
                        entries = devToolsState.networkEntries,
                        onClearNetwork = onClearNetwork,
                        onCopyText = onCopyText
                    )
                }
            }
        }
    }
}

@Composable
private fun ElementsTabContent(
    domTree: List<DomTreeNodeItem>,
    selectedElement: DomSelectedElementDetail?,
    isPickerActive: Boolean,
    onSelectDomNode: (String) -> Unit,
    onApplyElementEdit: (nodeId: String, inlineStyle: String, textContent: String, attrName: String, attrValue: String) -> Unit,
    onDeleteDomNode: (String) -> Unit,
    onCopyText: (String, String) -> Unit
) {
    var showDetailPane by remember(selectedElement?.nodeId) {
        mutableStateOf(selectedElement != null)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isPickerActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00E5FF).copy(alpha = 0.16f))
            ) {
                Text(
                    text = "Element Picker Active: Tap any element on the webpage above to inspect DOM & CSS.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00E5FF),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        if (selectedElement != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141D38))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = selectedElement.breadcrumb,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF00E5FF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF26345E),
                    onClick = { showDetailPane = !showDetailPane }
                ) {
                    Text(
                        text = if (showDetailPane) "View Full DOM Tree" else "Inspect Styles & Edit",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (showDetailPane && selectedElement != null) {
            SelectedElementDetailView(
                element = selectedElement,
                onApplyEdit = onApplyElementEdit,
                onDeleteNode = {
                    onDeleteDomNode(selectedElement.nodeId)
                    showDetailPane = false
                },
                onCopyHtml = { onCopyText("OuterHTML", selectedElement.outerHtml) }
            )
        } else if (domTree.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Open any webpage and tap Refresh or 'Pick' to inspect live DOM elements.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9EABC9),
                    modifier = Modifier.padding(20.dp)
                )
            }
        } else {
            val visibleNodes = remember(domTree) { domTree.take(150) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                visibleNodes.forEach { node ->
                    val isSelected = selectedElement?.nodeId == node.nodeId
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color.Transparent)
                            .clickable {
                                onSelectDomNode(node.nodeId)
                                showDetailPane = true
                            }
                            .padding(
                                start = ((node.depth.coerceAtMost(8)) * 12 + 6).dp,
                                end = 8.dp,
                                top = 5.dp,
                                bottom = 5.dp
                            )
                    ) {
                        Text(
                            text = "<${node.selector}>",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF8C62FF)
                        )
                        if (node.textPreview.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "\"${node.textPreview}\"",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB5C7E8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Text(
                            text = "</${node.tagName}>",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedElementDetailView(
    element: DomSelectedElementDetail,
    onApplyEdit: (nodeId: String, inlineStyle: String, textContent: String, attrName: String, attrValue: String) -> Unit,
    onDeleteNode: () -> Unit,
    onCopyHtml: () -> Unit
) {
    var inlineStyleInput by remember(element.nodeId, element.inlineStyle) {
        mutableStateOf(element.inlineStyle)
    }
    val textContentInput = remember(element.nodeId, element.innerText) { element.innerText }
    var attrNameInput by remember(element.nodeId) { mutableStateOf("") }
    var attrValInput by remember(element.nodeId) { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1C2747)) {
                Text(
                    text = "Box: ${element.widthPx}×${element.heightPx} @ (${element.leftPx}, ${element.topPx})",
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00E5FF),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onCopyHtml,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(NovaIcons.ContentCopy, "Copy HTML", Modifier.size(14.dp), tint = Color(0xFF00E5FF))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00E5FF))
                }
                OutlinedButton(
                    onClick = onDeleteNode,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(NovaIcons.Delete, "Remove Element", Modifier.size(14.dp), tint = Color(0xFFFF5252))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Remove", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF5252))
                }
            }
        }

        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF141D38), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "element.style (Live CSS Editor)",
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF00E5FF)
                )
                Spacer(modifier = Modifier.height(6.dp))
                NovaEditText(
                    value = inlineStyleInput,
                    onValueChange = { inlineStyleInput = it },
                    placeholder = "e.g. border: 2px solid red; background: #222;",
                    containerColor = Color(0xFF0E1529),
                    borderColor = Color(0xFF2E3F6E),
                    textColor = Color.White,
                    hintColor = Color(0xFF64748B),
                    monospace = true,
                    textSizeSp = 12f,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    NovaEditText(
                        value = attrNameInput,
                        onValueChange = { attrNameInput = it },
                        placeholder = "Attr name",
                        containerColor = Color(0xFF0E1529),
                        borderColor = Color(0xFF2E3F6E),
                        textColor = Color.White,
                        hintColor = Color(0xFF64748B),
                        monospace = true,
                        textSizeSp = 12f,
                        modifier = Modifier.weight(1f)
                    )
                    NovaEditText(
                        value = attrValInput,
                        onValueChange = { attrValInput = it },
                        placeholder = "Attr value",
                        containerColor = Color(0xFF0E1529),
                        borderColor = Color(0xFF2E3F6E),
                        textColor = Color.White,
                        hintColor = Color(0xFF64748B),
                        monospace = true,
                        textSizeSp = 12f,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        onApplyEdit(
                            element.nodeId,
                            inlineStyleInput,
                            textContentInput,
                            attrNameInput,
                            attrValInput
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Changes to Live Webpage", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        if (element.attributes.isNotEmpty()) {
            Text(
                text = "DOM Attributes (${element.attributes.size})",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF00E5FF)
            )
            element.attributes.forEach { (k, v) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141D38), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("$k=", fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.labelMedium, color = Color(0xFFFFB300))
                    Text("\"$v\"", fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.labelMedium, color = Color(0xFF00E676))
                }
            }
        }

        Text(
            text = "Computed CSS Styles",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF00E5FF)
        )
        element.computedStyles.forEach { (prop, value) ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF11182E), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(prop, fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9EABC9))
                Text(
                    text = value,
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFF3F6FF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Text(text = "outerHTML", style = MaterialTheme.typography.labelLarge, color = Color(0xFF00E5FF))
        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF11182E), modifier = Modifier.fillMaxWidth()) {
            Text(
                text = element.outerHtml,
                fontFamily = JetBrainsMonoFontFamily,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFD5E2FF),
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
private fun SourcesTabContent(
    sources: List<DevToolsSourceFile>,
    selectedSourceId: String,
    onSelectSource: (String) -> Unit,
    onFetchRemoteSource: (DevToolsSourceFile) -> Unit,
    onCopyText: (String, String) -> Unit
) {
    val activeSource = sources.find { it.id == selectedSourceId } ?: sources.firstOrNull()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141D38))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            sources.forEach { file ->
                val isSelected = file.id == activeSource?.id
                val badgeColor = when (file.type) {
                    "HTML" -> Color(0xFFFF7043)
                    "CSS" -> Color(0xFF00E5FF)
                    else -> Color(0xFFFFB300)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFF26345E) else Color(0xFF0E1529),
                    onClick = { onSelectSource(file.id) },
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = if (isSelected) badgeColor else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(file.type, fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = badgeColor)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(file.name.take(24), style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1)
                    }
                }
            }
        }

        if (activeSource == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Open a webpage to inspect its HTML, CSS, and JavaScript source files.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9EABC9)
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0E1529))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = activeSource.url,
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9EABC9),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if ((activeSource.url.startsWith("http") && activeSource.content.startsWith("// External")) ||
                        activeSource.content.startsWith("/* External")
                    ) {
                        FilledTonalButton(
                            onClick = { onFetchRemoteSource(activeSource) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(NovaIcons.Download, null, Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Load Remote", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    IconButton(
                        onClick = { onCopyText(activeSource.name, activeSource.content) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(NovaIcons.ContentCopy, "Copy Source Code", Modifier.size(15.dp), tint = Color(0xFF00E5FF))
                    }
                }
            }

            val lines = remember(activeSource.content) { activeSource.content.lines().take(250) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF090D1A))
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 6.dp)
            ) {
                lines.forEachIndexed { index, lineText ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${index + 1}".padStart(4, ' '),
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF475569),
                            modifier = Modifier.width(38.dp)
                        )
                        Text(
                            text = lineText,
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsoleTabContent(
    logs: List<DevToolsConsoleEntry>,
    onExecuteJs: (String) -> Unit,
    onClearConsole: () -> Unit,
    onCopyText: (String, String) -> Unit
) {
    var jsCommand by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredLogs = remember(logs, selectedFilter) {
        val list = if (selectedFilter == "ALL") logs else logs.filter { it.level.equals(selectedFilter, ignoreCase = true) }
        list.takeLast(120)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141D38))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("ALL", "ERROR", "WARN", "LOG", "RESULT").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.22f) else Color.Transparent,
                        onClick = { selectedFilter = filter }
                    ) {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF9EABC9),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onClearConsole, modifier = Modifier.size(28.dp)) {
                Icon(NovaIcons.DeleteSweep, "Clear Console", Modifier.size(16.dp), tint = Color(0xFF9EABC9))
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            filteredLogs.forEach { item ->
                val levelColor = when (item.level.uppercase()) {
                    "ERROR" -> Color(0xFFFF5252)
                    "WARN", "WARNING" -> Color(0xFFFFB300)
                    "RESULT" -> Color(0xFF00E676)
                    "CMD" -> Color(0xFF00E5FF)
                    else -> Color(0xFFE2E8F0)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF11182E),
                    onClick = { onCopyText("Console Log", item.message) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "[${item.level.take(4)}]",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = levelColor,
                            modifier = Modifier.width(50.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.message, fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.labelSmall, color = levelColor)
                            if (item.source.isNotBlank()) {
                                Text(item.source, fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF11182E))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Icon(NovaIcons.Terminal, null, Modifier.size(18.dp), tint = Color(0xFF00E5FF))
            Spacer(modifier = Modifier.width(6.dp))
            NovaEditText(
                value = jsCommand,
                onValueChange = { jsCommand = it },
                placeholder = "Run JS (e.g. document.title)…",
                containerColor = Color(0xFF0B1020),
                borderColor = Color(0xFF26345E),
                textColor = Color.White,
                hintColor = Color(0xFF64748B),
                monospace = true,
                textSizeSp = 12f,
                imeAction = EditorInfo.IME_ACTION_SEND,
                onImeAction = {
                    if (jsCommand.isNotBlank()) {
                        onExecuteJs(jsCommand)
                        jsCommand = ""
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("devtools_console_input")
            )
            IconButton(
                onClick = {
                    if (jsCommand.isNotBlank()) {
                        onExecuteJs(jsCommand)
                        jsCommand = ""
                    }
                },
                modifier = Modifier.testTag("devtools_console_run_button")
            ) {
                Icon(NovaIcons.Send, "Execute JS", tint = Color(0xFF00E5FF))
            }
        }
    }
}

@Composable
private fun NetworkTabContent(
    entries: List<DevToolsNetworkEntry>,
    onClearNetwork: () -> Unit,
    onCopyText: (String, String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedEntry by remember { mutableStateOf<DevToolsNetworkEntry?>(null) }

    val filteredEntries = remember(entries, selectedFilter) {
        val list = when (selectedFilter) {
            "XHR" -> entries.filter { it.initiatorType in listOf("fetch", "xhr", "xmlhttprequest") }
            "JS" -> entries.filter { it.initiatorType == "script" || it.url.contains(".js") }
            "CSS" -> entries.filter { it.initiatorType in listOf("css", "link") || it.url.contains(".css") }
            "MEDIA" -> entries.filter { it.initiatorType in listOf("video", "img", "audio") || it.url.contains(".mp4") }
            else -> entries
        }
        list.take(100)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141D38))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("ALL", "XHR", "JS", "CSS", "MEDIA").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.22f) else Color.Transparent,
                        onClick = {
                            selectedFilter = filter
                            selectedEntry = null
                        }
                    ) {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF9EABC9),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    selectedEntry = null
                    onClearNetwork()
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(NovaIcons.DeleteSweep, "Clear Network Log", Modifier.size(16.dp), tint = Color(0xFF9EABC9))
            }
        }

        val detail = selectedEntry
        if (detail != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${detail.method} • HTTP ${detail.status}",
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (detail.status in 200..299) Color(0xFF00E676) else Color(0xFFFFB300)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { onCopyText("cURL Command", "curl -X ${detail.method} '${detail.url}'") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Copy cURL", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = { selectedEntry = null },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Back to List", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF11182E), modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(10.dp)) {
                        Text("Request URL: ${detail.url}", fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.labelSmall, color = Color(0xFF00E5FF))
                        Text("Type: ${detail.initiatorType.uppercase()} • MIME: ${detail.mimeType}", fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9EABC9))
                        Text("Size: ${BrowserUrlUtils.formatBytes(detail.sizeBytes)} • Time: ${detail.durationMs} ms", fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.labelSmall, color = Color(0xFF00E676))
                    }
                }

                if (detail.responsePreview.isNotBlank()) {
                    Text("Response Body Preview", style = MaterialTheme.typography.labelLarge, color = Color(0xFF00E5FF))
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF090D1A), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = detail.responsePreview,
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                filteredEntries.forEach { req ->
                    val statusColor = when (req.status) {
                        in 200..299 -> Color(0xFF00E676)
                        in 300..399 -> Color(0xFF00E5FF)
                        else -> Color(0xFFFF5252)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF11182E),
                        onClick = { selectedEntry = req },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "${req.status}",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.width(34.dp)
                            )
                            Text(
                                text = req.method,
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFB300),
                                modifier = Modifier.width(38.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                val shortName = req.url.substringAfterLast('/').substringBefore('?').ifBlank {
                                    BrowserUrlUtils.extractDomain(req.url)
                                }
                                Text(shortName, fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(req.url, fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(req.initiatorType.uppercase(), fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.labelSmall, color = Color(0xFF00E5FF))
                                Text("${req.durationMs} ms", fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9EABC9))
                            }
                        }
                    }
                }
            }
        }
    }
}
