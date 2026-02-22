package net.onefivefour.echolist.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf

data class ColorTheme(
    val name: String,
    val lightColorScheme: ColorScheme,
    val darkColorScheme: ColorScheme
)

internal val LocalMaterialColors = staticCompositionLocalOf<ColorScheme> {
    error("ColorScheme not provided")
}
