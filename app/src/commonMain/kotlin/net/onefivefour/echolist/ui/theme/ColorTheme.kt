package net.onefivefour.echolist.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.staticCompositionLocalOf

data class ColorTheme(
    val name: String,
    val materialColorSchemeLight: ColorScheme,
    val materialColorSchemeDark: ColorScheme,
    val echoListColorSchemeLight: EchoListColorScheme,
    val echoListColorSchemeDark: EchoListColorScheme
)

internal val LocalMaterialColors = staticCompositionLocalOf<ColorScheme> {
    error("ColorScheme not provided")
}