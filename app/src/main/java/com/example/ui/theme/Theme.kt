package com.example.ui.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class NovaColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val error: Color,
    val outline: Color
)

// Default Normal Light Theme: Warm Orange
private val NovaDefaultLightOrangeColorScheme = NovaColorScheme(
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

// Normal Dark Theme: Pure AMOLED Black & Nova Orange
private val NovaAmoledDarkColorScheme = NovaColorScheme(
    primary = Color(0xFFFF8A3D),
    onPrimary = Color(0xFF1F0A00),
    primaryContainer = Color(0xFF331808),
    onPrimaryContainer = Color(0xFFFFD6B8),
    secondary = Color(0xFFFF6D00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF281205),
    onSecondaryContainer = Color(0xFFFFD3B6),
    tertiary = Color(0xFFFFAB40),
    background = Color(0xFF000000),
    onBackground = Color(0xFFF5F5F7),
    surface = Color(0xFF09090B),
    onSurface = Color(0xFFF5F5F7),
    surfaceVariant = Color(0xFF141418),
    onSurfaceVariant = Color(0xFFA6A6B2),
    error = NovaCrimsonError,
    outline = Color(0xFF272730)
)

// Incognito Theme: Cosmic Obsidian & Electric Cyan/Violet
private val NovaIncognitoDarkSapphireColorScheme = NovaColorScheme(
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

val LocalNovaColorScheme = staticCompositionLocalOf { NovaDefaultLightOrangeColorScheme }
val LocalNovaTypography = staticCompositionLocalOf { Typography }
val LocalContentColor = staticCompositionLocalOf { Color.Black }

object MaterialTheme {
    val colorScheme: NovaColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalNovaColorScheme.current

    val typography: NovaTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalNovaTypography.current
}

@Composable
fun NovaBrowserTheme(
    darkTheme: Boolean = false,
    isIncognito: Boolean = false,
    content: @Composable () -> Unit
) {
    val scheme = when {
        isIncognito -> NovaIncognitoDarkSapphireColorScheme
        darkTheme -> NovaAmoledDarkColorScheme
        else -> NovaDefaultLightOrangeColorScheme
    }

    val view = LocalView.current
    val useDarkStatusIcons = !isIncognito && !darkTheme
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = useDarkStatusIcons
            insetsController.isAppearanceLightNavigationBars = useDarkStatusIcons
        }
    }

    CompositionLocalProvider(
        LocalNovaColorScheme provides scheme,
        LocalNovaTypography provides Typography,
        LocalContentColor provides scheme.onBackground,
        content = content
    )
}
