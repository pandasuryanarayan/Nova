package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.MaterialTheme
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ============================================================================
// SHARED PALETTE & COMPACT SHEET HELPERS
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
    val isAmoledDark = !isIncognito && scheme.background == Color.Black
    val isDark = isIncognito || isAmoledDark
    val accentPrimary = scheme.primary
    val accentSecondary = scheme.secondary
    val sheetBg = when {
        isIncognito -> Color(0xFF0C1120)
        isAmoledDark -> Color(0xFF050507)
        else -> scheme.background
    }
    val cardBg = when {
        isIncognito -> Color(0xFF131B2E)
        isAmoledDark -> Color(0xFF101014)
        else -> scheme.surface
    }
    val cardElevatedBg = when {
        isIncognito -> Color(0xFF19233B)
        isAmoledDark -> Color(0xFF18181F)
        else -> scheme.surfaceVariant.copy(alpha = 0.65f)
    }
    val cardBorder = when {
        isIncognito -> Color(0xFF273657)
        isAmoledDark -> Color(0xFF282832)
        else -> scheme.outline.copy(alpha = 0.42f)
    }
    return SheetVisualPalette(
        sheetBg = sheetBg,
        cardBg = cardBg,
        cardElevatedBg = cardElevatedBg,
        cardBorder = cardBorder,
        badgeBg = Brush.linearGradient(
            listOf(accentPrimary.copy(alpha = 0.22f), accentSecondary.copy(alpha = 0.14f))
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
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(palette.badgeBg)
            .border(1.dp, palette.badgeBorder, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = palette.accentPrimary, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun MonogramSiteBadge(
    label: String,
    url: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cachedIcon = if (!url.isNullOrBlank()) rememberShortcutIcon(context = context, url = url).value else null
    val accentPrimary = MaterialTheme.colorScheme.primary
    val letter = label.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "N"

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (cachedIcon != null) Color.White else accentPrimary.copy(alpha = 0.15f))
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
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                color = accentPrimary
            )
        }
    }
}

