package com.example.ui.components

import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.browser.rememberShortcutIcon
import com.example.data.AdBlockMode
import com.example.data.QuickShortcutEntity
import com.example.ui.i18n.novaStrings
import com.example.ui.icons.NovaIcons
import com.example.ui.theme.MaterialTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NovaHomeContent(
    isIncognito: Boolean,
    isAdBlockEnabled: Boolean,
    adBlockMode: AdBlockMode = AdBlockMode.MINIMAL,
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
    onEditShortcut: (QuickShortcutEntity, String, String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = novaStrings
    var homeSearchQuery by remember { mutableStateOf("") }
    var editingShortcut by remember { mutableStateOf<QuickShortcutEntity?>(null) }
    val focusManager = LocalFocusManager.current
    val shortcutRows = remember(shortcuts) { shortcuts.chunked(4) }

    val incognitoAccent = Color(0xFF00E5FF)
    val incognitoPillBg = if (isIncognito) {
        Color(0xFF00E5FF).copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
    }
    val adBadgeBg = if (isIncognito) {
        Color(0xFF00E5FF).copy(alpha = 0.14f)
    } else {
        Color(0xFFFF8A3D).copy(alpha = 0.20f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp)
                .testTag("nova_home_screen"),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Brand Icon & Title + Omnibox + Split Pills
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 4.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(86.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
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
                            .background(
                                color = if (isIncognito) Color(0xFF0B1021) else Color(0xFF3E1800),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .border(
                                width = 1.8.dp,
                                brush = Brush.linearGradient(
                                    colors = if (isIncognito) {
                                        listOf(Color(0xFF00E5FF), Color(0xFF8C62FF))
                                    } else {
                                        listOf(Color(0xFFFF6D00), Color(0xFFFFAB40))
                                    }
                                ),
                                shape = RoundedCornerShape(22.dp)
                            )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_nova_launcher_fg),
                            contentDescription = "Nova Browser Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(22.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isIncognito) strings.appTitlePrivate else strings.appTitleStandard,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 31.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isIncognito) strings.appSubtitlePrivate else strings.appSubtitleStandard,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                NovaEditText(
                    value = homeSearchQuery,
                    onValueChange = { homeSearchQuery = it },
                    placeholder = strings.searchPlaceholder,
                    shape = RoundedCornerShape(28.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                    imeAction = EditorInfo.IME_ACTION_GO,
                    onImeAction = {
                        if (homeSearchQuery.isNotBlank()) {
                            val q = homeSearchQuery
                            homeSearchQuery = ""
                            focusManager.clearFocus()
                            onSearchOrLoadUrl(q)
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = NovaIcons.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = if (homeSearchQuery.isNotBlank()) {
                        {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { homeSearchQuery = "" },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = NovaIcons.Close,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val q = homeSearchQuery
                                        homeSearchQuery = ""
                                        focusManager.clearFocus()
                                        onSearchOrLoadUrl(q)
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = NovaIcons.PlayCircleFilled,
                                        contentDescription = "Go",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        null
                    },
                    textSizeSp = 15f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Split Buttons: [Ads Blocked • Mode] | [Incognito]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(adBadgeBg, RoundedCornerShape(16.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onOpenBlockedAds() }
                            .padding(horizontal = 12.dp)
                            .testTag("home_blocked_ads_badge")
                    ) {
                        Icon(
                            imageVector = NovaIcons.ShieldOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = if (isAdBlockEnabled) {
                                "$totalAdsBlocked ${strings.adsLabel} • ${strings.adBlockModeLabel(adBlockMode)}"
                            } else {
                                strings.adShieldOff
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(incognitoPillBg, RoundedCornerShape(16.dp))
                            .border(
                                width = 1.dp,
                                color = if (isIncognito) {
                                    incognitoAccent.copy(alpha = 0.65f)
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                },
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onToggleIncognito() }
                            .padding(horizontal = 12.dp)
                            .testTag("home_incognito_toggle")
                    ) {
                        Icon(
                            imageVector = NovaIcons.Incognito,
                            contentDescription = "Toggle Incognito Mode",
                            tint = if (isIncognito) incognitoAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = if (isIncognito) strings.incognitoOn else strings.incognitoLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isIncognito) incognitoAccent else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 2. Hub Cards: Downloads, Bookmarks, History
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeHubActionCard(
                    icon = NovaIcons.Download,
                    title = strings.downloadsTitle,
                    badge = "$downloadsCount ${strings.filesCountSuffix}",
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick = onOpenDownloads,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_downloads_button")
                )
                HomeHubActionCard(
                    icon = NovaIcons.Bookmarks,
                    title = strings.bookmarksTitle,
                    badge = "$bookmarksCount ${strings.savedCountSuffix}",
                    accentColor = MaterialTheme.colorScheme.secondary,
                    onClick = onOpenBookmarks,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_bookmarks_button")
                )
                HomeHubActionCard(
                    icon = NovaIcons.History,
                    title = strings.historyTitle,
                    badge = "$historyCount ${strings.visitsCountSuffix}",
                    accentColor = if (isIncognito) Color(0xFFFFB300) else Color(0xFFE65100),
                    onClick = onOpenHistory,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_history_button")
                )
            }

            // 3. Quick Shortcuts Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.quickShortcutsTitle,
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
                        imageVector = NovaIcons.Add,
                        contentDescription = strings.addSiteButton,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = strings.addSiteButton,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // 4. Quick Shortcuts Grid (4 columns)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                shortcutRows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { item ->
                            QuickShortcutTile(
                                item = item,
                                context = context,
                                onOpen = { onSearchOrLoadUrl(item.url) },
                                onEdit = { editingShortcut = item },
                                onDelete = { onDeleteShortcut(item.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(4 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // 5. Incognito Mode Notice Footer
            if (isIncognito) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp)
                ) {
                    Icon(
                        imageVector = NovaIcons.ShieldCheck,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.privateTabNotice,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }

    editingShortcut?.let { target ->
        var editTitle by remember(target.id) { mutableStateOf(target.title) }
        var editUrl by remember(target.id) { mutableStateOf(target.url) }

        AlertDialog(
            onDismissRequest = { editingShortcut = null },
            title = {
                Text(
                    text = strings.editShortcutDialogTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NovaEditText(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = strings.siteTitleLabel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_shortcut_title_input")
                    )
                    NovaEditText(
                        value = editUrl,
                        onValueChange = { editUrl = it },
                        label = strings.websiteUrlLabel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_shortcut_url_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editUrl.isNotBlank()) {
                            onEditShortcut(target, editTitle, editUrl)
                            editingShortcut = null
                        }
                    },
                    modifier = Modifier.testTag("edit_shortcut_save_button")
                ) {
                    Text(strings.saveButton, color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingShortcut = null }) {
                    Text(strings.cancelButton)
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickShortcutTile(
    item: QuickShortcutEntity,
    context: android.content.Context,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val itemAccent = remember(item.accentColorHex) { Color(item.accentColorHex) }
    val cachedIcon by rememberShortcutIcon(context = context, url = item.url)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onClick = onOpen,
                onLongClick = { menuExpanded = true }
            )
            .testTag("shortcut_card_${item.id}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 10.dp, start = 6.dp, end = 6.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = if (cachedIcon != null) Color.White.copy(alpha = 0.92f) else itemAccent.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(13.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(13.dp)
                    )
            ) {
                val iconBitmap = cachedIcon
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = "${item.title} Icon",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                } else {
                    Text(
                        text = item.badgeText.take(4),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = itemAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
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

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 2.dp, end = 2.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable { menuExpanded = true }
                    .testTag("shortcut_menu_button_${item.id}")
            ) {
                Icon(
                    imageVector = NovaIcons.Settings,
                    contentDescription = "Edit or Delete ${item.title}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                val strings = novaStrings
                DropdownMenuItem(
                    text = { Text(strings.editAction) },
                    leadingIcon = {
                        Icon(
                            imageVector = NovaIcons.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onEdit()
                    },
                    modifier = Modifier.testTag("shortcut_edit_option_${item.id}")
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = strings.deleteAction,
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = NovaIcons.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    },
                    modifier = Modifier.testTag("shortcut_delete_option_${item.id}")
                )
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
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
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = badge,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
