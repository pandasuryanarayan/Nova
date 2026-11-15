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

data class NetraColorScheme(
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
private val NetraDefaultLightOrangeColorScheme = NetraColorScheme(
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

// Normal Dark Theme: Pure AMOLED Black & Netra Orange
private val NetraAmoledDarkColorScheme = NetraColorScheme(
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
    error = NetraCrimsonError,
    outline = Color(0xFF272730)
)

// Incognito Theme: Cosmic Obsidian & Electric Cyan/Violet
private val NetraIncognitoDarkSapphireColorScheme = NetraColorScheme(
    primary = NetraCyanPrimary,
    onPrimary = Color(0xFF002026),
    primaryContainer = NetraCyanContainer,
    onPrimaryContainer = Color(0xFFA8F5FF),
    secondary = NetraVioletSecondary,
    onSecondary = Color.White,
    secondaryContainer = NetraVioletContainer,
    onSecondaryContainer = Color(0xFFE5DAFF),
    tertiary = NetraEmeraldSafe,
    background = NetraDarkBackground,
    onBackground = NetraTextPrimaryDark,
    surface = NetraDarkSurface,
    onSurface = NetraTextPrimaryDark,
    surfaceVariant = NetraDarkSurfaceVariant,
    onSurfaceVariant = NetraTextSecondaryDark,
    error = NetraCrimsonError,
    outline = Color(0xFF2D3C6B)
)

val LocalNetraColorScheme = staticCompositionLocalOf { NetraDefaultLightOrangeColorScheme }
val LocalNetraTypography = staticCompositionLocalOf { Typography }
val LocalContentColor = staticCompositionLocalOf { Color.Black }

object MaterialTheme {
    val colorScheme: NetraColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalNetraColorScheme.current

    val typography: NetraTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalNetraTypography.current
}

@Composable
fun NetraBrowserTheme(
    darkTheme: Boolean = false,
    isIncognito: Boolean = false,
    content: @Composable () -> Unit
) {
    val scheme = when {
        isIncognito -> NetraIncognitoDarkSapphireColorScheme
        darkTheme -> NetraAmoledDarkColorScheme
        else -> NetraDefaultLightOrangeColorScheme
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
        LocalNetraColorScheme provides scheme,
        LocalNetraTypography provides Typography,
        LocalContentColor provides scheme.onBackground,
        content = content
    )
}
