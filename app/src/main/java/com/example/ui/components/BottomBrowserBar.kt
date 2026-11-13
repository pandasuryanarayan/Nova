package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
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

/**
 * Lightweight Top Website Bar shown when any website is opened (`!activeTab.isHomePage`).
 */
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
    var urlInputFieldValue by remember(activeTab.url) {
        val initText = if (activeTab.url == NOVA_HOME_URL) "" else activeTab.url
        mutableStateOf(
            TextFieldValue(
                text = initText,
                selection = TextRange(initText.length)
            )
        )
    }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isEditingTopUrl) {
        if (isEditingTopUrl) {
            val initText = if (activeTab.url == NOVA_HOME_URL) "" else activeTab.url
            urlInputFieldValue = TextFieldValue(
                text = initText,
                selection = TextRange(initText.length)
            )
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {
            }
        }
    }

    val websiteTitle = remember(activeTab.title, activeTab.url) {
        activeTab.title
            .takeIf { it.isNotBlank() && it != NOVA_HOME_URL }
            ?: BrowserUrlUtils.extractDomain(activeTab.url).ifBlank { activeTab.url }
    }

    val bookmarkBgColor by animateColorAsState(
        targetValue = if (isBookmarked) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        },
        animationSpec = tween(durationMillis = 150),
        label = "bookmark_bg"
    )

    val bookmarkTint by animateColorAsState(
        targetValue = if (isBookmarked) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 150),
        label = "bookmark_tint"
    )

    val dropdownBgColor by animateColorAsState(
        targetValue = if (isUrlDropdownExpanded) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        },
        animationSpec = tween(durationMillis = 150),
        label = "dropdown_bg"
    )

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
                OutlinedTextField(
                    value = urlInputFieldValue,
                    onValueChange = { urlInputFieldValue = it },
                    placeholder = {
                        Text(
                            text = novaStrings.searchOrTypeUrlPlaceholder,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            val query = urlInputFieldValue.text
                            if (query.isNotBlank()) {
                                isEditingTopUrl = false
                                isUrlDropdownExpanded = false
                                focusManager.clearFocus()
                                onSubmitUrlOrSearch(query)
                            }
                        }
                    ),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (urlInputFieldValue.text.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        urlInputFieldValue = TextFieldValue(
                                            text = "",
                                            selection = TextRange.Zero
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = NovaIcons.Close,
                                        contentDescription = "Clear URL"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val query = urlInputFieldValue.text
                                        isEditingTopUrl = false
                                        isUrlDropdownExpanded = false
                                        focusManager.clearFocus()
                                        onSubmitUrlOrSearch(query)
                                    }
                                ) {
                                    Icon(
                                        imageVector = NovaIcons.Send,
                                        contentDescription = "Go",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .testTag("top_bar_url_edit_input")
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        isEditingTopUrl = false
                        focusManager.clearFocus()
                    }
                ) {
                    Icon(
                        imageVector = NovaIcons.Close,
                        contentDescription = "Cancel URL Edit"
                    )
                }
            }
        } else {
            // Top Bar Row: Lock button + Website Title + 1-Tap Bookmark Button + Dropdown Link Button
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
                            .background(
                                color = lockBgColor,
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        Icon(
                            imageVector = if (activeTab.isSecureSsl) {
                                NovaIcons.Lock
                            } else {
                                NovaIcons.Warning
                            },
                            contentDescription = "Site Security, Certificate, Cookies & Configuration",
                            tint = lockTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Website Title
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            isUrlDropdownExpanded = !isUrlDropdownExpanded
                        }
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

                // 1-Tap Bookmark Toggle Button
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
                            imageVector = if (isBookmarked) {
                                NovaIcons.Bookmark
                            } else {
                                NovaIcons.BookmarkBorder
                            },
                            contentDescription = if (isBookmarked) {
                                "Remove Bookmark"
                            } else {
                                "Bookmark This Website"
                            },
                            tint = bookmarkTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Dropdown Icon Button
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
                            imageVector = if (isUrlDropdownExpanded) {
                                NovaIcons.KeyboardArrowUp
                            } else {
                                NovaIcons.KeyboardArrowDown
                            },
                            contentDescription = if (isUrlDropdownExpanded) {
                                "Hide Website Link"
                            } else {
                                "Show Website Link"
                            },
                            tint = if (isUrlDropdownExpanded) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Fall-down panel below Top Bar showing the full link of the website
        AnimatedVisibility(
            visible = isUrlDropdownExpanded && !isEditingTopUrl,
            enter = expandVertically(animationSpec = tween(160)) + fadeIn(animationSpec = tween(140)),
            exit = shrinkVertically(animationSpec = tween(150)) + fadeOut(animationSpec = tween(120))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("top_bar_url_falldown_panel")
            ) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
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
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(9.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(9.dp))
                                .clickable { onCopyUrlClick(activeTab.url) }
                                .testTag("falldown_copy_url_button")
                        ) {
                            Icon(
                                imageVector = NovaIcons.ContentCopy,
                                contentDescription = "Copy Link",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(9.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(9.dp))
                                .clickable { isEditingTopUrl = true }
                                .testTag("falldown_edit_url_button")
                        ) {
                            Icon(
                                imageVector = NovaIcons.Edit,
                                contentDescription = "Edit Link",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(9.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(9.dp))
                                .clickable { onShareUrlClick(websiteTitle, activeTab.url) }
                                .testTag("falldown_share_url_button")
                        ) {
                            Icon(
                                imageVector = NovaIcons.Share,
                                contentDescription = "Share Link",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = activeTab.url,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))

        AnimatedVisibility(
            visible = activeTab.isLoading,
            enter = fadeIn(animationSpec = tween(120)) + expandVertically(animationSpec = tween(120)),
            exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120))
        ) {
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

/**
 * Redesigned Bottom Navigation & Website Toolbar:
 * - On the Home Screen (`activeTab.isHomePage`): displays the 4-tab bar:
 *   `HOME` (active pill indicator), `SAVED` (Bookmarks), `TABS` (with count badge), and `SETTINGS` (Control center).
 * - When a website is opened (`!activeTab.isHomePage`): smoothly shows website actions
 *   (Back, Home, Inspect/DevTools, Video Detector, Refresh/Stop, Tabs `[N]`, Settings).
 */
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
            // 4-Tab Home Navigation Bar: HOME | SAVED | TABS | SETTINGS
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .padding(horizontal = 8.dp)
            ) {
                // 1. HOME (Active)
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

                // 2. SAVED (Bookmarks)
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

                // 3. TABS (with Count Badge)
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

                // 4. SETTINGS (Control Center / Menu)
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
            // On-Demand Website Toolbar when browsing any webpage
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 10.dp)
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 6.dp)
                            .testTag("bottom_on_demand_website_actions")
                    ) {
                        // 1. Back
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
                                tint = if (activeTab.canGoBack || !activeTab.isHomePage) {
                                    accentColor
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 2. Home
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

                        // 3. Inspect (DevTools)
                        IconButton(
                            onClick = onToggleDevToolsClick,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("address_bar_devtools_button")
                        ) {
                            Icon(
                                imageVector = NovaIcons.Code,
                                contentDescription = strings.inspectButton,
                                tint = if (isDevToolsVisible) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    accentColor
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 4. Video Detector
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

                        // 5. Refresh / Stop
                        IconButton(
                            onClick = onReloadOrStopClick,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("reload_stop_button")
                        ) {
                            Icon(
                                imageVector = if (activeTab.isLoading) {
                                    NovaIcons.Close
                                } else {
                                    NovaIcons.Refresh
                                },
                                contentDescription = if (activeTab.isLoading) "Stop Loading" else "Refresh Page",
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // 6. Tabs Button with Properly Aligned Tab Icon & Count
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

                // 7. Settings / Control Center Button
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
        // Top glowing indicator line when active
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(3.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) {
                        Brush.horizontalGradient(
                            listOf(activeColor, MaterialTheme.colorScheme.secondary)
                        )
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
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 0.8.sp
            ),
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
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(novaStrings.findInPageButton, style = MaterialTheme.typography.bodyMedium) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            trailingIcon = {
                if (query.isNotBlank()) {
                    Text(
                        text = "$activeOrdinal/$totalMatches",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            },
            modifier = Modifier
                .weight(1f)
                .testTag("find_in_page_input")
        )
        IconButton(onClick = onFindPrev) {
            Icon(
                imageVector = NovaIcons.KeyboardArrowUp,
                contentDescription = "Previous Match"
            )
        }
        IconButton(onClick = onFindNext) {
            Icon(
                imageVector = NovaIcons.KeyboardArrowDown,
                contentDescription = "Next Match"
            )
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier.testTag("close_find_in_page_button")
        ) {
            Icon(
                imageVector = NovaIcons.Close,
                contentDescription = "Close Find in Page"
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
        IconButton(onClick = onTogglePlayPause) {
            Icon(
                imageVector = if (mediaState.isPlaying) NovaIcons.Pause else NovaIcons.PlayArrow,
                contentDescription = if (mediaState.isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        IconButton(onClick = onClose) {
            Icon(
                imageVector = NovaIcons.Close,
                contentDescription = "Stop Background Playback",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
