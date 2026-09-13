package com.ankitt.themovieshow.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    secondary = AccentBlue,
    tertiary = AccentBlue,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    outline = DarkOutline,
    onBackground = DarkOnBackground,
    onSurface = DarkOnBackground,
    onSurfaceVariant = DarkOnSurfaceVariant,
    onPrimary = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    secondary = AccentBlue,
    tertiary = AccentBlue,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    outline = LightOutline,
    onBackground = LightOnBackground,
    onSurface = LightOnBackground,
    onSurfaceVariant = LightOnSurfaceVariant,
    onPrimary = Color.White,
)

/**
 * App-wide Material 3 theme. Every screen, in every feature module, wraps its content in this —
 * it is the one thing every feature module is allowed to depend on for look-and-feel, so a design
 * change happens in exactly one place.
 *
 * A neutral white/grey brand palette (not a Material You dynamic scheme, hence [dynamicColor]
 * defaults to `false`) with a real light/dark pair, following [darkTheme]'s default of
 * [isSystemInDarkTheme] so the app switches with the system setting.
 */
@Composable
fun TheMovieShowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
        content = content,
    )
}
