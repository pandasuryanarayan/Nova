package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.QuickShortcutEntity

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NovaHomeContent(
    isIncognito: Boolean,
    isAdBlockEnabled: Boolean,
    totalAdsBlocked: Long,
    downloadsCount: Int,
    bookmarksCount: Int,
    historyCount: Int,
    shortcuts: List<QuickShortcutEntity>,
    onSearchOrLoadUrl: (String) -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenBlockedAds: () -> Unit = {},
    onToggleIncognito: () -> Unit,
    onAddShortcutClick: () -> Unit,
    onDeleteShortcut: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var homeSearchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val shortcutRows = remember(shortcuts) { shortcuts.chunked(4) }
    val incognitoPillBg by animateColorAsState(
        targetValue = if (isIncognito) {
            Color(0xFFD580FF).copy(alpha = 0.22f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
        },
        animationSpec = tween(durationMillis = 160),
        label = "incognito_pill_bg"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .testTag("nova_home_screen"),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Top Status Bar Row (Ad-Block Shield Pill on Left + Incognito Toggle Pill on Right)
            item(key = "top_status_row", contentType = "status_row") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Ad-Block Shield Status Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                color = Color(0xFF00E5FF).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(50)
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFF00E5FF).copy(alpha = 0.35f),
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { onOpenBlockedAds() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("home_blocked_ads_badge")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAdBlockEnabled) {
                                "$totalAdsBlocked Ads Blocked"
                            } else {
                                "Ad-Shield Off"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Right: Incognito / Private Mode Toggle Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(incognitoPillBg, RoundedCornerShape(50))
                            .border(
                                width = 1.dp,
                                color = if (isIncognito) {
                                    Color(0xFFD580FF).copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                },
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { onToggleIncognito() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("home_incognito_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle Incognito Mode",
                            tint = if (isIncognito) Color(0xFFD580FF) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isIncognito) "Incognito On" else "Incognito",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isIncognito) Color(0xFFD580FF) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 2. Chrome-Style Centric App Icon & Large Brand Identity + Middle Omnibox Search Bar
            item(key = "centric_brand_and_search", contentType = "brand_search") {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp)
                ) {
                    // Concentric Glowing App Icon Badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(88.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background( Color(0xFF0B1021), RoundedCornerShape(22.dp))
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF00E5FF),
                                            Color(0xFF8C62FF)
                                        )
                                    ),
                                    shape = RoundedCornerShape(22.dp)
                                )
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_nova_launcher_fg_1794331213367),
                                contentDescription = "Nova Browser Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(18.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Large Prominent App Name (Chrome/Google style placement)
                    Text(
                        text = if (isIncognito) "Nova Private" else "Nova Browser",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isIncognito) {
                            "Zero-trace private browsing • History & cookies disabled"
                        } else {
                            "Fast, private & ad-free web browsing"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Prominent Middle Search / URL Omnibox
                    OutlinedTextField(
                        value = homeSearchQuery,
                        onValueChange = { homeSearchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search Google or type URL…",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        trailingIcon = {
                            if (homeSearchQuery.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { homeSearchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Clear",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            val q = homeSearchQuery
                                            homeSearchQuery = ""
                                            focusManager.clearFocus()
                                            onSearchOrLoadUrl(q)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayCircleFilled,
                                            contentDescription = "Go",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                if (homeSearchQuery.isNotBlank()) {
                                    val q = homeSearchQuery
                                    homeSearchQuery = ""
                                    focusManager.clearFocus()
                                    onSearchOrLoadUrl(q)
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_input")
                    )
                }
            }

            // 3. Hub Cards: Downloads, Bookmarks, History
            item(key = "hub_cards", contentType = "hub") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeHubActionCard(
                        icon = Icons.Filled.Download,
                        title = "Downloads",
                        badge = "$downloadsCount files",
                        accentColor = MaterialTheme.colorScheme.primary,
                        onClick = onOpenDownloads,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_downloads_button")
                    )
                    HomeHubActionCard(
                        icon = Icons.Filled.Bookmarks,
                        title = "Bookmarks",
                        badge = "$bookmarksCount saved",
                        accentColor = MaterialTheme.colorScheme.secondary,
                        onClick = onOpenBookmarks,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_bookmarks_button")
                    )
                    HomeHubActionCard(
                        icon = Icons.Filled.History,
                        title = "History",
                        badge = "$historyCount visits",
                        accentColor = Color(0xFFFFB300),
                        onClick = onOpenHistory,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_history_button")
                    )
                }
            }

            // 4. Quick Shortcuts Grid Header
            item(key = "shortcuts_header", contentType = "header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Shortcuts",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp))
                            .clickable { onAddShortcutClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("add_shortcut_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Shortcut",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Site",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 5. Lightweight Quick Shortcuts 4-Column Grid Rows
            item(key = "shortcuts_grid", contentType = "grid") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    shortcutRows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { item ->
                                val itemAccent = remember(item.accentColorHex) { Color(item.accentColorHex) }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .combinedClickable(
                                            onClick = { onSearchOrLoadUrl(item.url) },
                                            onLongClick = { onDeleteShortcut(item.id) }
                                        )
                                        .padding(vertical = 12.dp, horizontal = 6.dp)
                                        .testTag("shortcut_card_${item.id}")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(itemAccent.copy(alpha = 0.16f), CircleShape)
                                    ) {
                                        Text(
                                            text = item.badgeText.take(4),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = itemAccent
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = item.subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            repeat(4 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // 6. Incognito Mode Notice Footer (if active)
            if (isIncognito) {
                item(key = "incognito_footer", contentType = "footer") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Policy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Private Tab Active: Pages you view in this tab won't stick around in your browser history, cookie store, or search history.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHubActionCard(
    icon: ImageVector,
    title: String,
    badge: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .background(accentColor.copy(alpha = 0.16f), CircleShape)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1
        )
        Text(
            text = badge,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
