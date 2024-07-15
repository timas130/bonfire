package com.sayzen.campfiresdk.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.sayzen.campfiresdk.controllers.notifications.ControllerApp

private val LegacyLightColors = lightColorScheme(
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF161616),
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    onSurface = Color(0xFF161616),
    surface = Color(0xFFE0E0E0),
    surfaceContainerLow = Color.White,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainer = surfaceContainerLight,
)

// I have no idea blyat
private val LegacyDarkColors = darkColorScheme(
    background = Color(0xFF252525),
    onBackground = Color(0xFFFFFFFF),
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xAAFFFFFF),
    surface = Color(0xFF363636),
    surfaceContainerLow = Color(0xFF212121),
    outlineVariant = Color(0xFF363636),
    surfaceVariant = surfaceVariantDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
    surfaceContainerHigh = surfaceContainerHighDark,
)

@Composable
fun BonfireTheme(
    useDarkTheme: Boolean = ControllerApp.isDarkThem(),
    content: @Composable () -> Unit
) {
    // I WILL NOT REDESIGN THE WHOLE APP
    // I WILL NOT REDESIGN THE WHOLE APP
    // I WILL NOT REDESIGN THE WHOLE APP
    // I WILL NOT REDESIGN THE WHOLE APP
    // I WILL NOT REDESIGN THE WHOLE APP
    val colors = if (useDarkTheme) {
        LegacyDarkColors
    } else {
        LegacyLightColors
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
