package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.browser.AdBlocker
import com.example.browser.AdFilterRuleItem
import com.example.browser.rememberShortcutIcon
import com.example.data.AdBlockMode
import com.example.data.AppLanguage
import com.example.data.BlockedAdEntry
import com.example.data.BookmarkEntity
import com.example.data.BrowserTab
import com.example.data.DownloadItemEntity
import com.example.data.DownloadStatus
import com.example.data.HistoryEntity
import com.example.data.SslCertificateDetails
import com.example.ui.i18n.LocalNovaStrings
import com.example.ui.icons.NovaIcons
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ============================================================================
// SHARED REDESIGN PALETTE & HELPERS
// ============================================================================

internal data class SheetVisualPalette(
    val sheetBg: Color,
    val cardBg: Color,
    val cardElevatedBg: Color,
    val cardBorder: Color,
    val badgeBg: Brush,
    val badgeBorder: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val accentGradient: Brush,
    val activePillBg: Color,
    val activePillBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val dangerRed: Color,
    val dangerBg: Color,
    val dangerBorder: Color
)

@Composable
internal fun rememberSheetPalette(isIncognito: Boolean = false): SheetVisualPalette {
    val scheme = MaterialTheme.colorScheme
    val isDark = isIncognito || scheme.background.luminance() < 0.35f
    val accentPrimary = scheme.primary
    val accentSecondary = scheme.secondary
    return SheetVisualPalette(
        sheetBg = if (isDark) Color(0xFF0C1120) else scheme.background,
        cardBg = if (isDark) Color(0xFF131B2E) else scheme.surface,
        cardElevatedBg = if (isDark) Color(0xFF19233B) else scheme.surfaceVariant.copy(alpha = 0.65f),
        cardBorder = if (isDark) Color(0xFF273657) else scheme.outline.copy(alpha = 0.42f),
        badgeBg = Brush.linearGradient(
            colors = listOf(
                accentPrimary.copy(alpha = 0.22f),
                accentSecondary.copy(alpha = 0.14f)
            )
        ),
        badgeBorder = accentPrimary.copy(alpha = 0.42f),
        accentPrimary = accentPrimary,
        accentSecondary = accentSecondary,
        accentGradient = Brush.horizontalGradient(listOf(accentPrimary, accentSecondary)),
        activePillBg = accentPrimary.copy(alpha = if (isDark) 0.24f else 0.14f),
        activePillBorder = accentPrimary.copy(alpha = 0.72f),
        textPrimary = scheme.onSurface,
        textSecondary = scheme.onSurfaceVariant,
        textMuted = scheme.onSurfaceVariant.copy(alpha = 0.75f),
        dangerRed = Color(0xFFE53935),
        dangerBg = Color(0xFFE53935).copy(alpha = 0.12f),
        dangerBorder = Color(0xFFE53935).copy(alpha = 0.38f)
    )
}

