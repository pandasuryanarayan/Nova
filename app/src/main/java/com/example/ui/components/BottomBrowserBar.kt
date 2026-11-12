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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.browser.BrowserUrlUtils
import com.example.data.BackgroundMediaState
import com.example.data.BrowserTab
import com.example.data.NOVA_HOME_URL

/**
 * Lightweight Top Website Bar shown when any website is opened (`!activeTab.isHomePage`).
 * Displays:
 * 1. Lock icon button (opens Certificate, Available Cookies & Site Configuration sheet)
 * 2. Website Title (set by website)
 * 3. 1-Tap Bookmark Toggle Icon Button (placed on the right side near the dropdown button)
 * 4. Dropdown Icon Button that toggles a fall-down drawer showing the full website URL;
 *    re-clicking the dropdown icon smoothly collapses and vanishes the fall-down drawer.
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
    var urlInputText by remember(activeTab.url) {
        mutableStateOf(if (activeTab.url == NOVA_HOME_URL) "" else activeTab.url)
    }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isEditingTopUrl) {
        if (isEditingTopUrl) {
            urlInputText = if (activeTab.url == NOVA_HOME_URL) "" else activeTab.url
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
                    value = urlInputText,
                    onValueChange = { urlInputText = it },
                    placeholder = {
                        Text(
                            text = "Search or type website URL…",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            if (urlInputText.isNotBlank()) {
                                val query = urlInputText
                                isEditingTopUrl = false
                                isUrlDropdownExpanded = false
                                focusManager.clearFocus()
                                onSubmitUrlOrSearch(query)
                            }
                        }
                    ),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (urlInputText.isNotEmpty()) {
                                IconButton(onClick = { urlInputText = "" }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear URL"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val query = urlInputText
                                        isEditingTopUrl = false
                                        isUrlDropdownExpanded = false
                                        focusManager.clearFocus()
                                        onSubmitUrlOrSearch(query)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
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
                        imageVector = Icons.Filled.Close,
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
                // 1. Lock Icon Button (Certificate, Cookies & Site Config)
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
                                color = if (activeTab.isSecureSsl) {
                                    Color(0xFF00E676).copy(alpha = 0.14f)
                                } else {
                                    Color(0xFFFFB300).copy(alpha = 0.16f)
                                },
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (activeTab.isSecureSsl) {
                                Icons.Filled.Lock
                            } else {
                                Icons.Filled.Warning
                            },
                            contentDescription = "Site Security, Certificate, Cookies & Configuration",
                            tint = if (activeTab.isSecureSsl) {
                                Color(0xFF00E676)
                            } else {
                                Color(0xFFFFB300)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // 2. Website Title (set by website)
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

                // 3. 1-Tap Bookmark Toggle Button (placed on right side near the dropdown button)
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
                            .background(bookmarkBgColor, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) {
                                Icons.Filled.Bookmark
                            } else {
                                Icons.Filled.BookmarkBorder
                            },
                            contentDescription = if (isBookmarked) {
                                "Remove Bookmark"
                            } else {
                                "Bookmark This Website"
                            },
                            tint = bookmarkTint,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                // 4. Dropdown Icon Button to show/hide full Website Link fall-down
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
                            .background(dropdownBgColor, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isUrlDropdownExpanded) {
                                Icons.Filled.KeyboardArrowUp
                            } else {
                                Icons.Filled.KeyboardArrowDown
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
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Fall-down panel below Top Bar showing the full link of the website; vanishes on re-click
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

                Text(
                    text = "WEBSITE LINK",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f),
                            shape = RoundedCornerShape(12.dp)
                        )
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

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = { onCopyUrlClick(activeTab.url) },
                        modifier = Modifier.testTag("falldown_copy_url_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Link", style = MaterialTheme.typography.labelMedium)
                    }

                    TextButton(
                        onClick = { isEditingTopUrl = true },
                        modifier = Modifier.testTag("falldown_edit_url_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit", style = MaterialTheme.typography.labelMedium)
                    }

                    TextButton(
                        onClick = { onShareUrlClick(websiteTitle, activeTab.url) },
                        modifier = Modifier.testTag("falldown_share_url_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))

        // Loading progress bar along the bottom edge of the Top Bar
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
 * Lightweight Bottom Browser Bar optimized for budget phones:
 * - On the Home Screen (`activeTab.isHomePage`), only shows Tab View (`[N]`) and 3-Dot Menu on the right
 *   (no Left/Back icon, no Home button, and no duplicate bottom search bar).
 * - When a website is opened (`!activeTab.isHomePage`), the on-demand website controls
 *   (Left Arrow, Home, Inspect, Video Detector, Refresh/Stop) smoothly slide in from Left to Right,
 *   and reverse-slide Right to Left when returning to the Home page.
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
    onOpenTabsClick: () -> Unit,
    onOpenMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val devToolsBg by animateColorAsState(
        targetValue = if (isDevToolsVisible) Color(0xFF00E676).copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = tween(150),
        label = "devtools_bg"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
            .testTag("bottom_browser_bar")
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 12.dp)
        ) {
            // Left / Center area: On-Demand Website Toolbar (slides in Left-to-Right when website opens,
            // and reverse-slides Right-to-Left when returning to Home page)
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.weight(1f)
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !activeTab.isHomePage,
                    enter = slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 220)
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = 180)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                            .testTag("bottom_on_demand_website_actions")
                    ) {
                        // 1. Left Arrow (Back)
                        IconButton(
                            onClick = onBackClick,
                            enabled = activeTab.canGoBack || !activeTab.isHomePage,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("nav_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(22.dp)
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
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Home",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // 3. Inspect (DevTools)
                        IconButton(
                            onClick = onToggleDevToolsClick,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("address_bar_devtools_button")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(devToolsBg, RoundedCornerShape(12.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Code,
                                    contentDescription = "Inspect Page (DevTools)",
                                    tint = if (isDevToolsVisible) {
                                        Color(0xFF00E676)
                                    } else {
                                        MaterialTheme.colorScheme.secondary
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // 4. Video Detector
                        IconButton(
                            onClick = onScanPageVideosClick,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("address_bar_video_detect_button")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Videocam,
                                    contentDescription = "Detect Videos on Page",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
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
                                    Icons.Filled.Close
                                } else {
                                    Icons.Filled.Refresh
                                },
                                contentDescription = if (activeTab.isLoading) "Stop Loading" else "Refresh Page",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Right area (Always visible on both Home Screen and Website Mode):
            // 6. Tab View Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(13.dp))
                    .clickable { onOpenTabsClick() }
                    .testTag("tabs_fab_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Tab,
                    contentDescription = "Open Tabs ($tabsCount)",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "$tabsCount",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 7. 3-Dot Menu Button
            IconButton(
                onClick = onOpenMenuClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("browser_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Browser Menu",
                    modifier = Modifier.size(22.dp)
                )
            }
        }
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
            placeholder = { Text("Find in page…", style = MaterialTheme.typography.bodyMedium) },
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
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = "Previous Match"
            )
        }
        IconButton(onClick = onFindNext) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Next Match"
            )
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier.testTag("close_find_in_page_button")
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
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
                imageVector = Icons.Filled.Headphones,
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
                imageVector = if (mediaState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (mediaState.isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Stop Background Playback",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
