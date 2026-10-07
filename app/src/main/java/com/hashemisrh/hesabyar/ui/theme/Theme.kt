package com.hashemisrh.hesabyar.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0F5C5A),
    onPrimary = Color(0xFFF5F3EE),
    secondary = Color(0xFFC7A86B),
    background = Color(0xFFF5F3EE),
    surface = Color.White,
    onBackground = Color(0xFF102A2A),
    onSurface = Color(0xFF102A2A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF2E8B75),
    onPrimary = Color(0xFFF5F3EE),
    secondary = Color(0xFFC7A86B),
    background = Color(0xFF102A2A),
    surface = Color(0xFF163636),
    onBackground = Color(0xFFF5F3EE),
    onSurface = Color(0xFFF5F3EE)
)

@Composable
fun HesabYarTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
