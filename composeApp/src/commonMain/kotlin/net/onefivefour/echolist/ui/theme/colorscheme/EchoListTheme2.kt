package net.onefivefour.echolist.ui.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import net.onefivefour.echolist.ui.theme.ColorTheme
import net.onefivefour.echolist.ui.theme.EchoListColorScheme

val EchoListTheme2 = ColorTheme(
    name = "EchoList 2",
    materialColorSchemeLight = lightColorScheme(
        background = Color(0xFFB3B3B3),
        surface = Color.White,
        primary = Color(0xFF023047),
        onPrimary = Color.White,
        secondary = Color(0xFF1F721D),
        onSecondary = Color.White,
        onBackground = Color(0xFF023047),
        onSurface = Color(0xFF023047)
    ),
    materialColorSchemeDark = darkColorScheme(
        background = Color(0xFF1A1A1A),
        surface = Color(0xFF2C2C2C),
        primary = Color(0xFF8ECAE6),
        onPrimary = Color(0xFF023047),
        secondary = Color(0xFFFFB3B3),
        onSecondary = Color(0xFF780000),
        onBackground = Color(0xFFFFFAF0),
        onSurface = Color(0xFFE0E0E0)
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
