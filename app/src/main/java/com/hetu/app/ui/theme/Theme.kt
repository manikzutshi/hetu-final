package com.hetu.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Hetu color palette - Dark earthy tones
 */
data class HetuColors(
    val background: Color = Color(0xFF1A1512),
    val surface: Color = Color(0xFF2D2420),
    val surfaceVariant: Color = Color(0xFF3D332C),
    val primary: Color = Color(0xFFD4785A),
    val primaryVariant: Color = Color(0xFFB85A3C),
    val onBackground: Color = Color(0xFFE8DDD4),
    val onSurface: Color = Color(0xFFC4B8AD),
    val accent: Color = Color(0xFF4CAF50),
    val error: Color = Color(0xFFCF6679)
)

val LocalHetuColors = staticCompositionLocalOf { HetuColors() }

object HetuTheme {
    val colors: HetuColors
        @Composable
        get() = LocalHetuColors.current
}

@Composable
fun HetuTheme(
    content: @Composable () -> Unit
) {
    val colors = HetuColors()
    
    CompositionLocalProvider(
        LocalHetuColors provides colors,
        content = content
    )
}
