package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

// Default Theme (Standard Mode)
private val NovaDefaultLightOrangeColorScheme = lightColorScheme(
    primary = IncognitoOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = IncognitoOrangePrimaryContainer,
    onPrimaryContainer = Color(0xFF381200),
    secondary = IncognitoOrangeSecondary,
    onSecondary = Color.White,
    secondaryContainer = IncognitoOrangeSecondaryContainer,
    onSecondaryContainer = Color(0xFF3E1000),
    tertiary = Color(0xFFBF360C),
    background = IncognitoOrangeBackground,
    onBackground = IncognitoOrangeTextPrimary,
    surface = IncognitoOrangeSurface,
    onSurface = IncognitoOrangeTextPrimary,
    surfaceVariant = IncognitoOrangeSurfaceVariant,
    onSurfaceVariant = IncognitoOrangeTextSecondary,
    error = Color(0xFFC62828),
    outline = IncognitoOrangeOutline
)

// Incognito Theme (When Incognito is ON): Cosmic Obsidian & Electric Cyan/Violet
private val NovaIncognitoDarkSapphireColorScheme = darkColorScheme(
    primary = NovaCyanPrimary,
    onPrimary = Color(0xFF002026),
    primaryContainer = NovaCyanContainer,
    onPrimaryContainer = Color(0xFFA8F5FF),
    secondary = NovaVioletSecondary,
    onSecondary = Color.White,
    secondaryContainer = NovaVioletContainer,
    onSecondaryContainer = Color(0xFFE5DAFF),
    tertiary = NovaEmeraldSafe,
    background = NovaDarkBackground,
    onBackground = NovaTextPrimaryDark,
    surface = NovaDarkSurface,
    onSurface = NovaTextPrimaryDark,
    surfaceVariant = NovaDarkSurfaceVariant,
    onSurfaceVariant = NovaTextSecondaryDark,
    error = NovaCrimsonError,
    outline = Color(0xFF2D3C6B)
)

@Composable
private fun animateColorScheme(target: ColorScheme): ColorScheme {
    val spec = tween<Color>(durationMillis = 460, easing = FastOutSlowInEasing)

    val primary by animateColorAsState(target.primary, spec, label = "primary")
    val onPrimary by animateColorAsState(target.onPrimary, spec, label = "onPrimary")
    val primaryContainer by animateColorAsState(target.primaryContainer, spec, label = "primaryContainer")
    val onPrimaryContainer by animateColorAsState(target.onPrimaryContainer, spec, label = "onPrimaryContainer")
    val secondary by animateColorAsState(target.secondary, spec, label = "secondary")
    val onSecondary by animateColorAsState(target.onSecondary, spec, label = "onSecondary")
    val secondaryContainer by animateColorAsState(target.secondaryContainer, spec, label = "secondaryContainer")
    val onSecondaryContainer by animateColorAsState(target.onSecondaryContainer, spec, label = "onSecondaryContainer")
    val tertiary by animateColorAsState(target.tertiary, spec, label = "tertiary")
    val background by animateColorAsState(target.background, spec, label = "background")
    val onBackground by animateColorAsState(target.onBackground, spec, label = "onBackground")
    val surface by animateColorAsState(target.surface, spec, label = "surface")
    val onSurface by animateColorAsState(target.onSurface, spec, label = "onSurface")
    val surfaceVariant by animateColorAsState(target.surfaceVariant, spec, label = "surfaceVariant")
    val onSurfaceVariant by animateColorAsState(target.onSurfaceVariant, spec, label = "onSurfaceVariant")
    val error by animateColorAsState(target.error, spec, label = "error")
    val outline by animateColorAsState(target.outline, spec, label = "outline")

    return target.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        error = error,
        outline = outline
    )
}

@Composable
fun NovaBrowserTheme(
    darkTheme: Boolean = true,
    isIncognito: Boolean = false,
    content: @Composable () -> Unit
) {
    // Light Orange is the Default Theme; Cosmic Dark Sapphire/Cyan is the Incognito Theme
    val targetScheme = if (isIncognito) {
        NovaIncognitoDarkSapphireColorScheme
    } else {
        NovaDefaultLightOrangeColorScheme
    }

    val animatedScheme = animateColorScheme(targetScheme)

    MaterialTheme(
        colorScheme = animatedScheme,
        typography = Typography,
        content = content
    )
}
