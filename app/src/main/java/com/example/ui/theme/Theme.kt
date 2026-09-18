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
    primary = WorkPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = WorkPrimaryDark,
    onPrimaryContainer = WorkContainer,
    secondary = BreakSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = OnBreakContainer,
    onSecondaryContainer = BreakContainer,
    tertiary = OvertimeTertiary,
    background = SlateBackgroundDark,
    surface = SlateSurfaceDark,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9)
)

private val LightColorScheme = lightColorScheme(
    primary = WorkPrimary,
    onPrimary = Color.White,
    primaryContainer = WorkContainer,
    onPrimaryContainer = OnWorkContainer,
    secondary = BreakSecondary,
    onSecondary = Color.White,
    secondaryContainer = BreakContainer,
    onSecondaryContainer = OnBreakContainer,
    tertiary = OvertimeTertiary,
    background = SlateBackgroundLight,
    surface = SlateSurfaceLight,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent branding colors
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
