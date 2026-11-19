package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.data.AppLanguage
import com.example.data.BrowserTab
import com.example.data.SavedPasswordEntity
import com.example.ui.i18n.LocalNetraStrings
import com.example.ui.icons.NetraIcons
import com.example.ui.theme.MaterialTheme

@Composable
internal fun AdvancedSettingsOverlayContent(
    activeTab: BrowserTab,
    appLanguage: AppLanguage,
    isPasswordManagerEnabled: Boolean,
    savedPasswords: List<SavedPasswordEntity>,
    palette: SheetVisualPalette,
    onSelectAppLanguage: (AppLanguage) -> Unit,
    onTogglePasswordManager: () -> Unit,
    onSavePassword: (id: Long, siteName: String, domainOrUrl: String, username: String, password: String) -> Unit,
    onDeletePassword: (Long) -> Unit,
    onAutofillPassword: (SavedPasswordEntity) -> Unit,
    onCopyPasswordText: (label: String, value: String) -> Unit,
    onGeneratePassword: () -> String
) {
    val strings = LocalNetraStrings.current
    var searchQuery by remember { mutableStateOf("") }
    val revealedIds = remember { mutableStateMapOf<Long, Boolean>() }
    var editingEntry by remember { mutableStateOf<SavedPasswordEntity?>(null) }
    var isEditorOpen by remember { mutableStateOf(false) }

    val activeDomain = remember(activeTab.url, activeTab.isHomePage) {
        if (activeTab.isHomePage) "" else extractHostClean(activeTab.url).lowercase()
    }

    val filteredPasswords = remember(savedPasswords, searchQuery, activeDomain) {
        val q = searchQuery.trim().lowercase()
        val base = if (q.isEmpty()) {
            savedPasswords
        } else {
            savedPasswords.filter {
                it.siteName.lowercase().contains(q) ||
                    it.domain.lowercase().contains(q) ||
                    it.username.lowercase().contains(q)
            }
        }
        if (activeDomain.isBlank()) {
            base
        } else {
            base.sortedByDescending {
                it.domain.equals(activeDomain, ignoreCase = true) ||
                    activeDomain.endsWith(".${it.domain.lowercase()}")
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = palette.sheetBg,
        border = BorderStroke(1.5.dp, palette.activePillBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("advanced_settings_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Advanced Settings Header + Netra (English / हिन्दी) & Android Version Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = palette.cardElevatedBg,
                border = BorderStroke(1.dp, palette.badgeBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(palette.badgeBg)
                                .border(1.dp, palette.badgeBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_advanced_settings),
                                contentDescription = strings.advancedSettingsTitle,
                                colorFilter = ColorFilter.tint(palette.accentPrimary),
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.advancedSettingsTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = palette.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = strings.advancedSettingsSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    HorizontalDivider(color = palette.cardBorder.copy(alpha = 0.7f))

                    // Netra (localized EN / HI) + App Version + Android text row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("advanced_netra_android_version"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = strings.appTitleStandard,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = palette.accentPrimary
                            )
                            if (strings.isHindi) {
                                Text(
                                    text = "(Netra)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.textSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = palette.activePillBg,
                            border = BorderStroke(1.dp, palette.activePillBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = NetraIcons.Android,
                                    contentDescription = strings.androidPlatformText,
                                    tint = palette.accentPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${strings.androidPlatformText} • v${BuildConfig.VERSION_NAME}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = palette.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 2. App Language Settings (Shifted here from Main Settings Sheet)
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
                            Icon(NetraIcons.Translate, null, tint = palette.accentPrimary, modifier = Modifier.size(19.dp))
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
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) palette.activePillBorder else palette.cardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
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

            // 3. Password Manager Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = palette.cardBg,
                border = BorderStroke(1.dp, palette.cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_manager_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(palette.cardElevatedBg)
                                    .border(1.dp, palette.cardBorder, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(NetraIcons.Key, null, tint = palette.accentPrimary, modifier = Modifier.size(19.dp))
                            }
                            Column {
                                Text(
                                    text = strings.passwordManagerTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                                    color = palette.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = strings.passwordsSavedCount(savedPasswords.size),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = palette.textSecondary
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                editingEntry = null
                                isEditorOpen = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = palette.accentPrimary,
                            modifier = Modifier.testTag("add_password_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(NetraIcons.Add, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = strings.addButton,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    // Auto-save & Autofill Toggle
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = palette.cardElevatedBg,
                        border = BorderStroke(1.dp, palette.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTogglePasswordManager() }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 10.dp)
                            ) {
                                Text(
                                    text = strings.passwordManagerAutoSaveTitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.textPrimary
                                )
                                Text(
                                    text = strings.passwordManagerAutoSaveSub,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textMuted
                                )
                            }
                            Switch(
                                checked = isPasswordManagerEnabled,
                                onCheckedChange = { onTogglePasswordManager() },
                                checkedTrackColor = palette.accentPrimary,
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.testTag("password_manager_switch")
                            )
                        }
                    }

                    if (savedPasswords.isNotEmpty() || searchQuery.isNotBlank()) {
                        NetraEditText(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = strings.passwordManagerSearchPlaceholder,
                            leadingIcon = {
                                Icon(NetraIcons.Search, null, tint = palette.textMuted, modifier = Modifier.size(17.dp))
                            },
                            trailingIcon = if (searchQuery.isNotEmpty()) {
                                {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { searchQuery = "" },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(NetraIcons.Close, "Clear search", tint = palette.textSecondary, modifier = Modifier.size(15.dp))
                                    }
                                }
                            } else null,
                            containerColor = palette.cardElevatedBg,
                            borderColor = palette.cardBorder,
                            textColor = palette.textPrimary,
                            hintColor = palette.textMuted,
                            minHeight = 44.dp,
                            verticalPadding = 4.dp,
                            modifier = Modifier.testTag("password_search_input")
                        )
                    }

                    if (filteredPasswords.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(palette.cardElevatedBg.copy(alpha = 0.7f))
                                .border(1.dp, palette.cardBorder, RoundedCornerShape(14.dp))
                                .padding(horizontal = 16.dp, vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = NetraIcons.Lock,
                                contentDescription = null,
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                            Text(
                                text = strings.passwordManagerEmptyTitle,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = strings.passwordManagerEmptySubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            filteredPasswords.forEach { entry ->
                                val isRevealed = revealedIds[entry.id] == true
                                val isCurrentSite = activeDomain.isNotBlank() &&
                                    (entry.domain.equals(activeDomain, ignoreCase = true) || activeDomain.endsWith(".${entry.domain.lowercase()}"))
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isCurrentSite) palette.activePillBg.copy(alpha = 0.45f) else palette.cardElevatedBg,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isCurrentSite) palette.activePillBorder else palette.cardBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("password_item_${entry.id}")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            MonogramSiteBadge(
                                                label = entry.siteName.ifBlank { entry.domain },
                                                url = entry.url.ifBlank { "https://${entry.domain}" }
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = entry.siteName.ifBlank { entry.domain },
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = palette.textPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = if (entry.username.isNotBlank()) "${entry.domain} • ${entry.username}" else entry.domain,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = palette.textSecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = if (isRevealed) entry.password else "•".repeat(entry.password.length.coerceIn(8, 14)),
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    color = if (isRevealed) palette.accentPrimary else palette.textMuted,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            // Autofill / Open button
                                            Surface(
                                                onClick = { onAutofillPassword(entry) },
                                                shape = RoundedCornerShape(10.dp),
                                                color = palette.activePillBg,
                                                border = BorderStroke(1.dp, palette.activePillBorder),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(36.dp)
                                                    .testTag("autofill_password_button_${entry.id}")
                                            ) {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                    Text(
                                                        text = strings.autofillButton,
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = palette.accentPrimary,
                                                        maxLines = 1
                                                    )
                                                }
                                            }

                                            // Copy Password button
                                            Surface(
                                                onClick = { onCopyPasswordText(strings.passwordLabel, entry.password) },
                                                shape = RoundedCornerShape(10.dp),
                                                color = palette.cardBg,
                                                border = BorderStroke(1.dp, palette.cardBorder),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(36.dp)
                                                    .testTag("copy_password_button_${entry.id}")
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxSize(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(NetraIcons.ContentCopy, null, tint = palette.textPrimary, modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = strings.copyPasswordLabel,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                        color = palette.textPrimary,
                                                        maxLines = 1
                                                    )
                                                }
                                            }

                                            // Show / Hide eye icon
                                            Surface(
                                                onClick = { revealedIds[entry.id] = !isRevealed },
                                                shape = RoundedCornerShape(10.dp),
                                                color = palette.cardBg,
                                                border = BorderStroke(1.dp, palette.cardBorder),
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .testTag("toggle_password_visibility_${entry.id}")
                                            ) {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                    Icon(
                                                        imageVector = if (isRevealed) NetraIcons.Incognito else NetraIcons.Visibility,
                                                        contentDescription = if (isRevealed) "Hide Password" else "Show Password",
                                                        tint = palette.accentPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            // Edit button
                                            Surface(
                                                onClick = {
                                                    editingEntry = entry
                                                    isEditorOpen = true
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                color = palette.cardBg,
                                                border = BorderStroke(1.dp, palette.cardBorder),
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .testTag("edit_password_button_${entry.id}")
                                            ) {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                    Icon(NetraIcons.Edit, strings.editAction, tint = palette.textSecondary, modifier = Modifier.size(16.dp))
                                                }
                                            }

                                            // Delete button
                                            Surface(
                                                onClick = { onDeletePassword(entry.id) },
                                                shape = RoundedCornerShape(10.dp),
                                                color = palette.dangerBg,
                                                border = BorderStroke(1.dp, palette.dangerBorder),
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .testTag("delete_password_button_${entry.id}")
                                            ) {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                    Icon(NetraIcons.DeleteOutline, strings.deleteAction, tint = palette.dangerRed, modifier = Modifier.size(16.dp))
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
    }

    if (isEditorOpen) {
        val prefillDomain = editingEntry?.domain ?: activeDomain
        val prefillSite = editingEntry?.siteName ?: activeTab.title.takeIf { !activeTab.isHomePage && it.isNotBlank() }.orEmpty().ifBlank { prefillDomain }
        PasswordEditorDialog(
            initialEntry = editingEntry,
            defaultSiteName = prefillSite,
            defaultDomainOrUrl = prefillDomain,
            palette = palette,
            onGeneratePassword = onGeneratePassword,
            onConfirm = { siteName, domainOrUrl, username, password ->
                onSavePassword(editingEntry?.id ?: 0L, siteName, domainOrUrl, username, password)
                isEditorOpen = false
                editingEntry = null
            },
            onDismiss = {
                isEditorOpen = false
                editingEntry = null
            }
        )
    }
}

@Composable
private fun PasswordEditorDialog(
    initialEntry: SavedPasswordEntity?,
    defaultSiteName: String,
    defaultDomainOrUrl: String,
    palette: SheetVisualPalette,
    onGeneratePassword: () -> String,
    onConfirm: (siteName: String, domainOrUrl: String, username: String, password: String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalNetraStrings.current
    var siteOrUrl by remember(initialEntry) { mutableStateOf(initialEntry?.url?.ifBlank { initialEntry.domain } ?: defaultDomainOrUrl) }
    var siteTitle by remember(initialEntry) { mutableStateOf(initialEntry?.siteName ?: defaultSiteName) }
    var username by remember(initialEntry) { mutableStateOf(initialEntry?.username ?: "") }
    var password by remember(initialEntry) { mutableStateOf(initialEntry?.password ?: "") }
    var showPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            SheetHeaderIconBadge(icon = NetraIcons.Key, palette = palette)
        },
        title = {
            Text(
                text = if (initialEntry != null) strings.editPasswordTitle else strings.addPasswordTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                NetraEditText(
                    value = siteOrUrl,
                    onValueChange = { siteOrUrl = it },
                    label = strings.siteOrDomainLabel,
                    placeholder = "example.com",
                    containerColor = palette.cardElevatedBg,
                    borderColor = palette.cardBorder,
                    textColor = palette.textPrimary,
                    hintColor = palette.textMuted,
                    modifier = Modifier.testTag("password_editor_domain_input")
                )
                NetraEditText(
                    value = siteTitle,
                    onValueChange = { siteTitle = it },
                    label = strings.siteTitleLabel,
                    placeholder = "Site Name",
                    containerColor = palette.cardElevatedBg,
                    borderColor = palette.cardBorder,
                    textColor = palette.textPrimary,
                    hintColor = palette.textMuted,
                    modifier = Modifier.testTag("password_editor_title_input")
                )
                NetraEditText(
                    value = username,
                    onValueChange = { username = it },
                    label = strings.usernameOrEmailLabel,
                    placeholder = "name@example.com",
                    containerColor = palette.cardElevatedBg,
                    borderColor = palette.cardBorder,
                    textColor = palette.textPrimary,
                    hintColor = palette.textMuted,
                    modifier = Modifier.testTag("password_editor_username_input")
                )
                NetraEditText(
                    value = password,
                    onValueChange = { password = it },
                    label = strings.passwordLabel,
                    placeholder = "••••••••••••",
                    monospace = true,
                    isPasswordMasked = !showPassword,
                    containerColor = palette.cardElevatedBg,
                    borderColor = palette.cardBorder,
                    textColor = palette.textPrimary,
                    hintColor = palette.textMuted,
                    trailingIcon = {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showPassword = !showPassword },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (showPassword) NetraIcons.Incognito else NetraIcons.Visibility,
                                contentDescription = if (showPassword) "Hide Password" else "Show Password",
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    modifier = Modifier.testTag("password_editor_password_input")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        onClick = {
                            password = onGeneratePassword()
                            showPassword = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = palette.activePillBg,
                        border = BorderStroke(1.dp, palette.activePillBorder),
                        modifier = Modifier.testTag("generate_password_button")
                    ) {
                        Text(
                            text = "⚡ ${strings.generatePasswordButton}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.accentPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(siteTitle, siteOrUrl, username, password) },
                enabled = siteOrUrl.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.testTag("password_editor_save_button")
            ) {
                Text(strings.saveButton)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelButton)
            }
        }
    )
}
