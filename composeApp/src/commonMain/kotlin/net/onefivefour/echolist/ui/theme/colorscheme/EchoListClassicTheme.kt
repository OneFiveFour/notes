package net.onefivefour.echolist.ui.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import net.onefivefour.echolist.ui.theme.ColorTheme
import net.onefivefour.echolist.ui.theme.EchoListColorScheme

val EchoListClassicTheme = ColorTheme(
    name = "EchoList Classic",
    materialColorSchemeLight = lightColorScheme(
        background = Color(0xFFFF00FF),
        surface = Color(0xFFFF00FF),
        primary = Color(0xFFFF00FF),
        onPrimary = Color(0xFFFF00FF),
        secondary = Color(0xFFFF00FF),
        onSecondary = Color(0xFFFF00FF),
        onBackground = Color(0xFFFF00FF),
        onSurface = Color(0xFFFF00FF)
    ),
    materialColorSchemeDark = darkColorScheme(
        background = Color(0xFFFF00FF),
        surface = Color(0xFFFF00FF),
        primary = Color(0xFFFF00FF),
        onPrimary = Color(0xFFFF00FF),
        secondary = Color(0xFFFF00FF),
        onSecondary = Color(0xFFFF00FF),
        onBackground = Color(0xFFFF00FF),
        onSurface = Color(0xFFFF00FF)
    ),
    echoListColorSchemeLight = EchoListColorScheme(
        background = Color(0xFF1A0B18),
        backgroundGradient1 = Color(0x3325C0F4),
        backgroundGradient2 = Color(0x33F425C0),
        backgroundGradient3 = Color(0x33F48C25)
    ),
    echoListColorSchemeDark = EchoListColorScheme(
        background = Color(0xFF1A0B18),
        backgroundGradient1 = Color(0x3325C0F4),
        backgroundGradient2 = Color(0x33F425C0),
        backgroundGradient3 = Color(0x33F48C25)
    )
)
