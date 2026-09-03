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
    primary = DinaSiriDarkPrimary,
    onPrimary = Color.Black,
    primaryContainer = DinaSiriDeepAmber,
    onPrimaryContainer = Color.White,
    secondary = DinaSiriDarkSecondary,
    onSecondary = Color.Black,
    tertiary = DinaSiriDarkTertiary,
    background = DinaSiriDarkBg,
    surface = DinaSiriDarkSurface,
    surfaceVariant = Color(0xFF38332E),
    onBackground = Color(0xFFF5F5F4),
    onSurface = Color(0xFFF5F5F4),
    outline = Color(0xFF57534E)
)

private val LightColorScheme = lightColorScheme(
    primary = DinaSiriTerracotta,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = DinaSiriDeepAmber,
    secondary = DinaSiriFieldGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8F3DC),
    onSecondaryContainer = Color(0xFF081C15),
    tertiary = DinaSiriGold,
    onTertiary = Color.White,
    background = DinaSiriCream,
    surface = DinaSiriParchment,
    surfaceVariant = Color(0xFFF3ECE0),
    onBackground = DinaSiriTextPrimary,
    onSurface = DinaSiriTextPrimary,
    onSurfaceVariant = DinaSiriTextSecondary,
    outline = DinaSiriCardBorder,
    error = DinaSiriError
)

@Composable
fun DinaSiriTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep DinaSiri signature warm Indian cultural palette
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
