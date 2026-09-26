package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = KaraPrimary,
    onPrimary = Color(0xFF1E0054),
    primaryContainer = KaraSurfaceVariantDark,
    onPrimaryContainer = KaraTextPrimary,
    secondary = KaraSecondary,
    onSecondary = Color(0xFF4A0025),
    secondaryContainer = Color(0xFF3B2035),
    onSecondaryContainer = Color(0xFFFFD8E4),
    tertiary = KaraTertiary,
    onTertiary = Color(0xFF00364D),
    background = KaraBackgroundDark,
    onBackground = KaraTextPrimary,
    surface = KaraSurfaceDark,
    onSurface = KaraTextPrimary,
    surfaceVariant = KaraSurfaceVariantDark,
    onSurfaceVariant = KaraTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = KaraPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = KaraSurfaceVariantLight,
    onPrimaryContainer = Color(0xFF21005D),
    secondary = KaraSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD8E4),
    onSecondaryContainer = Color(0xFF3E001D),
    tertiary = KaraTertiaryLight,
    onTertiary = Color.White,
    background = KaraBackgroundLight,
    onBackground = Color(0xFF1C1B1F),
    surface = KaraSurfaceLight,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = KaraSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF49454F)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek Kara AI dark aesthetic
    dynamicColor: Boolean = false, // Keep distinctive Kara branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