@Composable
private fun SheetHeaderIconBadge(
    icon: ImageVector,
    palette: SheetVisualPalette,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(palette.badgeBg)
            .border(1.dp, palette.badgeBorder, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = palette.accentPrimary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun MonogramSiteBadge(
    label: String,
    seed: String,
    url: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cachedIcon = if (!url.isNullOrBlank()) {
        rememberShortcutIcon(context = context, url = url).value
    } else {
        null
    }
    val accentPrimary = MaterialTheme.colorScheme.primary
    val letter = label.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "N"

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (cachedIcon != null) Color.White else accentPrimary.copy(alpha = 0.15f)
            )
            .border(1.dp, accentPrimary.copy(alpha = 0.38f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (cachedIcon != null) {
            Image(
                bitmap = cachedIcon,
                contentDescription = label,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        } else {
            Text(
                text = letter,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = accentPrimary
            )
        }
    }
}

private fun extractHostClean(url: String): String {
    return try {
        val host = URI(url).host?.removePrefix("www.")
        if (!host.isNullOrBlank()) host else url.removePrefix("https://").removePrefix("http://").substringBefore("/")
    } catch (_: Exception) {
        url.removePrefix("https://").removePrefix("http://").substringBefore("/")
    }
}

// ============================================================================
// 1. TABS SWITCHER BOTTOM SHEET
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabsSwitcherSheet(
    tabs: List<BrowserTab>,
    activeTabId: String,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onDuplicateTab: (String) -> Unit,
    onNewNormalTab: () -> Unit,
    onNewIncognitoTab: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalNovaStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val palette = rememberSheetPalette()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.sheetBg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("tabs_switcher_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
        ) {
            // Top Header Row: Tab Icon Badge + Title/Stats + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    SheetHeaderIconBadge(icon = NovaIcons.Tab, palette = palette)
                    Column {
                        Text(
                            text = strings.openTabsTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = palette.textPrimary
                        )
                        val incognitoCount = tabs.count { it.isIncognito }
                        Text(
                            text = strings.tabsHeaderStats(tabs.size, incognitoCount),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.8.sp
                            ),
                            color = palette.textSecondary
                        )
                    }
                }

                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    color = palette.cardBg,
                    border = BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = NovaIcons.Close,
                            contentDescription = "Close",
                            tint = palette.accentPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dedicated Spaced Action Row for "Private" and "New Tab" Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onNewIncognitoTab,
                    shape = RoundedCornerShape(14.dp),
                    color = palette.cardBg,
                    border = BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("new_incognito_tab_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = NovaIcons.Incognito,
                            contentDescription = strings.privateTabButton,
                            tint = palette.accentPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.privateTabButton,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    onClick = onNewNormalTab,
                    shape = RoundedCornerShape(14.dp),
                    color = palette.activePillBg,
                    border = BorderStroke(1.5.dp, palette.activePillBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("new_tab_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = NovaIcons.Add,
                            contentDescription = strings.newTabButton,
                            tint = palette.accentPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.newTabButton,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.accentPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 156.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            ) {
                items(tabs, key = { it.id }) { tab ->
                    val isSelected = tab.id == activeTabId
                    Surface(
                        onClick = { onSelectTab(tab.id) },
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) palette.activePillBg else palette.cardBg,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) palette.activePillBorder else palette.cardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tab_item_${tab.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (tab.isIncognito) NovaIcons.Incognito else NovaIcons.Public,
                                        contentDescription = null,
                                        tint = palette.accentPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (tab.isHomePage) strings.novaHomeTabTitle else tab.title,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = palette.textPrimary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onDuplicateTab(tab.id) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("duplicate_tab_${tab.id}")
                                    ) {
                                        Icon(
                                            imageVector = NovaIcons.ContentCopy,
                                            contentDescription = "Duplicate Tab",
                                            tint = palette.accentPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onCloseTab(tab.id) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("close_tab_${tab.id}")
                                    ) {
                                        Icon(
                                            imageVector = NovaIcons.Close,
                                            contentDescription = "Close Tab",
                                            tint = palette.textSecondary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(74.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(palette.cardElevatedBg)
                                    .border(1.dp, palette.cardBorder, RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column {
                                    Text(
                                        text = if (tab.isHomePage) "nova://start" else extractHostClean(tab.url),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = palette.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (tab.isIncognito) strings.stealthTabBadge else strings.standardTabBadge,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp
                                        ),
                                        color = if (tab.isIncognito) palette.accentPrimary else palette.textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. DOWNLOADS MANAGER BOTTOM SHEET (Ongoing / Success / Failure)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsManagerSheet(
    downloads: List<DownloadItemEntity>,
    onPauseDownload: (DownloadItemEntity) -> Unit,
    onResumeDownload: (DownloadItemEntity) -> Unit,
    onOpenFile: (DownloadItemEntity) -> Unit,
    onDeleteDownload: (DownloadItemEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalNovaStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val palette = rememberSheetPalette()

    // 0 = Ongoing, 1 = Success, 2 = Failure
    val initialTab = remember(downloads.size) {
        when {
            downloads.any {
                val st = it.statusEnum
                st == DownloadStatus.RUNNING || st == DownloadStatus.PAUSED || st == DownloadStatus.PENDING
            } -> 0
            downloads.any { it.statusEnum == DownloadStatus.COMPLETED } -> 1
            downloads.any { it.statusEnum == DownloadStatus.FAILED } -> 2
            else -> 1 // Default to Success/Completed videos view when empty
        }
    }
    var selectedSection by remember { mutableIntStateOf(initialTab) }
    var sortNewestFirst by remember { mutableStateOf(true) }

    val ongoingDownloads = remember(downloads, sortNewestFirst) {
        val list = downloads.filter {
            val st = it.statusEnum
            st == DownloadStatus.RUNNING ||
                st == DownloadStatus.PAUSED ||
                st == DownloadStatus.PENDING
        }
        if (sortNewestFirst) list.sortedByDescending { it.createdAt } else list.sortedBy { it.createdAt }
    }
    val successDownloads = remember(downloads, sortNewestFirst) {
        val list = downloads.filter { it.statusEnum == DownloadStatus.COMPLETED }
        if (sortNewestFirst) list.sortedByDescending { it.createdAt } else list.sortedBy { it.createdAt }
    }
    val failedDownloads = remember(downloads, sortNewestFirst) {
        val list = downloads.filter { it.statusEnum == DownloadStatus.FAILED }
        if (sortNewestFirst) list.sortedByDescending { it.createdAt } else list.sortedBy { it.createdAt }
    }

    val displayedDownloads = when (selectedSection) {
        0 -> ongoingDownloads
        1 -> successDownloads
        else -> failedDownloads
    }

    val totalBytes = remember(downloads) {
        downloads.sumOf { maxOf(it.downloadedBytes, 0L) }
    }
    val totalMbFormatted = remember(totalBytes) {
        if (totalBytes <= 0L) "0 MB" else String.format(Locale.US, "%.1f MB", totalBytes / (1024.0 * 1024.0))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.sheetBg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("downloads_manager_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SheetHeaderIconBadge(icon = NovaIcons.Download, palette = palette)
                    Column {
                        Text(
                            text = strings.downloadsTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = palette.textPrimary
                        )
                        Text(
                            text = strings.downloadsHeaderStats(downloads.size, totalMbFormatted),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.8.sp
                            ),
                            color = palette.textSecondary
                        )
                    }
                }

                // "Open folder" pill button
                Surface(
                    onClick = {
                        val firstCompleted = successDownloads.firstOrNull() ?: downloads.firstOrNull()
                        if (firstCompleted != null) {
                            onOpenFile(firstCompleted)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = palette.cardBg,
                    border = BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier.testTag("downloads_open_folder_button")
                ) {
                    Text(
                        text = strings.openFolderButton,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.accentPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Folder path line
            Text(
                text = strings.downloadsFolderLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.4.sp
                ),
                color = palette.textMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Pills Row: Success, Ongoing, Failure + Sort: Newest/Oldest
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DownloadFilterPill(
                    label = "${strings.downloadTabSuccess} (${successDownloads.size})",
                    selected = selectedSection == 1,
                    palette = palette,
                    onClick = { selectedSection = 1 },
                    modifier = Modifier.testTag("download_tab_success")
                )
                DownloadFilterPill(
                    label = "${strings.downloadTabOngoing} (${ongoingDownloads.size})",
                    selected = selectedSection == 0,
                    palette = palette,
                    onClick = { selectedSection = 0 },
                    modifier = Modifier.testTag("download_tab_ongoing")
                )
                DownloadFilterPill(
                    label = "${strings.downloadTabFailure} (${failedDownloads.size})",
                    selected = selectedSection == 2,
                    palette = palette,
                    onClick = { selectedSection = 2 },
                    modifier = Modifier.testTag("download_tab_failure")
                )
                DownloadFilterPill(
                    label = if (sortNewestFirst) strings.sortNewest else strings.sortOldest,
                    selected = false,
                    palette = palette,
                    onClick = { sortNewestFirst = !sortNewestFirst },
                    modifier = Modifier.testTag("download_sort_chip")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (displayedDownloads.isEmpty()) {
                // Empty state card
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = palette.cardBg,
                    border = BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SheetHeaderIconBadge(
                            icon = when (selectedSection) {
                                0 -> NovaIcons.Download
                                1 -> NovaIcons.Videocam
                                else -> NovaIcons.Warning
                            },
                            palette = palette
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (selectedSection) {
                                0 -> strings.noOngoingDownloadsTitle
                                1 -> strings.noSuccessDownloadsTitle
                                else -> strings.noFailureDownloadsTitle
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when (selectedSection) {
                                0 -> strings.noOngoingDownloadsSub
                                1 -> strings.noSuccessDownloadsSub
                                else -> strings.noFailureDownloadsSub
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 380.dp)
                ) {
                    items(displayedDownloads, key = { it.id }) { item ->
                        DownloadItemCard(
                            item = item,
                            palette = palette,
                            onPause = { onPauseDownload(item) },
                            onResume = { onResumeDownload(item) },
                            onOpen = { onOpenFile(item) },
                            onDelete = { onDeleteDownload(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadFilterPill(
    label: String,
    selected: Boolean,
    palette: SheetVisualPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) palette.activePillBg else palette.cardBg,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) palette.activePillBorder else palette.cardBorder
        ),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (selected) palette.accentPrimary else palette.textSecondary,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)
        )
    }
}

@Composable
private fun DownloadItemCard(
    item: DownloadItemEntity,
    palette: SheetVisualPalette,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val statusEnum = item.statusEnum
    val progress = item.progressFraction

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = palette.cardBg,
        border = BorderStroke(1.dp, palette.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("download_item_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    SheetHeaderIconBadge(
                        icon = NovaIcons.Videocam,
                        palette = palette,
                        modifier = Modifier.size(40.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.fileName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = palette.textPrimary
                        )
                        val statusText = when (statusEnum) {
                            DownloadStatus.RUNNING -> "DOWNLOADING • ${formatBytes(item.downloadedBytes)} / ${formatBytes(item.totalBytes)}"
                            DownloadStatus.PAUSED -> "PAUSED • ${formatBytes(item.downloadedBytes)}"
                            DownloadStatus.COMPLETED -> "COMPLETED • ${formatBytes(item.downloadedBytes)}"
                            DownloadStatus.FAILED -> "FAILED • TAP RETRY"
                            DownloadStatus.PENDING -> "QUEUED..."
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            color = when (statusEnum) {
                                DownloadStatus.FAILED -> palette.dangerRed
                                DownloadStatus.COMPLETED -> Color(0xFF10B981)
                                else -> palette.textSecondary
                            }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (statusEnum) {
                        DownloadStatus.RUNNING -> {
                            IconButton(
                                onClick = onPause,
                                modifier = Modifier.testTag("pause_download_${item.id}")
                            ) {
                                Icon(
                                    imageVector = NovaIcons.Pause,
                                    contentDescription = "Pause Download",
                                    tint = palette.accentPrimary
                                )
                            }
                        }

                        DownloadStatus.PAUSED -> {
                            IconButton(
                                onClick = onResume,
                                modifier = Modifier.testTag("resume_download_${item.id}")
                            ) {
                                Icon(
                                    imageVector = NovaIcons.PlayArrow,
                                    contentDescription = "Resume Download",
                                    tint = palette.accentPrimary
                                )
                            }
                        }

                        DownloadStatus.FAILED -> {
                            IconButton(
                                onClick = onResume,
                                modifier = Modifier.testTag("retry_download_${item.id}")
                            ) {
                                Icon(
                                    imageVector = NovaIcons.Refresh,
                                    contentDescription = "Retry Download",
                                    tint = palette.accentPrimary
                                )
                            }
                        }

                        DownloadStatus.COMPLETED -> {
                            IconButton(
                                onClick = onOpen,
                                modifier = Modifier.testTag("open_download_${item.id}")
                            ) {
                                Icon(
                                    imageVector = NovaIcons.FolderOpen,
                                    contentDescription = "Open File",
                                    tint = palette.accentPrimary
                                )
                            }
                        }

                        DownloadStatus.PENDING -> Unit
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_download_${item.id}")
                    ) {
                        Icon(
                            imageVector = NovaIcons.DeleteOutline,
                            contentDescription = "Delete Download",
                            tint = palette.dangerRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (statusEnum == DownloadStatus.RUNNING || statusEnum == DownloadStatus.PAUSED) {
                Spacer(modifier = Modifier.height(10.dp))
                if (item.totalBytes > 0) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape),
                        color = palette.accentPrimary,
                        trackColor = palette.cardElevatedBg
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape),
                        color = palette.accentPrimary,
                        trackColor = palette.cardElevatedBg
                    )
                }
            }
        }
    }
}

// ============================================================================
// 3. BOOKMARKS SHEET
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksSheet(
    bookmarks: List<BookmarkEntity>,
    onOpenUrl: (String) -> Unit,
    onDeleteBookmark: (Long) -> Unit,
    onDismiss: () -> Unit,
    onAddBookmark: ((String, String) -> Unit)? = null,
    onOpenAllBookmarks: ((List<String>) -> Unit)? = null
) {
    val strings = LocalNovaStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val palette = rememberSheetPalette()
    var isEditingMode by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newUrl by remember { mutableStateOf("https://") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.sheetBg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("bookmarks_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SheetHeaderIconBadge(icon = NovaIcons.BookmarkBorder, palette = palette)
                    Column {
                        Text(
                            text = strings.bookmarksTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = palette.textPrimary
                        )
                        Text(
                            text = strings.bookmarksHeaderStats(bookmarks.size),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.8.sp
                            ),
                            color = palette.textSecondary
                        )
                    }
                }

                // "✎ Edit" button
                Surface(
                    onClick = { isEditingMode = !isEditingMode },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isEditingMode) palette.activePillBg else palette.cardBg,
                    border = BorderStroke(1.dp, if (isEditingMode) palette.activePillBorder else palette.cardBorder),
                    modifier = Modifier.testTag("bookmarks_edit_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = NovaIcons.Edit,
                            contentDescription = strings.editAction,
                            tint = palette.accentPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isEditingMode) strings.doneAction else strings.editAction,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (bookmarks.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 310.dp)
                ) {
                    items(bookmarks, key = { it.id }) { bookmark ->
                        val cleanUrl = remember(bookmark.url) {
                            bookmark.url.removePrefix("https://").removePrefix("http://").removePrefix("www.")
                        }
                        Surface(
                            onClick = { onOpenUrl(bookmark.url) },
                            shape = RoundedCornerShape(20.dp),
                            color = palette.cardBg,
                            border = BorderStroke(1.dp, palette.cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bookmark_item_${bookmark.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MonogramSiteBadge(
                                    label = bookmark.title.ifBlank { cleanUrl },
                                    seed = cleanUrl,
                                    url = bookmark.url
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bookmark.title,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 20.sp
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = palette.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = cleanUrl,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = palette.textSecondary
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteBookmark(bookmark.id) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("delete_bookmark_${bookmark.id}")
                                ) {
                                    Icon(
                                        imageVector = NovaIcons.DeleteOutline,
                                        contentDescription = strings.deleteAction,
                                        tint = palette.dangerRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Hint Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = palette.cardBg,
                border = BorderStroke(1.dp, palette.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(strings.bookmarksHintPrefix)
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = palette.accentPrimary)) {
                            append(strings.addBookmarkButton)
                        }
                        append(strings.bookmarksHintSuffix)
                    },
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
                    color = palette.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom 2-button row: "+ Add bookmark" and "Open all"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    color = palette.cardBg,
                    border = BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bookmarks_add_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = NovaIcons.Add,
                            contentDescription = null,
                            tint = palette.accentPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.addBookmarkButton,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.textPrimary
                        )
                    }
                }

                Surface(
                    onClick = {
                        if (bookmarks.isNotEmpty()) {
                            if (onOpenAllBookmarks != null) {
                                onOpenAllBookmarks(bookmarks.map { it.url })
                            } else {
                                onOpenUrl(bookmarks.first().url)
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = palette.cardBg,
                    border = BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bookmarks_open_all_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.openAllBookmarksButton,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (bookmarks.isNotEmpty()) palette.accentPrimary else palette.textMuted
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(strings.addBookmarkButton, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text(strings.siteTitleLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        label = { Text(strings.websiteUrlLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanTarget = newUrl.trim()
                        if (cleanTarget.isNotEmpty()) {
                            if (onAddBookmark != null) {
                                onAddBookmark(newTitle.ifBlank { cleanTarget }, cleanTarget)
                            } else {
                                onOpenUrl(cleanTarget)
                            }
                            showAddDialog = false
                        }
                    }
                ) {
                    Text(strings.saveButton)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(strings.cancelButton)
                }
            }
        )
    }
}

// ============================================================================
// 4. BROWSING HISTORY SHEET
// ============================================================================

private data class HistoryDayGroup(
    val headerLabel: String,
    val items: List<HistoryEntity>,
    val visitCountsByUrl: Map<String, Int>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    history: List<HistoryEntity>,
    onOpenUrl: (String) -> Unit,
    onDeleteHistoryItem: (Long) -> Unit,
    onOpenClearDataDialog: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalNovaStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val palette = rememberSheetPalette()
    var searchQuery by remember { mutableStateOf("") }

    val filteredHistory = remember(history, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            history
        } else {
            history.filter {
                it.title.contains(q, ignoreCase = true) || it.url.contains(q, ignoreCase = true)
            }
        }
    }

    val todayCount = remember(history) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis
        history.count { it.visitedAt >= startOfToday }
    }

    val groupedHistory = remember(filteredHistory, strings) {
        val nowCal = Calendar.getInstance()
        val todayStart = (nowCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val yesterdayStart = todayStart - 24L * 60L * 60L * 1000L
        val dateFormat = SimpleDateFormat("MMM d", Locale.US)

        val buckets = LinkedHashMap<String, MutableList<HistoryEntity>>()
        for (entry in filteredHistory) {
            val dateStr = dateFormat.format(Date(entry.visitedAt)).uppercase(Locale.US)
            val prefix = when {
                entry.visitedAt >= todayStart -> "${strings.historyTodayPrefix} • $dateStr"
                entry.visitedAt >= yesterdayStart -> "${strings.historyYesterdayPrefix} • $dateStr"
                else -> dateStr
            }
            buckets.getOrPut(prefix) { mutableListOf() }.add(entry)
        }

        buckets.map { (header, entries) ->
            val counts = entries.groupingBy { it.url }.eachCount()
            HistoryDayGroup(
                headerLabel = header,
                items = entries,
                visitCountsByUrl = counts
            )
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.sheetBg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("history_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SheetHeaderIconBadge(icon = NovaIcons.History, palette = palette)
                    Column {
                        Text(
                            text = strings.browsingHistoryTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = palette.textPrimary
                        )
                        Text(
                            text = strings.historyVisitsStats(history.size, todayCount),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.8.sp
                            ),
                            color = palette.textSecondary
                        )
                    }
                }

                // Red-tinted "🗑 Clear all" pill button
                Surface(
                    onClick = onOpenClearDataDialog,
                    shape = RoundedCornerShape(14.dp),
                    color = palette.dangerBg,
                    border = BorderStroke(1.dp, palette.dangerBorder),
                    modifier = Modifier.testTag("clear_browsing_data_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = NovaIcons.DeleteOutline,
                            contentDescription = null,
                            tint = palette.dangerRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = strings.clearAllButton,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.dangerRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search history bar
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = palette.cardBg,
                border = BorderStroke(1.dp, palette.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = NovaIcons.Search,
                        contentDescription = null,
                        tint = palette.accentPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = strings.searchHistoryPlaceholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textMuted
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = palette.textPrimary,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(palette.accentPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("history_search_input")
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = NovaIcons.Close,
                            contentDescription = "Clear search",
                            tint = palette.textSecondary,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (groupedHistory.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = palette.cardBg,
                    border = BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SheetHeaderIconBadge(icon = NovaIcons.History, palette = palette)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) strings.noMatchingVisitsTitle else strings.noHistoryTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.noHistorySubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 430.dp)
                ) {
                    items(groupedHistory, key = { it.headerLabel }) { group ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Date header row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = group.headerLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = palette.textSecondary
                                )
                                Text(
                                    text = strings.historyVisitsLabel(group.items.size),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = palette.textMuted
                                )
                            }

                            // Unified container card for the day's history entries
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = palette.cardBg,
                                border = BorderStroke(1.dp, palette.cardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    group.items.forEachIndexed { idx, entry ->
                                        val host = remember(entry.url) { extractHostClean(entry.url) }
                                        val repeatCount = group.visitCountsByUrl[entry.url] ?: 1

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onOpenUrl(entry.url) }
                                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                                .testTag("history_item_${entry.id}"),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            MonogramSiteBadge(
                                                label = host.ifBlank { entry.title },
                                                seed = host,
                                                url = entry.url
                                            )

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = entry.title.ifBlank { host },
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        color = palette.textPrimary,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                    if (repeatCount > 1) {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = palette.accentPrimary.copy(alpha = 0.18f),
                                                            border = BorderStroke(
                                                                1.dp,
                                                                palette.accentPrimary.copy(alpha = 0.55f)
                                                            )
                                                        ) {
                                                            Text(
                                                                text = "${repeatCount}×",
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    fontFamily = FontFamily.Monospace,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 10.sp
                                                                ),
                                                                color = palette.accentPrimary,
                                                                modifier = Modifier.padding(
                                                                    horizontal = 6.dp,
                                                                    vertical = 2.dp
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "$host • ${timeFormat.format(Date(entry.visitedAt))}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontFamily = FontFamily.Monospace
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = palette.textSecondary
                                                )
                                            }

                                            IconButton(
                                                onClick = { onDeleteHistoryItem(entry.id) },
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .testTag("delete_history_${entry.id}")
                                            ) {
                                                Icon(
                                                    imageVector = NovaIcons.Close,
                                                    contentDescription = strings.deleteAction,
                                                    tint = palette.textSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        if (idx < group.items.lastIndex) {
                                            HorizontalDivider(
                                                color = palette.cardBorder.copy(alpha = 0.55f),
                                                modifier = Modifier.padding(horizontal = 14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 5. BROWSER MENU SHEET — "Control center"
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserMenuSheet(
    activeTab: BrowserTab,
    isBookmarked: Boolean,
    isAdBlockEnabled: Boolean,
    adBlockMode: AdBlockMode,
    appLanguage: AppLanguage,
    isDarkMode: Boolean,
    totalAdsBlocked: Long,
    blockedAdsInSessionCount: Int,
    onToggleBookmark: () -> Unit,
    onToggleAdBlock: () -> Unit,
    onSelectAdBlockMode: (AdBlockMode) -> Unit,
    onSelectAppLanguage: (AppLanguage) -> Unit,
    onOpenBlockedAdsList: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onToggleIncognito: () -> Unit,
    onUpdateTextZoom: (Int) -> Unit,
    onOpenFindInPage: () -> Unit,
    onOpenDevTools: () -> Unit,
    onScanPageVideos: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onSharePage: () -> Unit,
    onCopyPageLink: () -> Unit,
    onOpenClearData: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalNovaStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val palette = rememberSheetPalette(isIncognito = activeTab.isIncognito)
    val minimalTotal = remember { AdBlocker.getTotalRuleCount(AdBlockMode.MINIMAL) }
    val proTotal = remember { AdBlocker.getTotalRuleCount(AdBlockMode.PRO) }
    val maxTotal = remember { AdBlocker.getTotalRuleCount(AdBlockMode.MAX) }

    val activeModeLabel = when (adBlockMode) {
        AdBlockMode.MINIMAL -> strings.modeMinimalTitle
        AdBlockMode.PRO -> strings.modeProTitle
        AdBlockMode.MAX -> strings.modeMaxTitle
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.sheetBg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("browser_menu_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Header: Tune badge + "Control center" + "MINIMAL SHIELD • 242 BLOCKED" + Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    SheetHeaderIconBadge(icon = NovaIcons.Tune, palette = palette)
                    Column {
                        Text(
                            text = strings.controlCenterTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = palette.textPrimary
                        )
                        Text(
                            text = if (isAdBlockEnabled) {
                                strings.shieldHeaderActive(activeModeLabel.uppercase(Locale.ROOT), totalAdsBlocked)
                            } else {
                                strings.shieldHeaderPaused(totalAdsBlocked)
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.8.sp
                            ),
                            color = palette.textSecondary
                        )
                    }
                }

                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    color = palette.cardBg,
                    border = BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = NovaIcons.Close,
                            contentDescription = "Close",
                            tint = palette.accentPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 5 Quick Action Squircles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ControlCenterQuickTile(
                    icon = NovaIcons.BookmarkBorder,
                    label = strings.quickActionBookmarks,
                    palette = palette,
                    onClick = onOpenBookmarks,
                    modifier = Modifier.testTag("menu_bookmarks_button")
                )
                ControlCenterQuickTile(
                    icon = NovaIcons.Download,
                    label = strings.quickActionDownloads,
                    palette = palette,
                    onClick = onOpenDownloads,
                    modifier = Modifier.testTag("menu_downloads_button")
                )
                ControlCenterQuickTile(
                    icon = NovaIcons.History,
                    label = strings.quickActionHistory,
                    palette = palette,
                    onClick = onOpenHistory,
                    modifier = Modifier.testTag("menu_history_button")
                )
                ControlCenterQuickTile(
                    icon = NovaIcons.Share,
                    label = strings.quickActionShare,
                    palette = palette,
                    onClick = onSharePage,
                    modifier = Modifier.testTag("menu_share_button")
                )
                ControlCenterQuickTile(
                    icon = NovaIcons.ContentCopy,
                    label = strings.quickActionCopyLink,
                    palette = palette,
                    onClick = onCopyPageLink,
                    modifier = Modifier.testTag("menu_copy_link_button")
                )
            }

            HorizontalDivider(color = palette.cardBorder.copy(alpha = 0.7f))

            // Ad-block shield row with switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = NovaIcons.ShieldOutline,
                        contentDescription = null,
                        tint = palette.accentPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = strings.adBlockShieldMenuTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.textPrimary
                        )
                        Text(
                            text = "${activeModeLabel.uppercase(Locale.ROOT)} • $totalAdsBlocked",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.6.sp
                            ),
                            color = palette.textSecondary
                        )
                    }
                }
                Switch(
                    checked = isAdBlockEnabled,
                    onCheckedChange = { onToggleAdBlock() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = palette.accentPrimary
                    ),
                    modifier = Modifier.testTag("menu_adblock_switch")
                )
            }

            // 3 Mode Cards: Minimal (58 RULES), Pro (235), Max (267)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    Triple(AdBlockMode.MINIMAL, strings.modeMinimalTitle to strings.rulesCountSuffix(minimalTotal), "menu_adblock_mode_minimal"),
                    Triple(AdBlockMode.PRO, strings.modeProTitle to "$proTotal", "menu_adblock_mode_pro"),
                    Triple(AdBlockMode.MAX, strings.modeMaxTitle to "$maxTotal", "menu_adblock_mode_max")
                ).forEach { (mode, titleAndSub, tag) ->
                    val (modeTitle, countSubtitle) = titleAndSub
                    val isSelected = isAdBlockEnabled && adBlockMode == mode
                    Surface(
                        onClick = { onSelectAdBlockMode(mode) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) palette.activePillBg else palette.cardBg,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) palette.activePillBorder else palette.cardBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag(tag)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = modeTitle,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) palette.accentPrimary else palette.textPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = countSubtitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) palette.textPrimary.copy(alpha = 0.85f) else palette.textMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // "Filter modes / Inspect every active rule" card with "Configure" gradient button
            Surface(
                onClick = onOpenBlockedAdsList,
                shape = RoundedCornerShape(18.dp),
                color = palette.cardBg,
                border = BorderStroke(1.dp, palette.cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("menu_view_blocked_ads_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        SheetHeaderIconBadge(
                            icon = NovaIcons.ListAlt,
                            palette = palette,
                            modifier = Modifier.size(38.dp)
                        )
                        Column {
                            Text(
                                text = strings.filterModesTitle,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = palette.textPrimary
                            )
                            Text(
                                text = strings.filterModesSubtitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = palette.textSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.accentGradient)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.configureButton,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            HorizontalDivider(color = palette.cardBorder.copy(alpha = 0.7f))

            // Desktop site toggle row
            ControlCenterSwitchRow(
                icon = NovaIcons.DesktopWindows,
                title = strings.desktopSiteMode,
                subtitle = null,
                checked = activeTab.isDesktopMode,
                palette = palette,
                onCheckedChange = { onToggleDesktopMode() },
                testTag = "menu_desktop_mode_switch"
            )

            // Dark theme toggle row
            ControlCenterSwitchRow(
                icon = NovaIcons.DarkMode,
                title = strings.darkModeTheme,
                subtitle = null,
                checked = isDarkMode,
                palette = palette,
                onCheckedChange = { onToggleDarkMode() },
                testTag = "menu_dark_mode_switch"
            )

            // Incognito toggle row
            ControlCenterSwitchRow(
                icon = NovaIcons.Incognito,
                title = strings.incognitoLabel,
                subtitle = strings.nothingIsSavedSubtitle,
                checked = activeTab.isIncognito,
                palette = palette,
                onCheckedChange = { onToggleIncognito() },
                testTag = "menu_incognito_switch"
            )

            // Text zoom row with -, 100%, +, and Reset button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = NovaIcons.TextFields,
                        contentDescription = null,
                        tint = palette.accentPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = strings.textZoomLabel,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.textPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = { onUpdateTextZoom(activeTab.textZoom - 10) },
                        shape = RoundedCornerShape(10.dp),
                        color = palette.cardBg,
                        border = BorderStroke(1.dp, palette.cardBorder),
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("text_zoom_decrease")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = NovaIcons.Remove,
                                contentDescription = "Decrease Text Size",
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "${activeTab.textZoom}%",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = palette.textPrimary,
                        modifier = Modifier.widthIn(min = 44.dp),
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        onClick = { onUpdateTextZoom(activeTab.textZoom + 10) },
                        shape = RoundedCornerShape(10.dp),
                        color = palette.cardBg,
                        border = BorderStroke(1.dp, palette.cardBorder),
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("text_zoom_increase")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = NovaIcons.Add,
                                contentDescription = "Increase Text Size",
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = { onUpdateTextZoom(100) },
                        shape = RoundedCornerShape(10.dp),
                        color = palette.cardBg,
                        border = BorderStroke(1.dp, palette.cardBorder),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("text_zoom_reset")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = strings.resetZoomButton,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.accentPrimary
                            )
                        }
                    }
                }
            }

            // Language section (System, English, Hindi)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = NovaIcons.Translate,
                        contentDescription = null,
                        tint = palette.accentPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.languageSectionTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.textPrimary
                    )
                }

                val langOptions: List<Triple<AppLanguage, String, String>> = listOf(
                    Triple(AppLanguage.SYSTEM, strings.languageSystemOption, "menu_language_system"),
                    Triple(AppLanguage.ENGLISH, strings.languageEnglishOption, "menu_language_en"),
                    Triple(AppLanguage.HINDI, strings.languageHindiOption, "menu_language_hi")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    langOptions.forEach { (lang, label, tag) ->
                        val isSelected = appLanguage == lang
                        Surface(
                            onClick = { onSelectAppLanguage(lang) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) palette.activePillBg else palette.cardBg,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) palette.activePillBorder else palette.cardBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag(tag)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) palette.accentPrimary else palette.textPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 9.dp, horizontal = 6.dp)
                            )
                        }
                    }
                }
            }

            // Active Webpage Tools row (Bookmark, DevTools, Sniff Videos) when browsing a site
            if (!activeTab.isHomePage) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = onToggleBookmark,
                        shape = RoundedCornerShape(12.dp),
                        color = if (isBookmarked) palette.activePillBg else palette.cardBg,
                        border = BorderStroke(1.dp, if (isBookmarked) palette.activePillBorder else palette.cardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("menu_bookmark_toggle_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) NovaIcons.Bookmark else NovaIcons.BookmarkBorder,
                                contentDescription = null,
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isBookmarked) strings.savedButton else strings.bookmarkButton,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = palette.textPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        onClick = onOpenDevTools,
                        shape = RoundedCornerShape(12.dp),
                        color = palette.cardBg,
                        border = BorderStroke(1.dp, palette.cardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("menu_devtools_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = NovaIcons.Code,
                                contentDescription = null,
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = strings.inspectButton,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = palette.textPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        onClick = onScanPageVideos,
                        shape = RoundedCornerShape(12.dp),
                        color = palette.cardBg,
                        border = BorderStroke(1.dp, palette.cardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("menu_scan_videos_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = NovaIcons.Videocam,
                                contentDescription = null,
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = strings.videosButton,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = palette.textPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Bottom 2-button row: "🔍 Find in page" + "🗑 Clear data"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = onOpenFindInPage,
                    shape = RoundedCornerShape(16.dp),
                    color = palette.cardBg,
                    border = BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("menu_find_in_page_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = NovaIcons.Search,
                            contentDescription = null,
                            tint = palette.accentPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.findInPageButton,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.textPrimary
                        )
                    }
                }

                Surface(
                    onClick = onOpenClearData,
                    shape = RoundedCornerShape(16.dp),
                    color = palette.dangerBg,
                    border = BorderStroke(1.dp, palette.dangerBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("menu_clear_data_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = NovaIcons.DeleteOutline,
                            contentDescription = null,
                            tint = palette.dangerRed,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.clearDataButton,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.dangerRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlCenterQuickTile(
    icon: ImageVector,
    label: String,
    palette: SheetVisualPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(palette.cardElevatedBg)
                .border(1.dp, palette.cardBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = palette.accentPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                letterSpacing = 0.4.sp
            ),
            color = palette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ControlCenterSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    checked: Boolean,
    palette: SheetVisualPalette,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.accentPrimary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.textPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            letterSpacing = 0.6.sp
                        ),
                        color = palette.textMuted
                    )
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = palette.accentPrimary
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

// ============================================================================
// 6. CLEAR BROWSING DATA DIALOG & ADD SHORTCUT DIALOG
// ============================================================================

@Composable
fun ClearBrowsingDataDialog(
    onConfirmClear: (clearHistory: Boolean, clearCookies: Boolean, clearCache: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalNovaStrings.current
    var clearHistory by remember { mutableStateOf(true) }
    var clearCookies by remember { mutableStateOf(true) }
    var clearCache by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = NovaIcons.DeleteOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = strings.clearDataDialogTitle,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = strings.clearDataDialogSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { clearHistory = !clearHistory }
                ) {
                    Checkbox(checked = clearHistory, onCheckedChange = { clearHistory = it })
                    Text(strings.clearHistoryCheckbox)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { clearCookies = !clearCookies }
                ) {
                    Checkbox(checked = clearCookies, onCheckedChange = { clearCookies = it })
                    Text(strings.clearCookiesCheckbox)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { clearCache = !clearCache }
                ) {
                    Checkbox(checked = clearCache, onCheckedChange = { clearCache = it })
                    Text(strings.clearCacheCheckbox)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmClear(clearHistory, clearCookies, clearCache) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_clear_data_button")
            ) {
                Text(strings.clearNowButton)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelButton)
            }
        }
    )
}

@Composable
fun AddShortcutDialog(
    onConfirmAdd: (title: String, url: String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalNovaStrings.current
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("https://") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.addShortcutDialogTitle,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.siteTitleLabel) },
                    placeholder = { Text("e.g. GitHub") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shortcut_title_input")
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(strings.websiteUrlLabel) },
                    placeholder = { Text("https://github.com") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shortcut_url_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmAdd(title, url) },
                modifier = Modifier.testTag("confirm_add_shortcut_button")
            ) {
                Text(strings.addButton)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelButton)
            }
        }
    )
}

// ============================================================================
// 7. SSL SECURITY & SITE CONFIG INSPECTOR SHEET
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SslSecurityInfoDialog(
    tab: BrowserTab,
    certificateDetails: SslCertificateDetails?,
    siteCookies: List<Pair<String, String>>,
    isAdBlockEnabled: Boolean,
    isDarkMode: Boolean,
    onToggleAdBlock: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onCopyText: (String, String) -> Unit,
    onClearSiteCookies: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalNovaStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val palette = rememberSheetPalette(isIncognito = tab.isIncognito)
    var selectedSection by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.sheetBg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("ssl_security_info_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 380.dp, max = 600.dp)
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SheetHeaderIconBadge(
                    icon = if (tab.isSecureSsl) NovaIcons.Lock else NovaIcons.Warning,
                    palette = palette
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (tab.isSecureSsl) "Connection is Secure" else "Connection Not Secure",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.textPrimary
                    )
                    Text(
                        text = extractHostClean(tab.url),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = palette.textSecondary
                    )
                }
                IconButton(
                    onClick = { onCopyText("Website URL", tab.url) },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = NovaIcons.ContentCopy,
                        contentDescription = "Copy Full URL",
                        tint = palette.accentPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DownloadFilterPill(
                    label = "Certificate",
                    selected = selectedSection == 0,
                    palette = palette,
                    onClick = { selectedSection = 0 },
                    modifier = Modifier.testTag("lock_tab_certificate")
                )
                DownloadFilterPill(
                    label = "Cookies (${siteCookies.size})",
                    selected = selectedSection == 1,
                    palette = palette,
                    onClick = { selectedSection = 1 },
                    modifier = Modifier.testTag("lock_tab_cookies")
                )
                DownloadFilterPill(
                    label = "Site Config",
                    selected = selectedSection == 2,
                    palette = palette,
                    onClick = { selectedSection = 2 },
                    modifier = Modifier.testTag("lock_tab_site_config")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedSection) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (certificateDetails != null) {
                            CertificateFieldBlock("Issued To (Common Name)", certificateDetails.issuedToCommonName, palette)
                            CertificateFieldBlock("Issued To Organization", certificateDetails.issuedToOrganization, palette)
                            CertificateFieldBlock("Issued By (Authority)", certificateDetails.issuedByCommonName, palette)
                            CertificateFieldBlock("Valid From", certificateDetails.validFrom, palette)
                            CertificateFieldBlock("Expires On", certificateDetails.validUntil, palette)
                            CertificateFieldBlock("SHA-256 Fingerprint", certificateDetails.sha256Fingerprint, palette, monospace = true)
                        } else {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = palette.cardBg,
                                border = BorderStroke(1.dp, palette.cardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No X.509 SSL certificate is attached to this page.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textSecondary,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }

                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${siteCookies.size} cookies stored",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.textSecondary
                            )
                            if (siteCookies.isNotEmpty()) {
                                TextButton(
                                    onClick = onClearSiteCookies,
                                    modifier = Modifier.testTag("clear_site_cookies_button")
                                ) {
                                    Text("Clear Site Cookies", color = palette.dangerRed)
                                }
                            }
                        }

                        if (siteCookies.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No cookies stored for this domain.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.textSecondary
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                itemsIndexed(siteCookies, key = { idx, pair -> "${pair.first}_$idx" }) { _, (cookieName, cookieVal) ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = palette.cardBg,
                                        border = BorderStroke(1.dp, palette.cardBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = cookieName,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = palette.accentPrimary
                                                )
                                                SelectionContainer {
                                                    Text(
                                                        text = cookieVal.ifBlank { "(empty)" },
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontFamily = FontFamily.Monospace,
                                                            fontSize = 11.sp
                                                        ),
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis,
                                                        color = palette.textPrimary
                                                    )
                                                }
                                            }
                                            IconButton(
                                                onClick = { onCopyText(cookieName, "$cookieName=$cookieVal") },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = NovaIcons.ContentCopy,
                                                    contentDescription = "Copy Cookie",
                                                    tint = palette.accentPrimary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ControlCenterSwitchRow(
                            icon = NovaIcons.ShieldOutline,
                            title = strings.adBlockShieldMenuTitle,
                            subtitle = "BLOCKS ADS & TELEMETRY",
                            checked = isAdBlockEnabled,
                            palette = palette,
                            onCheckedChange = { onToggleAdBlock() },
                            testTag = "ssl_adblock_switch"
                        )
                        ControlCenterSwitchRow(
                            icon = NovaIcons.DesktopWindows,
                            title = strings.desktopSiteMode,
                            subtitle = "DESKTOP USER-AGENT",
                            checked = tab.isDesktopMode,
                            palette = palette,
                            onCheckedChange = { onToggleDesktopMode() },
                            testTag = "ssl_desktop_switch"
                        )
                        ControlCenterSwitchRow(
                            icon = NovaIcons.DarkMode,
                            title = strings.darkModeTheme,
                            subtitle = "DARK WEB PALETTE",
                            checked = isDarkMode,
                            palette = palette,
                            onCheckedChange = { onToggleDarkMode() },
                            testTag = "ssl_dark_switch"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CertificateFieldBlock(
    label: String,
    value: String,
    palette: SheetVisualPalette,
    monospace: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = palette.cardBg,
        border = BorderStroke(1.dp, palette.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = palette.textSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            SelectionContainer {
                Text(
                    text = value.ifBlank { "Unspecified" },
                    style = if (monospace) {
                        MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    } else {
                        MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    },
                    color = palette.textPrimary
                )
            }
        }
    }
}

// ============================================================================
// 8. AD-BLOCK SHIELD SHEET
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedAdsSheet(
    blockedAds: List<BlockedAdEntry>,
    isAdBlockEnabled: Boolean,
    adBlockMode: AdBlockMode,
    totalAdsBlocked: Long,
    activeTabId: String,
    onToggleAdBlock: () -> Unit,
    onSelectAdBlockMode: (AdBlockMode) -> Unit,
    onCopyAdUrl: (String) -> Unit,
    onClearLog: () -> Unit,
    onDismiss: () -> Unit,
    currentSiteUrl: String = "https://www.google.com"
) {
    val strings = LocalNovaStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val palette = rememberSheetPalette()

    // 0 = All, 1 = Current Tab, 2 = Ads, 3 = Trackers, 4 = Active Filter Rules
    var selectedFilter by remember { mutableIntStateOf(0) }
    var customRulesList by remember { mutableStateOf(listOf<String>()) }
    var showAddRuleDialog by remember { mutableStateOf(false) }
    var newCustomRuleInput by remember { mutableStateOf("") }

    val (hostCount, pathCount, cosmeticCount) = remember(adBlockMode) {
        AdBlocker.getRuleCountsForMode(adBlockMode)
    }
    val activeRules: List<AdFilterRuleItem> = remember(adBlockMode, customRulesList) {
        val base = AdBlocker.getRulesCatalogForMode(adBlockMode)
        val extra = customRulesList.map { rule ->
            AdFilterRuleItem(
                rule = rule,
                category = "Custom Rule",
                tier = adBlockMode
            )
        }
        extra + base
    }
    val minimalTotal = remember { AdBlocker.getTotalRuleCount(AdBlockMode.MINIMAL) }
    val proTotal = remember { AdBlocker.getTotalRuleCount(AdBlockMode.PRO) }
    val maxTotal = remember { AdBlocker.getTotalRuleCount(AdBlockMode.MAX) }

    val filteredEntries = remember(blockedAds, selectedFilter, activeTabId) {
        when (selectedFilter) {
            1 -> blockedAds.filter { it.tabId == activeTabId }
            2 -> blockedAds.filter { !it.category.contains("Tracker", ignoreCase = true) && !it.category.contains("Analytics", ignoreCase = true) }
            3 -> blockedAds.filter { it.category.contains("Tracker", ignoreCase = true) || it.category.contains("Analytics", ignoreCase = true) || it.category.contains("Pixel", ignoreCase = true) }
            else -> blockedAds
        }
    }

    val currentTabBlockedCount = remember(blockedAds, activeTabId) {
        blockedAds.count { it.tabId == activeTabId }
    }
    val currentSiteHost: String = remember(currentSiteUrl, blockedAds, activeTabId) {
        val fromUrl = extractHostClean(currentSiteUrl)
        if (fromUrl.isNotBlank() && !fromUrl.startsWith("nova:")) {
            fromUrl
        } else {
            val fallbackDomain = blockedAds.firstOrNull { it.tabId == activeTabId }?.pageDomain.orEmpty()
            if (fallbackDomain.isNotBlank()) fallbackDomain else "google.com"
        }
    }

    val activeModeLabel = when (adBlockMode) {
        AdBlockMode.MINIMAL -> strings.modeMinimalTitle
        AdBlockMode.PRO -> strings.modeProTitle
        AdBlockMode.MAX -> strings.modeMaxTitle
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.sheetBg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("blocked_ads_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
        ) {
            // Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    SheetHeaderIconBadge(icon = NovaIcons.ShieldCheck, palette = palette)
                    Column {
                        Text(
                            text = strings.adBlockShieldMenuTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = palette.textPrimary
                        )
                        Text(
                            text = if (isAdBlockEnabled) {
                                strings.adShieldHeaderActive(totalAdsBlocked, activeRules.size)
                            } else {
                                strings.adShieldHeaderDisabled
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.7.sp
                            ),
                            color = palette.textSecondary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (blockedAds.isNotEmpty()) {
                        IconButton(
                            onClick = onClearLog,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("clear_blocked_ads_log_button")
                        ) {
                            Icon(
                                imageVector = NovaIcons.DeleteOutline,
                                contentDescription = strings.clearAllButton,
                                tint = palette.dangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Switch(
                        checked = isAdBlockEnabled,
                        onCheckedChange = { onToggleAdBlock() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = palette.accentPrimary
                        ),
                        modifier = Modifier.testTag("blocked_ads_sheet_toggle")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3 Mode Cards: Minimal ON (58 FILTERS • FAST), Pro (235 FILTERS), Max (267 FILTERS)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple(AdBlockMode.MINIMAL, strings.modeMinimalTitle to strings.filtersCountFast(minimalTotal), "adblock_mode_card_minimal"),
                    Triple(AdBlockMode.PRO, strings.modeProTitle to strings.filtersCountOnly(proTotal), "adblock_mode_card_pro"),
                    Triple(AdBlockMode.MAX, strings.modeMaxTitle to strings.filtersCountOnly(maxTotal), "adblock_mode_card_max")
                ).forEach { (mode, titleAndSub, tag) ->
                    val (modeTitle, subtitle) = titleAndSub
                    val isSelected = isAdBlockEnabled && adBlockMode == mode
                    Surface(
                        onClick = { onSelectAdBlockMode(mode) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) palette.activePillBg else palette.cardBg,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) palette.activePillBorder else palette.cardBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag(tag)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = modeTitle,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) palette.accentPrimary else palette.textPrimary
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Surface(
                                        shape = RoundedCornerShape(5.dp),
                                        color = palette.accentPrimary
                                    ) {
                                        Text(
                                            text = "ON",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 9.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                ),
                                color = if (isSelected) palette.textPrimary.copy(alpha = 0.86f) else palette.textMuted,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Breakdown line
            Text(
                text = strings.filterBreakdownLine(hostCount, pathCount, cosmeticCount),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                ),
                color = palette.textSecondary,
                modifier = Modifier.padding(horizontal = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Pills Row: All (0), Tab (0), Ads, Trackers, Rules (58)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DownloadFilterPill(
                    label = "${strings.filterChipAll} (${blockedAds.size})",
                    selected = selectedFilter == 0,
                    palette = palette,
                    onClick = { selectedFilter = 0 }
                )
                DownloadFilterPill(
                    label = "${strings.filterChipTab} ($currentTabBlockedCount)",
                    selected = selectedFilter == 1,
                    palette = palette,
                    onClick = { selectedFilter = 1 }
                )
                DownloadFilterPill(
                    label = strings.filterChipAds,
                    selected = selectedFilter == 2,
                    palette = palette,
                    onClick = { selectedFilter = 2 }
                )
                DownloadFilterPill(
                    label = strings.filterChipTrackers,
                    selected = selectedFilter == 3,
                    palette = palette,
                    onClick = { selectedFilter = 3 }
                )
                DownloadFilterPill(
                    label = "${strings.filterChipRules} (${activeRules.size})",
                    selected = selectedFilter == 4,
                    palette = palette,
                    onClick = { selectedFilter = 4 },
                    modifier = Modifier.testTag("blocked_ads_rules_chip")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Current site status card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = palette.cardBg,
                border = BorderStroke(1.dp, palette.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        MonogramSiteBadge(
                            label = currentSiteHost,
                            seed = currentSiteHost,
                            url = currentSiteUrl
                        )
                        Column {
                            Text(
                                text = currentSiteHost,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = strings.thisPageBlockedCount(currentTabBlockedCount),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = palette.textSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = palette.cardElevatedBg,
                        border = BorderStroke(1.dp, palette.cardBorder)
                    ) {
                        Text(
                            text = if (currentTabBlockedCount == 0) strings.siteCleanBadge else strings.siteProtectedBadge,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.accentPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedFilter == 4) {
                // Active Rules list view
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 280.dp)
                        .testTag("active_filter_rules_list")
                ) {
                    itemsIndexed(activeRules, key = { idx, item -> "${item.tier.name}_${item.category}_${idx}_${item.rule}" }) { _, ruleItem ->
                        Surface(
                            onClick = { onCopyAdUrl(ruleItem.rule) },
                            shape = RoundedCornerShape(14.dp),
                            color = palette.cardBg,
                            border = BorderStroke(1.dp, palette.cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = palette.activePillBg
                                        ) {
                                            Text(
                                                text = ruleItem.tier.shortBadge.uppercase(Locale.US),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp
                                                ),
                                                color = palette.accentPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = ruleItem.category,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = palette.textSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = ruleItem.rule,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = palette.textPrimary
                                    )
                                }
                                Icon(
                                    imageVector = NovaIcons.ContentCopy,
                                    contentDescription = "Copy Rule",
                                    tint = palette.accentPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            } else if (filteredEntries.isEmpty()) {
                // Summary / Empty state card
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = palette.cardBg,
                    border = BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SheetHeaderIconBadge(icon = NovaIcons.Block, palette = palette)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isAdBlockEnabled) {
                                strings.shieldActiveEmptyTitle(activeModeLabel, activeRules.size)
                            } else {
                                strings.shieldDisabledEmptyTitle
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when (adBlockMode) {
                                AdBlockMode.MINIMAL -> strings.shieldMinimalEmptyDesc(activeRules.size)
                                AdBlockMode.PRO -> strings.shieldProEmptyDesc(activeRules.size)
                                AdBlockMode.MAX -> strings.shieldMaxEmptyDesc(activeRules.size)
                            },
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = palette.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 280.dp)
                ) {
                    items(filteredEntries, key = { it.id }) { item ->
                        Surface(
                            onClick = { onCopyAdUrl(item.adUrl) },
                            shape = RoundedCornerShape(16.dp),
                            color = palette.cardBg,
                            border = BorderStroke(1.dp, palette.cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("blocked_ad_item_${item.id}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.category} • ${item.filterRule}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = palette.accentPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = timeFormatter.format(Date(item.timestamp)),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp
                                        ),
                                        color = palette.textMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.adUrl,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = palette.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom 2-button row: "Pause on this site" + "Custom rules"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = onToggleAdBlock,
                    shape = RoundedCornerShape(16.dp),
                    color = palette.cardBg,
                    border = BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("adblock_pause_site_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAdBlockEnabled) strings.pauseOnSiteButton else strings.resumeOnSiteButton,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.textPrimary
                        )
                    }
                }

                Surface(
                    onClick = {
                        if (selectedFilter == 4) {
                            showAddRuleDialog = true
                        } else {
                            selectedFilter = 4
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = palette.cardBg,
                    border = BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("adblock_custom_rules_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedFilter == 4) strings.addCustomRuleButton else strings.customRulesButton,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.accentPrimary
                        )
                    }
                }
            }
        }
    }

    if (showAddRuleDialog) {
        AlertDialog(
            onDismissRequest = { showAddRuleDialog = false },
            title = { Text(strings.addCustomRuleButton, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newCustomRuleInput,
                    onValueChange = { newCustomRuleInput = it },
                    label = { Text("Host, path, or CSS selector") },
                    placeholder = { Text("e.g. ads.example.com or .banner-slot") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rule = newCustomRuleInput.trim()
                        if (rule.isNotEmpty()) {
                            customRulesList = listOf(rule) + customRulesList
                            newCustomRuleInput = ""
                            showAddRuleDialog = false
                        }
                    }
                ) {
                    Text(strings.addButton)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRuleDialog = false }) {
                    Text(strings.cancelButton)
                }
            }
        )
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
        mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
        else -> String.format(Locale.US, "%.0f KB", kb)
    }
}
