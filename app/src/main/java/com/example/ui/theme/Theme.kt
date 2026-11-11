package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NovaDarkColorScheme = darkColorScheme(
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

private val NovaIncognitoColorScheme = darkColorScheme(
    primary = IncognitoAccent,
    onPrimary = Color(0xFF240038),
    primaryContainer = Color(0xFF3E1E5A),
    onPrimaryContainer = Color(0xFFF5D9FF),
    secondary = NovaCyanPrimary,
    onSecondary = Color(0xFF002026),
    secondaryContainer = Color(0xFF2B2046),
    onSecondaryContainer = Color(0xFFE7D8FF),
    tertiary = NovaAmberWarning,
    background = IncognitoBackground,
    onBackground = Color(0xFFF7EFFF),
    surface = IncognitoSurface,
    onSurface = Color(0xFFF7EFFF),
    surfaceVariant = Color(0xFF2D214A),
    onSurfaceVariant = Color(0xFFC1AFDF),
    error = NovaCrimsonError,
    outline = Color(0xFF493675)
)

private val NovaLightColorScheme = lightColorScheme(
    primary = NovaBluePrimaryLight,
    onPrimary = Color.White,
    primaryContainer = NovaBlueContainerLight,
    onPrimaryContainer = Color(0xFF001F28),
    secondary = NovaVioletSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = NovaVioletContainerLight,
    onSecondaryContainer = Color(0xFF1C0563),
    tertiary = Color(0xFF00875A),
    background = NovaLightBackground,
    onBackground = NovaTextPrimaryLight,
    surface = NovaLightSurface,
    onSurface = NovaTextPrimaryLight,
    surfaceVariant = NovaLightSurfaceVariant,
    onSurfaceVariant = NovaTextSecondaryLight,
    error = Color(0xFFD32F2F),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun NovaBrowserTheme(
    darkTheme: Boolean = true,
    isIncognito: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isIncognito -> NovaIncognitoColorScheme
        darkTheme -> NovaDarkColorScheme
        else -> NovaLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
