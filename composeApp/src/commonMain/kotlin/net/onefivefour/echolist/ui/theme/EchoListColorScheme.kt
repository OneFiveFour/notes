package net.onefivefour.echolist.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class EchoListColorScheme(
    val background: Color,
    val backgroundGradient1: Color,
    val backgroundGradient2: Color,
    val backgroundGradient3: Color
)

val LocalEchoListColors = staticCompositionLocalOf {
    EchoListColorScheme(
        background = Color.Unspecified,
        backgroundGradient1 = Color.Unspecified,
        backgroundGradient2 = Color.Unspecified,
        backgroundGradient3 = Color.Unspecified
    )
}