package com.example.ui.components

import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.browser.BrowserUrlUtils
import com.example.data.BackgroundMediaState
import com.example.data.BrowserTab
import com.example.data.NOVA_HOME_URL
import com.example.ui.i18n.novaStrings
import com.example.ui.icons.NovaIcons
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MaterialTheme

@Composable
fun TopWebsiteHeaderBar(
    activeTab: BrowserTab,
    isBookmarked: Boolean,
    onSslIconClick: () -> Unit,
    onToggleBookmarkClick: () -> Unit,
    onSubmitUrlOrSearch: (String) -> Unit,
    onCopyUrlClick: (String) -> Unit,
    onShareUrlClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isUrlDropdownExpanded by remember(activeTab.id) { mutableStateOf(false) }
    var isEditingTopUrl by remember(activeTab.id) { mutableStateOf(false) }
    var urlInputText by remember(activeTab.url) {
        mutableStateOf(if (activeTab.url == NOVA_HOME_URL) "" else activeTab.url)
    }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isEditingTopUrl) {
        if (isEditingTopUrl) {
            urlInputText = if (activeTab.url == NOVA_HOME_URL) "" else activeTab.url
        }
    }

    val websiteTitle = remember(activeTab.title, activeTab.url) {
        activeTab.title
            .takeIf { it.isNotBlank() && it != NOVA_HOME_URL }
            ?: BrowserUrlUtils.extractDomain(activeTab.url).ifBlank { activeTab.url }
    }

    val bookmarkBgColor = if (isBookmarked) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
    }
    val bookmarkTint = if (isBookmarked) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val dropdownBgColor = if (isUrlDropdownExpanded) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .testTag("top_website_header_bar")
    ) {
        if (isEditingTopUrl) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                NovaEditText(
                    value = urlInputText,
                    onValueChange = { urlInputText = it },
                    placeholder = novaStrings.searchOrTypeUrlPlaceholder,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    borderColor = MaterialTheme.colorScheme.primary,
                    imeAction = EditorInfo.IME_ACTION_GO,
                    onImeAction = {
                        if (urlInputText.isNotBlank()) {
                            isEditingTopUrl = false
                            isUrlDropdownExpanded = false
                            focusManager.clearFocus()
                            onSubmitUrlOrSearch(urlInputText)
                        }
                    },
                    requestFocusOnMount = true,
                    trailingIcon = if (urlInputText.isNotEmpty()) {
                        {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { urlInputText = "" },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = NovaIcons.Close,
                                        contentDescription = "Clear URL",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        isEditingTopUrl = false
                                        isUrlDropdownExpanded = false
                                        focusManager.clearFocus()
                                        onSubmitUrlOrSearch(urlInputText)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = NovaIcons.Send,
                                        contentDescription = "Go",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        null
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("top_bar_url_edit_input")
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        isEditingTopUrl = false
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = NovaIcons.Close,
                        contentDescription = "Cancel URL Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 8.dp)
            ) {
                val lockBgColor = if (activeTab.isSecureSsl) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                } else {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.16f)
                }
                val lockTint = if (activeTab.isSecureSsl) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
                IconButton(
                    onClick = onSslIconClick,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("ssl_lock_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .background(lockBgColor, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = if (activeTab.isSecureSsl) NovaIcons.Lock else NovaIcons.Warning,
                            contentDescription = "Site Security, Certificate, Cookies & Configuration",
                            tint = lockTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isUrlDropdownExpanded = !isUrlDropdownExpanded }
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                        .testTag("top_bar_website_title")
                ) {
                    Text(
                        text = websiteTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onToggleBookmarkClick,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("top_bar_bookmark_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .background(bookmarkBgColor, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) NovaIcons.Bookmark else NovaIcons.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Remove Bookmark" else "Bookmark This Website",
                            tint = bookmarkTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                IconButton(
                    onClick = { isUrlDropdownExpanded = !isUrlDropdownExpanded },
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("top_bar_url_dropdown_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .background(dropdownBgColor, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = if (isUrlDropdownExpanded) NovaIcons.KeyboardArrowUp else NovaIcons.KeyboardArrowDown,
                            contentDescription = if (isUrlDropdownExpanded) "Hide Website Link" else "Show Website Link",
                            tint = if (isUrlDropdownExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (isUrlDropdownExpanded && !isEditingTopUrl) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("top_bar_url_falldown_panel")
            ) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = novaStrings.websiteLinkTitle,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UrlActionSmallBtn(
                            icon = NovaIcons.ContentCopy,
                            contentDesc = "Copy Link",
                            onClick = { onCopyUrlClick(activeTab.url) },
                            tag = "falldown_copy_url_button"
                        )
                        UrlActionSmallBtn(
                            icon = NovaIcons.Edit,
                            contentDesc = "Edit Link",
                            onClick = { isEditingTopUrl = true },
                            tag = "falldown_edit_url_button"
                        )
                        UrlActionSmallBtn(
                            icon = NovaIcons.Share,
                            contentDesc = "Share Link",
                            onClick = { onShareUrlClick(websiteTitle, activeTab.url) },
                            tag = "falldown_share_url_button"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .clickable { onCopyUrlClick(activeTab.url) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = activeTab.url,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        if (activeTab.isLoading) {
            LinearProgressIndicator(
                progress = { (activeTab.progress / 100f).coerceIn(0.05f, 1f) },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
            )
        }
    }
}

@Composable
private fun UrlActionSmallBtn(
    icon: ImageVector,
    contentDesc: String,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(9.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
fun BottomBrowserBar(
    activeTab: BrowserTab,
    tabsCount: Int,
    isDevToolsVisible: Boolean,
    onBackClick: () -> Unit,
    onForwardClick: () -> Unit,
    onHomeClick: () -> Unit,
    onReloadOrStopClick: () -> Unit,
    onSubmitUrlOrSearch: (String) -> Unit,
    onSslIconClick: () -> Unit,
    onToggleDevToolsClick: () -> Unit,
    onScanPageVideosClick: () -> Unit,
    onOpenBookmarksClick: () -> Unit = {},
    onOpenTabsClick: () -> Unit,
    onOpenMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = novaStrings
    val accentColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
            .testTag("bottom_browser_bar")
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

        if (activeTab.isHomePage) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .padding(horizontal = 8.dp)
            ) {
                BottomNavTabItem(
                    icon = NovaIcons.HomeFilled,
                    label = strings.navHome,
                    isSelected = true,
                    badgeCount = null,
                    onClick = onHomeClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nav_home_button")
                )

                BottomNavTabItem(
                    icon = NovaIcons.BookmarkBorder,
                    label = strings.navSaved,
                    isSelected = false,
                    badgeCount = null,
                    onClick = onOpenBookmarksClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bottom_saved_bookmarks_button")
                )

                BottomNavTabItem(
                    icon = NovaIcons.Tab,
                    label = strings.navTabs,
                    isSelected = false,
                    badgeCount = tabsCount,
                    onClick = onOpenTabsClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tabs_fab_button")
                )

                BottomNavTabItem(
                    icon = NovaIcons.Settings,
                    label = strings.navSettings,
                    isSelected = false,
                    badgeCount = null,
                    onClick = onOpenMenuClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("browser_menu_button")
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                        .testTag("bottom_on_demand_website_actions")
                ) {
                    IconButton(
                        onClick = onBackClick,
                        enabled = activeTab.canGoBack || !activeTab.isHomePage,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("nav_back_button")
                    ) {
                        Icon(
                            imageVector = NovaIcons.ArrowBack,
                            contentDescription = "Back",
                            tint = if (activeTab.canGoBack || !activeTab.isHomePage) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onHomeClick,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("nav_home_button")
                    ) {
                        Icon(
                            imageVector = NovaIcons.Home,
                            contentDescription = strings.navHome,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleDevToolsClick,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("address_bar_devtools_button")
                    ) {
                        Icon(
                            imageVector = NovaIcons.Code,
                            contentDescription = strings.inspectButton,
                            tint = if (isDevToolsVisible) MaterialTheme.colorScheme.secondary else accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onScanPageVideosClick,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("address_bar_video_detect_button")
                    ) {
                        Icon(
                            imageVector = NovaIcons.Videocam,
                            contentDescription = strings.videosButton,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onReloadOrStopClick,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("reload_stop_button")
                    ) {
                        Icon(
                            imageVector = if (activeTab.isLoading) NovaIcons.Close else NovaIcons.Refresh,
                            contentDescription = if (activeTab.isLoading) "Stop Loading" else "Refresh Page",
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
                        .border(1.dp, accentColor.copy(alpha = 0.42f), RoundedCornerShape(12.dp))
                        .clickable { onOpenTabsClick() }
                        .padding(horizontal = 10.dp)
                        .testTag("tabs_fab_button")
                ) {
                    Icon(
                        imageVector = NovaIcons.Tab,
                        contentDescription = "${strings.openTabsTitle} ($tabsCount)",
                        tint = accentColor,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "$tabsCount",
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onOpenMenuClick,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("browser_menu_button")
                ) {
                    Icon(
                        imageVector = NovaIcons.Settings,
                        contentDescription = strings.navSettings,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavTabItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    badgeCount: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
    val labelInactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(3.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) {
                        Brush.horizontalGradient(listOf(activeColor, MaterialTheme.colorScheme.secondary))
                    } else {
                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier.size(20.dp)
            )

            if (badgeCount != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 10.dp, y = (-5).dp)
                        .size(16.dp)
                        .background(activeColor, CircleShape)
                ) {
                    Text(
                        text = "$badgeCount",
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.8.sp),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isSelected) activeColor else labelInactiveColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FindInPageTopBar(
    query: String,
    activeOrdinal: Int,
    totalMatches: Int,
    onQueryChange: (String) -> Unit,
    onFindNext: () -> Unit,
    onFindPrev: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("find_in_page_bar")
    ) {
        NovaEditText(
            value = query,
            onValueChange = onQueryChange,
            placeholder = novaStrings.findInPageButton,
            shape = RoundedCornerShape(14.dp),
            imeAction = EditorInfo.IME_ACTION_SEARCH,
            onImeAction = onFindNext,
            trailingIcon = if (query.isNotBlank()) {
                {
                    Text(
                        text = "$activeOrdinal/$totalMatches",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            } else {
                null
            },
            modifier = Modifier
                .weight(1f)
                .testTag("find_in_page_input")
        )
        IconButton(onClick = onFindPrev, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = NovaIcons.KeyboardArrowUp,
                contentDescription = "Previous Match",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onFindNext, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = NovaIcons.KeyboardArrowDown,
                contentDescription = "Next Match",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(40.dp)
                .testTag("close_find_in_page_button")
        ) {
            Icon(
                imageVector = NovaIcons.Close,
                contentDescription = "Close Find in Page",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun BackgroundMediaMiniBar(
    mediaState: BackgroundMediaState,
    onTogglePlayPause: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("background_media_mini_bar")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(
                imageVector = NovaIcons.Headphones,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mediaState.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Background Audio • ${mediaState.domain}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
        IconButton(onClick = onTogglePlayPause, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = if (mediaState.isPlaying) NovaIcons.Pause else NovaIcons.PlayArrow,
                contentDescription = if (mediaState.isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onClose, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = NovaIcons.Close,
                contentDescription = "Stop Background Playback",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