@Composable
private fun SheetCloseButton(palette: SheetVisualPalette, onDismiss: () -> Unit) {
    Surface(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
        color = palette.cardBg,
        border = BorderStroke(1.dp, palette.cardBorder),
        modifier = Modifier.size(38.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(NovaIcons.Close, "Close", tint = palette.accentPrimary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun FilterPill(
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
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) palette.activePillBorder else palette.cardBorder),
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

private fun extractHostClean(url: String): String = try {
    val host = URI(url).host?.removePrefix("www.")
    if (!host.isNullOrBlank()) host else url.removePrefix("https://").removePrefix("http://").substringBefore("/")
} catch (_: Exception) {
    url.removePrefix("https://").removePrefix("http://").substringBefore("/")
}

// ============================================================================
// 1. TABS SWITCHER BOTTOM SHEET
// ============================================================================

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
    val palette = rememberSheetPalette()

    NovaBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.sheetBg,
        modifier = Modifier.testTag("tabs_switcher_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
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
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, letterSpacing = 0.8.sp),
                            color = palette.textSecondary
                        )
                    }
                }
                SheetCloseButton(palette = palette, onDismiss = onDismiss)
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                        Icon(NovaIcons.Incognito, strings.privateTabButton, tint = palette.accentPrimary, modifier = Modifier.size(18.dp))
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
                        Icon(NovaIcons.Add, strings.newTabButton, tint = palette.accentPrimary, modifier = Modifier.size(18.dp))
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

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
            ) {
                val cols = if (maxWidth >= 520.dp) 3 else 2
                val rows = remember(tabs, cols) { tabs.chunked(cols) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rows.forEach { rowTabs ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowTabs.forEach { tab ->
                                val isSelected = tab.id == activeTabId
                                Surface(
                                    onClick = { onSelectTab(tab.id) },
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (isSelected) palette.activePillBg else palette.cardBg,
                                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) palette.activePillBorder else palette.cardBorder),
                                    modifier = Modifier
                                        .weight(1f)
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
                                                    Icon(NovaIcons.ContentCopy, "Duplicate Tab", tint = palette.accentPrimary, modifier = Modifier.size(14.dp))
                                                }
                                                IconButton(
                                                    onClick = { onCloseTab(tab.id) },
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .testTag("close_tab_${tab.id}")
                                                ) {
                                                    Icon(NovaIcons.Close, "Close Tab", tint = palette.textSecondary, modifier = Modifier.size(15.dp))
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(72.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(palette.cardElevatedBg)
                                                .border(1.dp, palette.cardBorder, RoundedCornerShape(12.dp))
                                                .padding(10.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Column {
                                                Text(
                                                    text = if (tab.isHomePage) "nova://start" else extractHostClean(tab.url),
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = palette.textPrimary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = if (tab.isIncognito) strings.stealthTabBadge else strings.standardTabBadge,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                                    color = if (tab.isIncognito) palette.accentPrimary else palette.textMuted
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            repeat(cols - rowTabs.size) {
                                Spacer(modifier = Modifier.weight(1f))
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
    val palette = rememberSheetPalette()

    val initialTab = remember(downloads.size) {
        when {
            downloads.any { it.statusEnum in listOf(DownloadStatus.RUNNING, DownloadStatus.PAUSED, DownloadStatus.PENDING) } -> 0
            downloads.any { it.statusEnum == DownloadStatus.COMPLETED } -> 1
            downloads.any { it.statusEnum == DownloadStatus.FAILED } -> 2
            else -> 1
        }
    }
    var selectedSection by remember { mutableIntStateOf(initialTab) }
    var sortNewestFirst by remember { mutableStateOf(true) }

    val ongoingDownloads = remember(downloads, sortNewestFirst) {
        val list = downloads.filter { it.statusEnum in listOf(DownloadStatus.RUNNING, DownloadStatus.PAUSED, DownloadStatus.PENDING) }
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
    val totalMbFormatted = remember(downloads) {
        val totalBytes = downloads.sumOf { maxOf(it.downloadedBytes, 0L) }
        if (totalBytes <= 0L) "0 MB" else String.format(Locale.US, "%.1f MB", totalBytes / (1024.0 * 1024.0))
    }

    NovaBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.sheetBg,
        modifier = Modifier.testTag("downloads_manager_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SheetHeaderIconBadge(icon = NovaIcons.Download, palette = palette)
                    Column {
                        Text(
                            text = strings.downloadsTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = palette.textPrimary
                        )
                        Text(
                            text = strings.downloadsHeaderStats(downloads.size, totalMbFormatted),
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, letterSpacing = 0.8.sp),
                            color = palette.textSecondary
                        )
                    }
                }

                Surface(
                    onClick = {
                        val firstCompleted = successDownloads.firstOrNull() ?: downloads.firstOrNull()
                        if (firstCompleted != null) onOpenFile(firstCompleted)
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

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "PATH: /Download/NovaBrowser",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, letterSpacing = 0.4.sp),
                color = palette.textMuted
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterPill("${strings.downloadTabSuccess} (${successDownloads.size})", selectedSection == 1, palette, { selectedSection = 1 }, Modifier.testTag("download_tab_success"))
                FilterPill("${strings.downloadTabOngoing} (${ongoingDownloads.size})", selectedSection == 0, palette, { selectedSection = 0 }, Modifier.testTag("download_tab_ongoing"))
                FilterPill("${strings.downloadTabFailure} (${failedDownloads.size})", selectedSection == 2, palette, { selectedSection = 2 }, Modifier.testTag("download_tab_failure"))
                FilterPill(if (sortNewestFirst) strings.sortNewest else strings.sortOldest, false, palette, { sortNewestFirst = !sortNewestFirst }, Modifier.testTag("download_sort_chip"))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (displayedDownloads.isEmpty()) {
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
                                0 -> strings.noOngoingDownloadsSubtitle
                                1 -> strings.noSuccessDownloadsSubtitle
                                else -> strings.noFailureDownloadsSubtitle
                            },
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = palette.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 380.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    displayedDownloads.forEach { item ->
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
private fun DownloadItemCard(
    item: DownloadItemEntity,
    palette: SheetVisualPalette,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalNovaStrings.current
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
                    SheetHeaderIconBadge(icon = NovaIcons.Videocam, palette = palette, modifier = Modifier.size(40.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.fileName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = palette.textPrimary
                        )
                        val sizeLabel = if (item.totalBytes > 0) formatBytes(item.downloadedBytes) else strings.unknownSize
                        val statusText = when (statusEnum) {
                            DownloadStatus.RUNNING -> "${formatBytes(item.downloadedBytes)} / ${if (item.totalBytes > 0) formatBytes(item.totalBytes) else strings.unknownSize}"
                            DownloadStatus.PAUSED -> "${strings.downloadStatusPaused} • $sizeLabel"
                            DownloadStatus.COMPLETED -> "${strings.downloadStatusCompleted} • ${formatBytes(item.downloadedBytes)}"
                            DownloadStatus.FAILED -> strings.downloadStatusFailed
                            DownloadStatus.PENDING -> strings.downloadStatusStarting
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
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
                        DownloadStatus.RUNNING -> IconButton(onClick = onPause, modifier = Modifier.testTag("pause_download_${item.id}")) {
                            Icon(NovaIcons.Pause, "Pause Download", tint = palette.accentPrimary)
                        }
                        DownloadStatus.PAUSED -> IconButton(onClick = onResume, modifier = Modifier.testTag("resume_download_${item.id}")) {
                            Icon(NovaIcons.PlayArrow, "Resume Download", tint = palette.accentPrimary)
                        }
                        DownloadStatus.FAILED -> IconButton(onClick = onResume, modifier = Modifier.testTag("retry_download_${item.id}")) {
                            Icon(NovaIcons.Refresh, "Retry Download", tint = palette.accentPrimary)
                        }
                        DownloadStatus.COMPLETED -> IconButton(onClick = onOpen, modifier = Modifier.testTag("open_download_${item.id}")) {
                            Icon(NovaIcons.FolderOpen, "Open File", tint = palette.accentPrimary)
                        }
                        DownloadStatus.PENDING -> Unit
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_download_${item.id}")) {
                        Icon(NovaIcons.DeleteOutline, "Delete Download", tint = palette.dangerRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (statusEnum == DownloadStatus.RUNNING || statusEnum == DownloadStatus.PAUSED) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { if (item.totalBytes > 0) progress else 0.35f },
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

// ============================================================================
// 3. BOOKMARKS SHEET (No Long-Press Hint Card)
// ============================================================================

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
    val palette = rememberSheetPalette()
    var isEditingMode by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newUrl by remember { mutableStateOf("https://") }

    NovaBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.sheetBg,
        modifier = Modifier.testTag("bookmarks_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SheetHeaderIconBadge(icon = NovaIcons.BookmarkBorder, palette = palette)
                    Column {
                        Text(
                            text = strings.bookmarksTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = palette.textPrimary
                        )
                        Text(
                            text = strings.bookmarksHeaderStats(bookmarks.size),
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, letterSpacing = 0.8.sp),
                            color = palette.textSecondary
                        )
                    }
                }

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
                        Icon(NovaIcons.Edit, strings.editAction, tint = palette.accentPrimary, modifier = Modifier.size(15.dp))
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    bookmarks.forEach { bookmark ->
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
                                MonogramSiteBadge(label = bookmark.title.ifBlank { cleanUrl }, url = bookmark.url)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bookmark.title,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, lineHeight = 20.sp),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = palette.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = cleanUrl,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
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
                                    Icon(NovaIcons.DeleteOutline, strings.deleteAction, tint = palette.dangerRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

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
                        Icon(NovaIcons.Add, null, tint = palette.accentPrimary, modifier = Modifier.size(18.dp))
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
                            if (onOpenAllBookmarks != null) onOpenAllBookmarks(bookmarks.map { it.url })
                            else onOpenUrl(bookmarks.first().url)
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
                            text = strings.openAllButton,
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
                    NovaEditText(value = newTitle, onValueChange = { newTitle = it }, label = strings.siteTitleLabel, modifier = Modifier.fillMaxWidth())
                    NovaEditText(value = newUrl, onValueChange = { newUrl = it }, label = strings.websiteUrlLabel, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanTarget = newUrl.trim()
                        if (cleanTarget.isNotEmpty()) {
                            if (onAddBookmark != null) onAddBookmark(newTitle.ifBlank { cleanTarget }, cleanTarget)
                            else onOpenUrl(cleanTarget)
                            showAddDialog = false
                        }
                    }
                ) { Text(strings.saveButton) }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text(strings.cancelButton) }
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

@Composable
fun HistorySheet(
    history: List<HistoryEntity>,
    onOpenUrl: (String) -> Unit,
    onDeleteHistoryItem: (Long) -> Unit,
    onOpenClearDataDialog: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalNovaStrings.current
    val palette = rememberSheetPalette()
    var searchQuery by remember { mutableStateOf("") }

    val filteredHistory = remember(history, searchQuery) {
        val q = searchQuery.trim()
        val list = if (q.isEmpty()) history else history.filter { it.title.contains(q, ignoreCase = true) || it.url.contains(q, ignoreCase = true) }
        list.take(150)
    }

    val todayCount = remember(history) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        history.count { it.visitedAt >= cal.timeInMillis }
    }

    val groupedHistory = remember(filteredHistory, strings) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val todayStart = cal.timeInMillis
        val yesterdayStart = todayStart - 24L * 60L * 60L * 1000L
        val dateFormat = SimpleDateFormat("MMM d", Locale.US)

        val buckets = LinkedHashMap<String, MutableList<HistoryEntity>>()
        for (entry in filteredHistory) {
            val dateStr = dateFormat.format(Date(entry.visitedAt)).uppercase(Locale.US)
            val prefix = when {
                entry.visitedAt >= todayStart -> "${strings.todaySuffix} • $dateStr"
                entry.visitedAt >= yesterdayStart -> "${strings.yesterdayLabel} • $dateStr"
                else -> dateStr
            }
            buckets.getOrPut(prefix) { mutableListOf() }.add(entry)
        }
        buckets.map { (header, entries) ->
            HistoryDayGroup(headerLabel = header, items = entries, visitCountsByUrl = entries.groupingBy { it.url }.eachCount())
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    NovaBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.sheetBg,
        modifier = Modifier.testTag("history_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SheetHeaderIconBadge(icon = NovaIcons.History, palette = palette)
                    Column {
                        Text(
                            text = strings.browsingHistoryTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = palette.textPrimary
                        )
                        Text(
                            text = strings.historyVisitsStats(history.size, todayCount),
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, letterSpacing = 0.8.sp),
                            color = palette.textSecondary
                        )
                    }
                }

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
                        Icon(NovaIcons.DeleteOutline, null, tint = palette.dangerRed, modifier = Modifier.size(16.dp))
                        Text(
                            text = strings.clearAllButton,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.dangerRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            NovaEditText(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = strings.searchHistoryPlaceholder,
                containerColor = palette.cardBg,
                borderColor = palette.cardBorder,
                textColor = palette.textPrimary,
                hintColor = palette.textMuted,
                leadingIcon = { Icon(NovaIcons.Search, null, tint = palette.accentPrimary, modifier = Modifier.size(18.dp)) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(28.dp)) {
                            Icon(NovaIcons.Close, "Clear search", tint = palette.textSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_input")
            )

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
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    groupedHistory.forEach { group ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = group.headerLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp),
                                    color = palette.textSecondary
                                )
                                Text(
                                    text = strings.historyVisitsLabel(group.items.size),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = palette.textMuted
                                )
                            }

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
                                            MonogramSiteBadge(label = host.ifBlank { entry.title }, url = entry.url)
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text(
                                                        text = entry.title.ifBlank { host },
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        color = palette.textPrimary,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                    if (repeatCount > 1) {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = palette.accentPrimary.copy(alpha = 0.18f),
                                                            border = BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.55f))
                                                        ) {
                                                            Text(
                                                                text = "${repeatCount}×",
                                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                                                color = palette.accentPrimary,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "$host • ${timeFormat.format(Date(entry.visitedAt))}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
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
                                                Icon(NovaIcons.Close, strings.deleteAction, tint = palette.textSecondary, modifier = Modifier.size(16.dp))
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
// 5. BROWSER MENU SHEET — "Control center" (No Tune 3-dot Badge, Rules on Pro/Max)
// ============================================================================

@Composable
fun BrowserMenuSheet(
    activeTab: BrowserTab,
    isBookmarked: Boolean,
    isAdBlockEnabled: Boolean,
    adBlockMode: AdBlockMode,
    appLanguage: AppLanguage,
    isDarkMode: Boolean,
    totalAdsBlocked: Long,
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
    val palette = rememberSheetPalette(isIncognito = activeTab.isIncognito)
    val minimalTotal = remember { AdBlocker.getTotalRuleCount(AdBlockMode.MINIMAL) }
    val proTotal = remember { AdBlocker.getTotalRuleCount(AdBlockMode.PRO) }
    val maxTotal = remember { AdBlocker.getTotalRuleCount(AdBlockMode.MAX) }
    val activeModeLabel = strings.adBlockModeLabel(adBlockMode)

    NovaBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.sheetBg,
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
            // Header: Clean Title + Shield Stats + Close button (no left 3-dot Tune badge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, letterSpacing = 0.8.sp),
                        color = palette.textSecondary
                    )
                }
                SheetCloseButton(palette = palette, onDismiss = onDismiss)
            }

            // 5 Quick Action Tiles
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ControlCenterQuickTile(NovaIcons.BookmarkBorder, strings.quickActionBookmarks, palette, onOpenBookmarks, Modifier.testTag("menu_bookmarks_button"))
                ControlCenterQuickTile(NovaIcons.Download, strings.quickActionDownloads, palette, onOpenDownloads, Modifier.testTag("menu_downloads_button"))
                ControlCenterQuickTile(NovaIcons.History, strings.quickActionHistory, palette, onOpenHistory, Modifier.testTag("menu_history_button"))
                ControlCenterQuickTile(NovaIcons.Share, strings.quickActionShare, palette, onSharePage, Modifier.testTag("menu_share_button"))
                ControlCenterQuickTile(NovaIcons.ContentCopy, strings.quickActionCopyLink, palette, onCopyPageLink, Modifier.testTag("menu_copy_link_button"))
            }

            HorizontalDivider(color = palette.cardBorder.copy(alpha = 0.7f))

            // Ad-block shield header + switch
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
                    Icon(NovaIcons.ShieldOutline, null, tint = palette.accentPrimary, modifier = Modifier.size(22.dp))
                    Column {
                        Text(strings.adBlockShieldMenuTitle, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = palette.textPrimary)
                        Text(
                            text = "${activeModeLabel.uppercase(Locale.ROOT)} • $totalAdsBlocked",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, letterSpacing = 0.6.sp),
                            color = palette.textSecondary
                        )
                    }
                }
                Switch(
                    checked = isAdBlockEnabled,
                    onCheckedChange = { onToggleAdBlock() },
                    checkedTrackColor = palette.accentPrimary,
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("menu_adblock_switch")
                )
            }

            // 3 Mode Cards: Minimal / न्यूनतम, Pro / अधिक, Max / अधिकतम — all include "RULES" / "नियम"
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    Triple(AdBlockMode.MINIMAL, strings.modeMinimalTitle to strings.rulesCountSuffix(minimalTotal), "menu_adblock_mode_minimal"),
                    Triple(AdBlockMode.PRO, strings.modeProTitle to strings.rulesCountSuffix(proTotal), "menu_adblock_mode_pro"),
                    Triple(AdBlockMode.MAX, strings.modeMaxTitle to strings.rulesCountSuffix(maxTotal), "menu_adblock_mode_max")
                ).forEach { (mode, titleAndSub, tag) ->
                    val (modeTitle, countSubtitle) = titleAndSub
                    val isSelected = isAdBlockEnabled && adBlockMode == mode
                    Surface(
                        onClick = { onSelectAdBlockMode(mode) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) palette.activePillBg else palette.cardBg,
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) palette.activePillBorder else palette.cardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag(tag)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = modeTitle,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) palette.accentPrimary else palette.textPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = countSubtitle,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                                color = if (isSelected) palette.textPrimary.copy(alpha = 0.85f) else palette.textMuted,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Filter modes inspect card
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
                        SheetHeaderIconBadge(icon = NovaIcons.ListAlt, palette = palette, modifier = Modifier.size(38.dp))
                        Column {
                            Text(strings.filterModesTitle, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = palette.textPrimary)
                            Text(strings.filterModesSubtitle, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = palette.textSecondary)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.accentGradient)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(strings.configureButton, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            HorizontalDivider(color = palette.cardBorder.copy(alpha = 0.7f))

            ControlCenterSwitchRow(NovaIcons.DesktopWindows, strings.desktopSiteMode, null, activeTab.isDesktopMode, palette, { onToggleDesktopMode() }, "menu_desktop_mode_switch")
            ControlCenterSwitchRow(NovaIcons.DarkMode, strings.darkModeTheme, null, isDarkMode, palette, { onToggleDarkMode() }, "menu_dark_mode_switch")
            ControlCenterSwitchRow(NovaIcons.Incognito, strings.incognitoLabel, strings.nothingIsSavedSubtitle, activeTab.isIncognito, palette, { onToggleIncognito() }, "menu_incognito_switch")

            // Text zoom controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(NovaIcons.TextFields, null, tint = palette.accentPrimary, modifier = Modifier.size(20.dp))
                    Text(strings.textZoomLabel, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = palette.textPrimary)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            Icon(NovaIcons.Remove, "Decrease Text Size", tint = palette.accentPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(
                        text = "${activeTab.textZoom}%",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
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
                            Icon(NovaIcons.Add, "Increase Text Size", tint = palette.accentPrimary, modifier = Modifier.size(16.dp))
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
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
                            Text(strings.resetButton, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = palette.accentPrimary)
                        }
                    }
                }
            }

            // Language selector
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(NovaIcons.Translate, null, tint = palette.accentPrimary, modifier = Modifier.size(18.dp))
                    Text(strings.languageSectionTitle, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = palette.textPrimary)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Triple(AppLanguage.SYSTEM, strings.languageSystemOption, "menu_language_system"),
                        Triple(AppLanguage.ENGLISH, strings.languageEnglishOption, "menu_language_en"),
                        Triple(AppLanguage.HINDI, strings.languageHindiOption, "menu_language_hi")
                    ).forEach { (lang, label, tag) ->
                        val isSelected = appLanguage == lang
                        Surface(
                            onClick = { onSelectAppLanguage(lang) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) palette.activePillBg else palette.cardBg,
                            border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) palette.activePillBorder else palette.cardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .testTag(tag)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
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

            if (!activeTab.isHomePage) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PageToolBtn(
                        icon = if (isBookmarked) NovaIcons.Bookmark else NovaIcons.BookmarkBorder,
                        label = if (isBookmarked) strings.savedButton else strings.bookmarkButton,
                        active = isBookmarked,
                        palette = palette,
                        onClick = onToggleBookmark,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("menu_bookmark_toggle_button")
                    )
                    PageToolBtn(NovaIcons.Code, strings.inspectButton, false, palette, onOpenDevTools, Modifier.weight(1f).testTag("menu_devtools_button"))
                    PageToolBtn(NovaIcons.Videocam, strings.videosButton, false, palette, onScanPageVideos, Modifier.weight(1f).testTag("menu_scan_videos_button"))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        Icon(NovaIcons.Search, null, tint = palette.accentPrimary, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.findInPageButton, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = palette.textPrimary)
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
                        Icon(NovaIcons.DeleteOutline, null, tint = palette.dangerRed, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.clearDataButton, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = palette.dangerRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun PageToolBtn(
    icon: ImageVector,
    label: String,
    active: Boolean,
    palette: SheetVisualPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (active) palette.activePillBg else palette.cardBg,
        border = BorderStroke(1.dp, if (active) palette.activePillBorder else palette.cardBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = palette.accentPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = palette.textPrimary, maxLines = 1)
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
            Icon(icon, label, tint = palette.accentPrimary, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp, letterSpacing = 0.4.sp),
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
            Icon(icon, null, tint = palette.accentPrimary, modifier = Modifier.size(20.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = palette.textPrimary)
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp, letterSpacing = 0.6.sp),
                        color = palette.textMuted
                    )
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            checkedTrackColor = palette.accentPrimary,
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
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
        icon = { Icon(NovaIcons.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(strings.clearDataDialogTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(strings.clearDataDialogSubtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { clearHistory = !clearHistory }) {
                    Checkbox(checked = clearHistory, onCheckedChange = { clearHistory = it })
                    Text(strings.clearHistoryCheckbox)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { clearCookies = !clearCookies }) {
                    Checkbox(checked = clearCookies, onCheckedChange = { clearCookies = it })
                    Text(strings.clearCookiesCheckbox)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { clearCache = !clearCache }) {
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
            ) { Text(strings.clearNowButton) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancelButton) }
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
        title = { Text(strings.addShortcutDialogTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NovaEditText(
                    value = title,
                    onValueChange = { title = it },
                    label = strings.siteTitleLabel,
                    placeholder = "e.g. GitHub",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shortcut_title_input")
                )
                NovaEditText(
                    value = url,
                    onValueChange = { url = it },
                    label = strings.websiteUrlLabel,
                    placeholder = "https://github.com",
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
            ) { Text(strings.addButton) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancelButton) }
        }
    )
}

// ============================================================================
// 7. SSL SECURITY & SITE CONFIG INSPECTOR SHEET
// ============================================================================

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
    val palette = rememberSheetPalette(isIncognito = tab.isIncognito)
    var selectedSection by remember { mutableIntStateOf(0) }

    NovaBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.sheetBg,
        modifier = Modifier.testTag("ssl_security_info_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 380.dp, max = 560.dp)
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SheetHeaderIconBadge(icon = if (tab.isSecureSsl) NovaIcons.Lock else NovaIcons.Warning, palette = palette)
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
                IconButton(onClick = { onCopyText("Website URL", tab.url) }, modifier = Modifier.size(34.dp)) {
                    Icon(NovaIcons.ContentCopy, "Copy Full URL", tint = palette.accentPrimary, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterPill("Certificate", selectedSection == 0, palette, { selectedSection = 0 }, Modifier.testTag("lock_tab_certificate"))
                FilterPill("Cookies (${siteCookies.size})", selectedSection == 1, palette, { selectedSection = 1 }, Modifier.testTag("lock_tab_cookies"))
                FilterPill("Site Config", selectedSection == 2, palette, { selectedSection = 2 }, Modifier.testTag("lock_tab_site_config"))
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
                            CertificateFieldBlock("Issued To (Common Name)", certificateDetails.issuedToCommonName, palette, onCopyText)
                            CertificateFieldBlock("Issued To Organization", certificateDetails.issuedToOrganization, palette, onCopyText)
                            CertificateFieldBlock("Issued By (Authority)", certificateDetails.issuedByCommonName, palette, onCopyText)
                            CertificateFieldBlock("Valid From", certificateDetails.validFrom, palette, onCopyText)
                            CertificateFieldBlock("Expires On", certificateDetails.validUntil, palette, onCopyText)
                            CertificateFieldBlock("SHA-256 Fingerprint", certificateDetails.sha256Fingerprint, palette, onCopyText, monospace = true)
                        } else {
                            Surface(shape = RoundedCornerShape(16.dp), color = palette.cardBg, border = BorderStroke(1.dp, palette.cardBorder), modifier = Modifier.fillMaxWidth()) {
                                Text("No X.509 SSL certificate is attached to this page.", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary, modifier = Modifier.padding(16.dp))
                            }
                        }
                    }
                }

                1 -> {
                    Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${siteCookies.size} cookies stored", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                            if (siteCookies.isNotEmpty()) {
                                TextButton(onClick = onClearSiteCookies, modifier = Modifier.testTag("clear_site_cookies_button")) {
                                    Text("Clear Site Cookies", color = palette.dangerRed)
                                }
                            }
                        }
                        if (siteCookies.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                Text("No cookies stored for this domain.", style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                siteCookies.take(80).forEach { (cookieName, cookieVal) ->
                                    Surface(shape = RoundedCornerShape(14.dp), color = palette.cardBg, border = BorderStroke(1.dp, palette.cardBorder), modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(cookieName, style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold), color = palette.accentPrimary)
                                                Text(
                                                    text = cookieVal.ifBlank { "(empty)" },
                                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = palette.textPrimary
                                                )
                                            }
                                            IconButton(onClick = { onCopyText(cookieName, "$cookieName=$cookieVal") }, modifier = Modifier.size(28.dp)) {
                                                Icon(NovaIcons.ContentCopy, "Copy Cookie", tint = palette.accentPrimary, modifier = Modifier.size(14.dp))
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
                        ControlCenterSwitchRow(NovaIcons.ShieldOutline, strings.adBlockShieldMenuTitle, "BLOCKS ADS & TELEMETRY", isAdBlockEnabled, palette, { onToggleAdBlock() }, "ssl_adblock_switch")
                        ControlCenterSwitchRow(NovaIcons.DesktopWindows, strings.desktopSiteMode, "DESKTOP USER-AGENT", tab.isDesktopMode, palette, { onToggleDesktopMode() }, "ssl_desktop_switch")
                        ControlCenterSwitchRow(NovaIcons.DarkMode, strings.darkModeTheme, "AMOLED DARK PALETTE", isDarkMode, palette, { onToggleDarkMode() }, "ssl_dark_switch")
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
    onCopyText: (String, String) -> Unit,
    monospace: Boolean = false
) {
    val cleanVal = value.ifBlank { "Unspecified" }
    Surface(
        onClick = { onCopyText(label, cleanVal) },
        shape = RoundedCornerShape(14.dp),
        color = palette.cardBg,
        border = BorderStroke(1.dp, palette.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = cleanVal,
                style = if (monospace) {
                    MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                } else {
                    MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                },
                color = palette.textPrimary
            )
        }
    }
}

// ============================================================================
// 8. AD-BLOCK SHIELD SHEET
// ============================================================================

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
    val palette = rememberSheetPalette()

    var selectedFilter by remember { mutableIntStateOf(0) }
    var customRulesList by remember { mutableStateOf(listOf<String>()) }
    var showAddRuleDialog by remember { mutableStateOf(false) }
    var newCustomRuleInput by remember { mutableStateOf("") }

    val (hostCount, pathCount, cosmeticCount) = remember(adBlockMode) { AdBlocker.getRuleCountsForMode(adBlockMode) }
    val activeRules: List<AdFilterRuleItem> = remember(adBlockMode, customRulesList) {
        customRulesList.map { AdFilterRuleItem(rule = it, category = "Custom Rule", tier = adBlockMode) } +
            AdBlocker.getRulesCatalogForMode(adBlockMode)
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

    val currentTabBlockedCount = remember(blockedAds, activeTabId) { blockedAds.count { it.tabId == activeTabId } }
    val currentSiteHost = remember(currentSiteUrl, blockedAds, activeTabId) {
        val fromUrl = extractHostClean(currentSiteUrl)
        if (fromUrl.isNotBlank() && !fromUrl.startsWith("nova:")) fromUrl
        else blockedAds.firstOrNull { it.tabId == activeTabId }?.pageDomain.orEmpty().ifBlank { "google.com" }
    }
    val activeModeLabel = strings.adBlockModeLabel(adBlockMode)

    NovaBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.sheetBg,
        modifier = Modifier.testTag("blocked_ads_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    SheetHeaderIconBadge(icon = NovaIcons.ShieldCheck, palette = palette)
                    Column {
                        Text(strings.adBlockShieldMenuTitle, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = palette.textPrimary)
                        Text(
                            text = if (isAdBlockEnabled) strings.adShieldHeaderActive(totalAdsBlocked, activeRules.size) else strings.adShieldHeaderDisabled,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, letterSpacing = 0.7.sp),
                            color = palette.textSecondary
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (blockedAds.isNotEmpty()) {
                        IconButton(
                            onClick = onClearLog,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("clear_blocked_ads_log_button")
                        ) {
                            Icon(NovaIcons.DeleteOutline, strings.clearAllButton, tint = palette.dangerRed, modifier = Modifier.size(18.dp))
                        }
                    }
                    Switch(
                        checked = isAdBlockEnabled,
                        onCheckedChange = { onToggleAdBlock() },
                        checkedTrackColor = palette.accentPrimary,
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("blocked_ads_sheet_toggle")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) palette.activePillBorder else palette.cardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag(tag)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Text(modeTitle, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = if (isSelected) palette.accentPrimary else palette.textPrimary)
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Surface(shape = RoundedCornerShape(5.dp), color = palette.accentPrimary) {
                                        Text(
                                            text = "ON",
                                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
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
            Text(
                text = strings.filterBreakdownLine(hostCount, pathCount, cosmeticCount),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                color = palette.textSecondary,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterPill("${strings.filterChipAll} (${blockedAds.size})", selectedFilter == 0, palette, { selectedFilter = 0 })
                FilterPill("${strings.filterChipTab} ($currentTabBlockedCount)", selectedFilter == 1, palette, { selectedFilter = 1 })
                FilterPill(strings.filterChipAds, selectedFilter == 2, palette, { selectedFilter = 2 })
                FilterPill(strings.filterChipTrackers, selectedFilter == 3, palette, { selectedFilter = 3 })
                FilterPill("${strings.filterChipRules} (${activeRules.size})", selectedFilter == 4, palette, { selectedFilter = 4 }, Modifier.testTag("blocked_ads_rules_chip"))
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                        MonogramSiteBadge(label = currentSiteHost, url = currentSiteUrl)
                        Column {
                            Text(currentSiteHost, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(strings.thisPageBlockedCount(currentTabBlockedCount), style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = palette.textSecondary)
                        }
                    }
                    Surface(shape = RoundedCornerShape(12.dp), color = palette.cardElevatedBg, border = BorderStroke(1.dp, palette.cardBorder)) {
                        Text(
                            text = if (currentTabBlockedCount == 0) strings.cleanBadge else strings.protectedBadge,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.accentPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedFilter == 4) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 270.dp)
                        .verticalScroll(rememberScrollState())
                        .testTag("active_filter_rules_list")
                ) {
                    activeRules.take(150).forEach { ruleItem ->
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
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Surface(shape = RoundedCornerShape(6.dp), color = palette.activePillBg) {
                                            Text(
                                                text = ruleItem.tier.shortBadge.uppercase(Locale.US),
                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                                color = palette.accentPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(ruleItem.category, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = palette.textSecondary)
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(ruleItem.rule, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold), color = palette.textPrimary)
                                }
                                Icon(NovaIcons.ContentCopy, "Copy Rule", tint = palette.accentPrimary, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            } else if (filteredEntries.isEmpty()) {
                Surface(shape = RoundedCornerShape(22.dp), color = palette.cardBg, border = BorderStroke(1.dp, palette.cardBorder), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SheetHeaderIconBadge(icon = NovaIcons.Block, palette = palette)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isAdBlockEnabled) strings.shieldActiveEmptyTitle(activeModeLabel, activeRules.size) else strings.shieldDisabledEmptyTitle,
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 270.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    filteredEntries.take(120).forEach { item ->
                        Surface(
                            onClick = { onCopyAdUrl(item.adUrl) },
                            shape = RoundedCornerShape(16.dp),
                            color = palette.cardBg,
                            border = BorderStroke(1.dp, palette.cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("blocked_ad_item_${item.id}")
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${item.category} • ${item.filterRule}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                        color = palette.accentPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(timeFormatter.format(Date(item.timestamp)), style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp), color = palette.textMuted)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(item.adUrl, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp), maxLines = 2, overflow = TextOverflow.Ellipsis, color = palette.textPrimary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    onClick = onToggleAdBlock,
                    shape = RoundedCornerShape(16.dp),
                    color = palette.cardBg,
                    border = BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("adblock_pause_site_button")
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isAdBlockEnabled) strings.pauseOnSiteButton else strings.resumeOnSiteButton,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.textPrimary
                        )
                    }
                }

                Surface(
                    onClick = { if (selectedFilter == 4) showAddRuleDialog = true else selectedFilter = 4 },
                    shape = RoundedCornerShape(16.dp),
                    color = palette.cardBg,
                    border = BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("adblock_custom_rules_button")
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (selectedFilter == 4) strings.addCustomRuleButton else strings.filterChipRules,
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
                NovaEditText(
                    value = newCustomRuleInput,
                    onValueChange = { newCustomRuleInput = it },
                    label = "Host, path, or CSS selector",
                    placeholder = "e.g. ads.example.com or .banner-slot",
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
                ) { Text(strings.addButton) }
            },
            dismissButton = {
                TextButton(onClick = { showAddRuleDialog = false }) { Text(strings.cancelButton) }
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
