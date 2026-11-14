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
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.data.SiteConfigSettings
import com.example.data.SiteCookieEntry
import com.example.data.SiteCookiePolicy
import com.example.data.SitePermissionMode
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
// 1. TABS SWITCHER BOTTOM SHEET & WEBSITE PREVIEW
// ============================================================================

@Composable
private fun TabWebsitePreviewCard(
    tab: BrowserTab,
    palette: SheetVisualPalette,
    strings: com.example.ui.i18n.NovaStrings
) {
    val isYouTube = remember(tab.url, tab.title) {
        tab.url.contains("youtube.com", ignoreCase = true) ||
            tab.url.contains("youtu.be", ignoreCase = true) ||
            tab.title.contains("YouTube", ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isYouTube) Color(0xFF0F0F0F) else palette.cardElevatedBg)
            .border(1.dp, if (isYouTube) Color(0xFF333333) else palette.cardBorder, RoundedCornerShape(12.dp))
    ) {
        if (tab.isHomePage) {
            // Netra Home Preview
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(palette.accentPrimary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = NovaIcons.Public,
                        contentDescription = null,
                        tint = palette.accentPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(palette.cardBg)
                        .border(0.5.dp, palette.cardBorder, RoundedCornerShape(9.dp))
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(NovaIcons.Search, null, tint = palette.textMuted, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "netra://start",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontFamily = FontFamily.Monospace),
                            color = palette.textMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(Color(0xFF4285F4), Color(0xFFFF0000), Color(0xFF00C853), Color(0xFFFF8A3D)).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(color.copy(alpha = 0.25f))
                                .border(0.5.dp, color.copy(alpha = 0.5f), CircleShape)
                        )
                    }
                }
            }
        } else if (isYouTube) {
            // YouTube Website Page Preview
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Mini YouTube Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .background(Color(0xFF212121))
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(14.dp, 10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFFF0000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(NovaIcons.PlayArrow, null, tint = Color.White, modifier = Modifier.size(8.dp))
                        }
                        Text(
                            text = "YouTube",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp),
                            color = Color.White
                        )
                    }
                    Icon(NovaIcons.Search, null, tint = Color.LightGray, modifier = Modifier.size(10.dp))
                }

                // Mini Video Player
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1E1E1E), Color(0xFF0A0A0A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF0000).copy(alpha = 0.90f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(NovaIcons.PlayArrow, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "1080p",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(0.42f)
                            .height(2.dp)
                            .background(Color(0xFFFF0000))
                    )
                }

                // Video Details & Channel Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tab.title.removeSuffix(" - YouTube").ifBlank { "YouTube Video" },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF0000).copy(alpha = 0.6f))
                        )
                        Text(
                            text = "m.youtube.com • Recommended",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp),
                            color = Color(0xFFAAAAAA),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        } else {
            // General Website Page Preview
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
            ) {
                // Mini Address & Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(palette.cardBg)
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(NovaIcons.Lock, null, tint = palette.accentPrimary, modifier = Modifier.size(9.dp))
                        Text(
                            text = extractHostClean(tab.url),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold),
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = if (tab.isIncognito) strings.stealthTabBadge else strings.standardTabBadge,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, fontFamily = FontFamily.Monospace),
                        color = if (tab.isIncognito) palette.accentPrimary else palette.textMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Hero Content Preview Block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(palette.accentPrimary.copy(alpha = 0.08f))
                        .border(0.5.dp, palette.accentPrimary.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                        .padding(6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(palette.accentPrimary.copy(alpha = 0.20f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = extractHostClean(tab.url).firstOrNull()?.uppercaseChar()?.toString() ?: "W",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 11.sp),
                                color = palette.accentPrimary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.5.sp),
                                color = palette.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(palette.textMuted.copy(alpha = 0.35f))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Simulated Content Lines
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(palette.cardBg)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.68f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(palette.cardBg)
                    )
                }
            }
        }
    }
}

