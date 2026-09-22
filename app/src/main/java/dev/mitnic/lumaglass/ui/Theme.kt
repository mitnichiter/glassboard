package dev.mitnic.lumaglass.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import dev.mitnic.lumaglass.data.GlassTheme

@Immutable
data class GlassPalette(
    val deep: Color,
    val mid: Color,
    val glowA: Color,
    val glowB: Color,
    val keyTint: Color,
    val keyStrong: Color,
    val text: Color,
)

private val Aurora = GlassPalette(
    deep = Color(0xFF09101E), mid = Color(0xFF19203A),
    glowA = Color(0xFF6C63FF), glowB = Color(0xFF2ED3C6),
    keyTint = Color(0x2EFFFFFF), keyStrong = Color(0x4D8D82FF), text = Color.White,
)
private val Midnight = GlassPalette(
    deep = Color(0xFF05070D), mid = Color(0xFF111827),
    glowA = Color(0xFF2563EB), glowB = Color(0xFF8B5CF6),
    keyTint = Color(0x26FFFFFF), keyStrong = Color(0x4D3B82F6), text = Color.White,
)
private val Pearl = GlassPalette(
    deep = Color(0xFFE8ECF4), mid = Color(0xFFC8D1E0),
    glowA = Color(0xFF8B7CF6), glowB = Color(0xFF42BFC4),
    keyTint = Color(0x66FFFFFF), keyStrong = Color(0x668A7CFF), text = Color(0xFF111526),
)

val LocalGlassPalette = staticCompositionLocalOf { Aurora }

@Composable
fun LumaGlassTheme(theme: GlassTheme, content: @Composable () -> Unit) {
    val palette = when (theme) {
        GlassTheme.AURORA -> Aurora
        GlassTheme.MIDNIGHT -> Midnight
        GlassTheme.PEARL -> Pearl
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalGlassPalette provides palette) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = palette.glowA,
                secondary = palette.glowB,
                surface = palette.deep,
                onSurface = palette.text,
            ),
            content = content,
        )
    }
}