@Composable
fun TabsSwitcherSheet(
    tabs: List<BrowserTab>,
    activeTabId: String,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onCloseAllTabs: () -> Unit,
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
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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

                Surface(
                    onClick = onCloseAllTabs,
                    shape = RoundedCornerShape(14.dp),
                    color = palette.dangerBg,
                    border = BorderStroke(1.dp, palette.dangerBorder),
                    modifier = Modifier.testTag("close_all_tabs_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = NovaIcons.DeleteSweep,
                            contentDescription = strings.closeAllTabsButton,
                            tint = palette.dangerRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = strings.closeAllTabsButton,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.dangerRed
                        )
                    }
                }
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
                        .height(48.dp)
                        .testTag("new_incognito_tab_button")
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
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
                }

                Surface(
                    onClick = onNewNormalTab,
                    shape = RoundedCornerShape(14.dp),
                    color = palette.activePillBg,
                    border = BorderStroke(1.5.dp, palette.activePillBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("new_tab_button")
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
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

                                        TabWebsitePreviewCard(
                                            tab = tab,
                                            palette = palette,
                                            strings = strings
                                        )
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
                text = "PATH: /Download/Netra",
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
                    SheetHeaderIconBadge(icon = NovaIcons.Bookmarks, palette = palette)
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
                .padding(horizontal = 18.dp, vertical = 6.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 5 Quick Action Tiles
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ControlCenterQuickTile(NovaIcons.Bookmarks, strings.quickActionBookmarks, palette, onOpenBookmarks, Modifier.testTag("menu_bookmarks_button"))
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

            // Display, privacy & zoom grouped card (Desktop site & below)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = palette.cardBg,
                border = BorderStroke(1.dp, palette.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ControlCenterSwitchRow(
                        icon = NovaIcons.DesktopWindows,
                        title = strings.desktopSiteMode,
                        subtitle = null,
                        checked = activeTab.isDesktopMode,
                        palette = palette,
                        onCheckedChange = { onToggleDesktopMode() },
                        testTag = "menu_desktop_mode_switch"
                    )
                    HorizontalDivider(
                        color = palette.cardBorder.copy(alpha = 0.55f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    ControlCenterSwitchRow(
                        icon = NovaIcons.DarkMode,
                        title = strings.darkModeTheme,
                        subtitle = null,
                        checked = isDarkMode,
                        palette = palette,
                        onCheckedChange = { onToggleDarkMode() },
                        testTag = "menu_dark_mode_switch"
                    )
                    HorizontalDivider(
                        color = palette.cardBorder.copy(alpha = 0.55f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    ControlCenterSwitchRow(
                        icon = NovaIcons.Incognito,
                        title = strings.incognitoLabel,
                        subtitle = strings.nothingIsSavedSubtitle,
                        checked = activeTab.isIncognito,
                        palette = palette,
                        onCheckedChange = { onToggleIncognito() },
                        testTag = "menu_incognito_switch"
                    )
                    HorizontalDivider(
                        color = palette.cardBorder.copy(alpha = 0.55f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )

                    // Text zoom row aligned with switch rows above
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(palette.cardElevatedBg)
                                    .border(1.dp, palette.cardBorder, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(NovaIcons.TextFields, null, tint = palette.accentPrimary, modifier = Modifier.size(19.dp))
                            }
                            Text(
                                text = strings.textZoomLabel,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                color = palette.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Surface(
                                onClick = { onUpdateTextZoom(activeTab.textZoom - 10) },
                                shape = RoundedCornerShape(10.dp),
                                color = palette.cardElevatedBg,
                                border = BorderStroke(1.dp, palette.cardBorder),
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("text_zoom_decrease")
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(NovaIcons.Remove, "Decrease Text Size", tint = palette.accentPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = "${activeTab.textZoom}%",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = palette.textPrimary,
                                modifier = Modifier.widthIn(min = 40.dp),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false
                            )
                            Surface(
                                onClick = { onUpdateTextZoom(activeTab.textZoom + 10) },
                                shape = RoundedCornerShape(10.dp),
                                color = palette.cardElevatedBg,
                                border = BorderStroke(1.dp, palette.cardBorder),
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("text_zoom_increase")
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(NovaIcons.Add, "Increase Text Size", tint = palette.accentPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                            Surface(
                                onClick = { onUpdateTextZoom(100) },
                                shape = RoundedCornerShape(10.dp),
                                color = palette.cardElevatedBg,
                                border = BorderStroke(1.dp, palette.cardBorder),
                                modifier = Modifier.testTag("text_zoom_reset")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .height(32.dp)
                                        .widthIn(min = 52.dp)
                                        .padding(horizontal = 10.dp)
                                ) {
                                    Text(
                                        text = strings.resetButton,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 12.sp,
                                            lineHeight = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.sp
                                        ),
                                        color = palette.accentPrimary,
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

            // Language selector card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = palette.cardBg,
                border = BorderStroke(1.dp, palette.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(palette.cardElevatedBg)
                                .border(1.dp, palette.cardBorder, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(NovaIcons.Translate, null, tint = palette.accentPrimary, modifier = Modifier.size(19.dp))
                        }
                        Text(
                            text = strings.languageSectionTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(AppLanguage.SYSTEM, strings.languageSystemOption, "menu_language_system"),
                            Triple(AppLanguage.ENGLISH, strings.languageEnglishOption, "menu_language_en"),
                            Triple(AppLanguage.HINDI, strings.languageHindiOption, "menu_language_hi")
                        ).forEach { (lang, label, tag) ->
                            val isSelected = appLanguage == lang
                            Surface(
                                onClick = { onSelectAppLanguage(lang) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) palette.activePillBg else palette.cardElevatedBg,
                                border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) palette.activePillBorder else palette.cardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag(tag)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                        ),
                                        color = if (isSelected) palette.accentPrimary else palette.textPrimary,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (!activeTab.isHomePage) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                    PageToolBtn(
                        icon = NovaIcons.Code,
                        label = strings.inspectButton,
                        active = false,
                        palette = palette,
                        onClick = onOpenDevTools,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("menu_devtools_button")
                    )
                    PageToolBtn(
                        icon = NovaIcons.Videocam,
                        label = strings.videosButton,
                        active = false,
                        palette = palette,
                        onClick = onScanPageVideos,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("menu_scan_videos_button")
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = onOpenFindInPage,
                    shape = RoundedCornerShape(14.dp),
                    color = palette.cardBg,
                    border = BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("menu_find_in_page_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(NovaIcons.Search, null, tint = palette.accentPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.findInPageButton,
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    onClick = onOpenClearData,
                    shape = RoundedCornerShape(14.dp),
                    color = palette.dangerBg,
                    border = BorderStroke(1.dp, palette.dangerBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("menu_clear_data_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(NovaIcons.DeleteOutline, null, tint = palette.dangerRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.clearDataButton,
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                            color = palette.dangerRed,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
        shape = RoundedCornerShape(14.dp),
        color = if (active) palette.activePillBg else palette.cardBg,
        border = BorderStroke(if (active) 1.5.dp else 1.dp, if (active) palette.activePillBorder else palette.cardBorder),
        modifier = modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = palette.accentPrimary, modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold),
                color = if (active) palette.accentPrimary else palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
    val strings = LocalNovaStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.cardElevatedBg)
                    .border(1.dp, palette.cardBorder, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = palette.accentPrimary, modifier = Modifier.size(19.dp))
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            letterSpacing = if (strings.isHindi) 0.sp else 0.25.sp
                        ),
                        color = palette.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
    siteCookies: List<SiteCookieEntry>,
    siteConfig: SiteConfigSettings,
    isAdBlockEnabled: Boolean,
    isDarkMode: Boolean,
    onToggleAdBlock: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onUpdateSiteConfig: (reloadPage: Boolean, (SiteConfigSettings) -> SiteConfigSettings) -> Unit,
    onResetSiteConfigAndData: () -> Unit,
    onClearSingleCookie: (String) -> Unit,
    onCopyText: (String, String) -> Unit,
    onClearSiteCookies: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalNovaStrings.current
    val palette = rememberSheetPalette(isIncognito = tab.isIncognito)
    var selectedSection by remember { mutableIntStateOf(0) }
    val cleanHost = remember(tab.url) { extractHostClean(tab.url) }
    val effectiveAdBlockOn = isAdBlockEnabled && siteConfig.isAdBlockEnabled

    NovaBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.sheetBg,
        modifier = Modifier.testTag("ssl_security_info_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 420.dp, max = 620.dp)
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
                        text = if (tab.isSecureSsl) strings.connSecureTitle else strings.connNotSecureTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.textPrimary
                    )
                    Text(
                        text = cleanHost,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = palette.textSecondary
                    )
                }
                IconButton(onClick = { onCopyText(strings.websiteLinkTitle, tab.url) }, modifier = Modifier.size(34.dp)) {
                    Icon(NovaIcons.ContentCopy, "Copy Full URL", tint = palette.accentPrimary, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterPill(strings.tabCertificate, selectedSection == 0, palette, { selectedSection = 0 }, Modifier.testTag("lock_tab_certificate"))
                FilterPill(strings.cookiesTabLabel(siteCookies.size), selectedSection == 1, palette, { selectedSection = 1 }, Modifier.testTag("lock_tab_cookies"))
                FilterPill(strings.tabSiteConfig, selectedSection == 2, palette, { selectedSection = 2 }, Modifier.testTag("lock_tab_site_config"))
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedSection) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (certificateDetails != null) {
                            Text(
                                text = strings.certTapToCopyHint,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textMuted,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )

                            CertificateSectionCard(title = strings.certSectionSecurity, palette = palette) {
                                CertificateFieldRow(strings.certProtocolLabel, certificateDetails.protocol, palette, onCopyText)
                                if (certificateDetails.validityStatus.isNotBlank()) {
                                    CertificateDivider(palette)
                                    CertificateFieldRow(strings.certValidityStatusLabel, certificateDetails.validityStatus, palette, onCopyText)
                                }
                                if (certificateDetails.x509Version.isNotBlank()) {
                                    CertificateDivider(palette)
                                    CertificateFieldRow(strings.certVersionLabel, certificateDetails.x509Version, palette, onCopyText)
                                }
                                if (certificateDetails.keyUsage.isNotBlank()) {
                                    CertificateDivider(palette)
                                    CertificateFieldRow(strings.certKeyUsageLabel, certificateDetails.keyUsage, palette, onCopyText)
                                }
                            }

                            CertificateSectionCard(title = strings.certSectionIssuedTo, palette = palette) {
                                CertificateFieldRow(strings.certCommonNameLabel, certificateDetails.issuedToCommonName, palette, onCopyText)
                                CertificateDivider(palette)
                                CertificateFieldRow(strings.certOrganizationLabel, certificateDetails.issuedToOrganization, palette, onCopyText)
                                CertificateDivider(palette)
                                CertificateFieldRow(strings.certOrgUnitLabel, certificateDetails.issuedToOrganizationalUnit, palette, onCopyText)
                                if (certificateDetails.issuedToCountry.isNotBlank()) {
                                    CertificateDivider(palette)
                                    CertificateFieldRow(strings.certCountryLabel, certificateDetails.issuedToCountry, palette, onCopyText)
                                }
                                if (certificateDetails.subjectDn.isNotBlank()) {
                                    CertificateDivider(palette)
                                    CertificateFieldRow(strings.certFullDnLabel, certificateDetails.subjectDn, palette, onCopyText, monospace = true)
                                }
                            }

                            CertificateSectionCard(title = strings.certSectionIssuedBy, palette = palette) {
                                CertificateFieldRow(strings.certCommonNameLabel, certificateDetails.issuedByCommonName, palette, onCopyText)
                                CertificateDivider(palette)
                                CertificateFieldRow(strings.certOrganizationLabel, certificateDetails.issuedByOrganization, palette, onCopyText)
                                CertificateDivider(palette)
                                CertificateFieldRow(strings.certOrgUnitLabel, certificateDetails.issuedByOrganizationalUnit, palette, onCopyText)
                                if (certificateDetails.issuerDn.isNotBlank()) {
                                    CertificateDivider(palette)
                                    CertificateFieldRow(strings.certFullDnLabel, certificateDetails.issuerDn, palette, onCopyText, monospace = true)
                                }
                            }

                            CertificateSectionCard(title = strings.certSectionValidity, palette = palette) {
                                CertificateFieldRow(strings.certValidFromLabel, certificateDetails.validFrom, palette, onCopyText)
                                CertificateDivider(palette)
                                CertificateFieldRow(strings.certExpiresOnLabel, certificateDetails.validUntil, palette, onCopyText)
                            }

                            if (certificateDetails.subjectAltNames.isNotEmpty()) {
                                CertificateSectionCard(
                                    title = "${strings.certSectionSan} (${certificateDetails.subjectAltNames.size})",
                                    palette = palette
                                ) {
                                    CertificateFieldRow(
                                        label = "DNS / IP SANs",
                                        value = certificateDetails.subjectAltNames.joinToString("\n"),
                                        palette = palette,
                                        onCopyText = onCopyText,
                                        monospace = true
                                    )
                                }
                            }

                            CertificateSectionCard(title = strings.certSectionCrypto, palette = palette) {
                                if (certificateDetails.publicKeyInfo.isNotBlank()) {
                                    CertificateFieldRow(strings.certPublicKeyLabel, certificateDetails.publicKeyInfo, palette, onCopyText)
                                    CertificateDivider(palette)
                                }
                                if (certificateDetails.signatureAlgorithm.isNotBlank()) {
                                    CertificateFieldRow(strings.certSigAlgLabel, certificateDetails.signatureAlgorithm, palette, onCopyText)
                                    CertificateDivider(palette)
                                }
                                CertificateFieldRow(strings.certSerialLabel, certificateDetails.serialNumber, palette, onCopyText, monospace = true)
                                CertificateDivider(palette)
                                CertificateFieldRow(strings.certSha256Label, certificateDetails.sha256Fingerprint, palette, onCopyText, monospace = true)
                                if (certificateDetails.sha1Fingerprint.isNotBlank()) {
                                    CertificateDivider(palette)
                                    CertificateFieldRow(strings.certSha1Label, certificateDetails.sha1Fingerprint, palette, onCopyText, monospace = true)
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = palette.cardBg,
                                border = BorderStroke(1.dp, palette.cardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = strings.certEmptyNotice,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textSecondary,
                                    modifier = Modifier.padding(16.dp)
                                )
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.cookiesStoredCount(siteCookies.size),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = palette.textPrimary
                                )
                                Text(
                                    text = when {
                                        tab.isIncognito -> "${strings.incognitoLabel} • Disabled"
                                        siteConfig.cookiePolicy == SiteCookiePolicy.ALLOW_ALL -> "${strings.siteCookiesTitle}: ${strings.siteCookieAllowAll}"
                                        siteConfig.cookiePolicy == SiteCookiePolicy.BLOCK_THIRD_PARTY -> "${strings.siteCookiesTitle}: ${strings.siteCookieBlock3p}"
                                        else -> "${strings.siteCookiesTitle}: ${strings.siteCookieBlockAll}"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textSecondary
                                )
                            }
                            if (siteCookies.isNotEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = {
                                            val allRaw = siteCookies.joinToString("; ") { "${it.name}=${it.value}" }
                                            onCopyText("Cookies ($cleanHost)", allRaw)
                                        },
                                        modifier = Modifier.testTag("copy_all_cookies_button")
                                    ) {
                                        Text(strings.copyAllButton, color = palette.accentPrimary, style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp))
                                    }
                                    TextButton(
                                        onClick = onClearSiteCookies,
                                        modifier = Modifier.testTag("clear_site_cookies_button")
                                    ) {
                                        Text(strings.clearAllButton, color = palette.dangerRed, style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp))
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        if (siteCookies.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                Text(strings.noCookiesDomain, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                siteCookies.forEach { cookie ->
                                    Surface(
                                        onClick = { onCopyText(cookie.name, "${cookie.name}=${cookie.value}") },
                                        shape = RoundedCornerShape(14.dp),
                                        color = palette.cardBg,
                                        border = BorderStroke(1.dp, palette.cardBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = cookie.name,
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontFamily = FontFamily.Monospace,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = palette.accentPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = palette.cardElevatedBg,
                                                        border = BorderStroke(1.dp, palette.cardBorder)
                                                    ) {
                                                        Text(
                                                            text = "${cookie.byteSize} B",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontFamily = FontFamily.Monospace,
                                                                fontSize = 9.5.sp
                                                            ),
                                                            color = palette.textMuted,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = cookie.value.ifBlank { "(empty)" },
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 11.sp
                                                    ),
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = palette.textPrimary
                                                )
                                                Text(
                                                    text = cookie.domain,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = palette.textMuted
                                                )
                                            }
                                            IconButton(
                                                onClick = { onCopyText(cookie.name, "${cookie.name}=${cookie.value}") },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(NovaIcons.ContentCopy, "Copy Cookie", tint = palette.accentPrimary, modifier = Modifier.size(15.dp))
                                            }
                                            IconButton(
                                                onClick = { onClearSingleCookie(cookie.name) },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(NovaIcons.DeleteOutline, "Delete Cookie", tint = palette.dangerRed, modifier = Modifier.size(16.dp))
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
                        // Site domain summary badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = palette.cardBg,
                            border = BorderStroke(1.dp, palette.cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MonogramSiteBadge(label = cleanHost, url = tab.url)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cleanHost,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = palette.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = strings.siteConfigDomainNote,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.textSecondary
                                    )
                                }
                            }
                        }

                        // Card 1: Chrome-style Content & Controls
                        SectionHeaderLabel(strings.siteSectionControls, palette)
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = palette.cardBg,
                            border = BorderStroke(1.dp, palette.cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                ControlCenterSwitchRow(
                                    icon = NovaIcons.ShieldOutline,
                                    title = strings.adBlockShieldMenuTitle,
                                    subtitle = "BLOCKS ADS & TELEMETRY",
                                    checked = effectiveAdBlockOn,
                                    palette = palette,
                                    onCheckedChange = { checked ->
                                        onUpdateSiteConfig(true) { it.copy(isAdBlockEnabled = checked) }
                                        if (checked && !isAdBlockEnabled) {
                                            onToggleAdBlock()
                                        }
                                    },
                                    testTag = "ssl_adblock_switch"
                                )
                                CertificateDivider(palette)
                                ControlCenterSwitchRow(
                                    icon = NovaIcons.DesktopWindows,
                                    title = strings.desktopSiteMode,
                                    subtitle = "DESKTOP USER-AGENT",
                                    checked = tab.isDesktopMode,
                                    palette = palette,
                                    onCheckedChange = { onToggleDesktopMode() },
                                    testTag = "ssl_desktop_switch"
                                )
                                CertificateDivider(palette)
                                ControlCenterSwitchRow(
                                    icon = NovaIcons.DarkMode,
                                    title = strings.darkModeTheme,
                                    subtitle = "AMOLED DARK PALETTE",
                                    checked = isDarkMode,
                                    palette = palette,
                                    onCheckedChange = { onToggleDarkMode() },
                                    testTag = "ssl_dark_switch"
                                )
                                CertificateDivider(palette)
                                ControlCenterSwitchRow(
                                    icon = NovaIcons.Code,
                                    title = strings.siteJsTitle,
                                    subtitle = strings.siteJsSub,
                                    checked = siteConfig.javaScriptEnabled,
                                    palette = palette,
                                    onCheckedChange = { enabled ->
                                        onUpdateSiteConfig(true) { it.copy(javaScriptEnabled = enabled) }
                                    },
                                    testTag = "site_cfg_js_switch"
                                )
                                CertificateDivider(palette)
                                SiteCookiePolicyRow(
                                    title = strings.siteCookiesTitle,
                                    policy = siteConfig.cookiePolicy,
                                    allowAllLabel = strings.siteCookieAllowAll,
                                    block3pLabel = strings.siteCookieBlock3p,
                                    blockAllLabel = strings.siteCookieBlockAll,
                                    palette = palette,
                                    onSelectPolicy = { nextPolicy ->
                                        onUpdateSiteConfig(true) { it.copy(cookiePolicy = nextPolicy) }
                                    }
                                )
                                CertificateDivider(palette)
                                ControlCenterSwitchRow(
                                    icon = NovaIcons.OpenInNew,
                                    title = strings.sitePopupsTitle,
                                    subtitle = strings.sitePopupsSub,
                                    checked = siteConfig.popupsAllowed,
                                    palette = palette,
                                    onCheckedChange = { allowed ->
                                        onUpdateSiteConfig(false) { it.copy(popupsAllowed = allowed) }
                                    },
                                    testTag = "site_cfg_popups_switch"
                                )
                                CertificateDivider(palette)
                                ControlCenterSwitchRow(
                                    icon = NovaIcons.Image,
                                    title = strings.siteImagesTitle,
                                    subtitle = strings.siteImagesSub,
                                    checked = siteConfig.imagesEnabled,
                                    palette = palette,
                                    onCheckedChange = { enabled ->
                                        onUpdateSiteConfig(true) { it.copy(imagesEnabled = enabled) }
                                    },
                                    testTag = "site_cfg_images_switch"
                                )
                                CertificateDivider(palette)
                                ControlCenterSwitchRow(
                                    icon = NovaIcons.VolumeUp,
                                    title = strings.siteSoundTitle,
                                    subtitle = strings.siteSoundSub,
                                    checked = siteConfig.soundEnabled,
                                    palette = palette,
                                    onCheckedChange = { enabled ->
                                        onUpdateSiteConfig(false) { it.copy(soundEnabled = enabled) }
                                    },
                                    testTag = "site_cfg_sound_switch"
                                )
                                CertificateDivider(palette)
                                ControlCenterSwitchRow(
                                    icon = NovaIcons.PlayArrow,
                                    title = strings.siteAutoplayTitle,
                                    subtitle = strings.siteAutoplaySub,
                                    checked = siteConfig.autoplayEnabled,
                                    palette = palette,
                                    onCheckedChange = { enabled ->
                                        onUpdateSiteConfig(false) { it.copy(autoplayEnabled = enabled) }
                                    },
                                    testTag = "site_cfg_autoplay_switch"
                                )
                            }
                        }

                        // Card 2: Chrome-style Permissions (Ask / Allow / Block)
                        SectionHeaderLabel(strings.siteSectionPermissions, palette)
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = palette.cardBg,
                            border = BorderStroke(1.dp, palette.cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                SitePermissionModeRow(
                                    icon = NovaIcons.LocationOn,
                                    title = strings.siteLocationTitle,
                                    mode = siteConfig.locationPermission,
                                    askLabel = strings.sitePermAsk,
                                    allowLabel = strings.sitePermAllow,
                                    blockLabel = strings.sitePermBlock,
                                    palette = palette,
                                    testTagPrefix = "site_cfg_location",
                                    onSelectMode = { mode ->
                                        onUpdateSiteConfig(false) { it.copy(locationPermission = mode) }
                                    }
                                )
                                CertificateDivider(palette)
                                SitePermissionModeRow(
                                    icon = NovaIcons.Videocam,
                                    title = strings.siteCameraTitle,
                                    mode = siteConfig.cameraPermission,
                                    askLabel = strings.sitePermAsk,
                                    allowLabel = strings.sitePermAllow,
                                    blockLabel = strings.sitePermBlock,
                                    palette = palette,
                                    testTagPrefix = "site_cfg_camera",
                                    onSelectMode = { mode ->
                                        onUpdateSiteConfig(false) { it.copy(cameraPermission = mode) }
                                    }
                                )
                                CertificateDivider(palette)
                                SitePermissionModeRow(
                                    icon = NovaIcons.Mic,
                                    title = strings.siteMicTitle,
                                    mode = siteConfig.microphonePermission,
                                    askLabel = strings.sitePermAsk,
                                    allowLabel = strings.sitePermAllow,
                                    blockLabel = strings.sitePermBlock,
                                    palette = palette,
                                    testTagPrefix = "site_cfg_mic",
                                    onSelectMode = { mode ->
                                        onUpdateSiteConfig(false) { it.copy(microphonePermission = mode) }
                                    }
                                )
                                CertificateDivider(palette)
                                ControlCenterSwitchRow(
                                    icon = NovaIcons.ShieldCheck,
                                    title = strings.siteDrmTitle,
                                    subtitle = strings.siteDrmSub,
                                    checked = siteConfig.protectedContentAllowed,
                                    palette = palette,
                                    onCheckedChange = { allowed ->
                                        onUpdateSiteConfig(false) { it.copy(protectedContentAllowed = allowed) }
                                    },
                                    testTag = "site_cfg_drm_switch"
                                )
                            }
                        }

                        // Card 3: Chrome-style Storage & Security
                        SectionHeaderLabel(strings.siteSectionSecurity, palette)
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = palette.cardBg,
                            border = BorderStroke(1.dp, palette.cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                ControlCenterSwitchRow(
                                    icon = NovaIcons.Storage,
                                    title = strings.siteDomStorageTitle,
                                    subtitle = strings.siteDomStorageSub,
                                    checked = siteConfig.domStorageEnabled,
                                    palette = palette,
                                    onCheckedChange = { enabled ->
                                        onUpdateSiteConfig(true) { it.copy(domStorageEnabled = enabled) }
                                    },
                                    testTag = "site_cfg_storage_switch"
                                )
                                CertificateDivider(palette)
                                ControlCenterSwitchRow(
                                    icon = NovaIcons.Warning,
                                    title = strings.siteMixedContentTitle,
                                    subtitle = strings.siteMixedContentSub,
                                    checked = siteConfig.insecureContentAllowed,
                                    palette = palette,
                                    onCheckedChange = { allowed ->
                                        onUpdateSiteConfig(true) { it.copy(insecureContentAllowed = allowed) }
                                    },
                                    testTag = "site_cfg_mixed_content_switch"
                                )
                                CertificateDivider(palette)
                                ControlCenterSwitchRow(
                                    icon = NovaIcons.ShieldCheck,
                                    title = strings.siteSafeBrowsingTitle,
                                    subtitle = strings.siteSafeBrowsingSub,
                                    checked = siteConfig.safeBrowsingEnabled,
                                    palette = palette,
                                    onCheckedChange = { enabled ->
                                        onUpdateSiteConfig(false) { it.copy(safeBrowsingEnabled = enabled) }
                                    },
                                    testTag = "site_cfg_safe_browsing_switch"
                                )
                            }
                        }

                        // Reset & Clear site data button
                        Surface(
                            onClick = onResetSiteConfigAndData,
                            shape = RoundedCornerShape(16.dp),
                            color = palette.dangerBg,
                            border = BorderStroke(1.dp, palette.dangerBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("site_cfg_reset_button")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(NovaIcons.DeleteOutline, null, tint = palette.dangerRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.siteResetAndClearButton,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = if (strings.isHindi) 0.sp else 0.1.sp
                                    ),
                                    color = palette.dangerRed,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderLabel(title: String, palette: SheetVisualPalette) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        ),
        color = palette.textSecondary,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun CertificateDivider(palette: SheetVisualPalette) {
    HorizontalDivider(
        color = palette.cardBorder.copy(alpha = 0.55f),
        modifier = Modifier.padding(horizontal = 14.dp)
    )
}

@Composable
private fun CertificateSectionCard(
    title: String,
    palette: SheetVisualPalette,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionHeaderLabel(title, palette)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = palette.cardBg,
            border = BorderStroke(1.dp, palette.cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun CertificateFieldRow(
    label: String,
    value: String,
    palette: SheetVisualPalette,
    onCopyText: (String, String) -> Unit,
    monospace: Boolean = false
) {
    val cleanVal = value.ifBlank { "Unspecified" }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCopyText(label, cleanVal) }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
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
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            NovaIcons.ContentCopy,
            contentDescription = "Copy $label",
            tint = palette.accentPrimary.copy(alpha = 0.75f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun SiteCookiePolicyRow(
    title: String,
    policy: SiteCookiePolicy,
    allowAllLabel: String,
    block3pLabel: String,
    blockAllLabel: String,
    palette: SheetVisualPalette,
    onSelectPolicy: (SiteCookiePolicy) -> Unit
) {
    val strings = LocalNovaStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.cardElevatedBg)
                    .border(1.dp, palette.cardBorder, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(NovaIcons.Public, null, tint = palette.accentPrimary, modifier = Modifier.size(19.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                color = palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                Triple(SiteCookiePolicy.ALLOW_ALL, allowAllLabel, "site_cfg_cookie_allow"),
                Triple(SiteCookiePolicy.BLOCK_THIRD_PARTY, block3pLabel, "site_cfg_cookie_no3p"),
                Triple(SiteCookiePolicy.BLOCK_ALL, blockAllLabel, "site_cfg_cookie_block")
            ).forEach { (option, label, tag) ->
                val selected = policy == option
                Surface(
                    onClick = { onSelectPolicy(option) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) palette.activePillBg else palette.cardElevatedBg,
                    border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) palette.activePillBorder else palette.cardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag(tag)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 11.5.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = if (strings.isHindi) 0.sp else 0.1.sp
                            ),
                            color = if (selected) palette.accentPrimary else palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SitePermissionModeRow(
    icon: ImageVector,
    title: String,
    mode: SitePermissionMode,
    askLabel: String,
    allowLabel: String,
    blockLabel: String,
    palette: SheetVisualPalette,
    testTagPrefix: String,
    onSelectMode: (SitePermissionMode) -> Unit
) {
    val strings = LocalNovaStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.cardElevatedBg)
                    .border(1.dp, palette.cardBorder, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = palette.accentPrimary, modifier = Modifier.size(19.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                color = palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                Triple(SitePermissionMode.ASK, askLabel, "${testTagPrefix}_ask"),
                Triple(SitePermissionMode.ALLOW, allowLabel, "${testTagPrefix}_allow"),
                Triple(SitePermissionMode.BLOCK, blockLabel, "${testTagPrefix}_block")
            ).forEach { (option, label, tag) ->
                val selected = mode == option
                Surface(
                    onClick = { onSelectMode(option) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) palette.activePillBg else palette.cardElevatedBg,
                    border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) palette.activePillBorder else palette.cardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag(tag)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 11.5.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = if (strings.isHindi) 0.sp else 0.1.sp
                            ),
                            color = if (selected) palette.accentPrimary else palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
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
        val isInternalHome = currentSiteUrl.startsWith("netra://", ignoreCase = true) ||
            currentSiteUrl.startsWith("nova://", ignoreCase = true)
        val fromUrl = if (isInternalHome) "" else extractHostClean(currentSiteUrl)
        if (fromUrl.isNotBlank()) fromUrl
        else blockedAds.firstOrNull { it.tabId == activeTabId }?.pageDomain.orEmpty().ifBlank { "Netra" }
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
                    activeRules.forEach { ruleItem ->
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
                                AdBlockMode.PRO -> strings.shieldProEmptyDesc(activeRules.size, minimalTotal)
                                AdBlockMode.MAX -> strings.shieldMaxEmptyDesc(activeRules.size, proTotal)
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
                    filteredEntries.take(200).forEach { item ->
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
