package net.onefivefour.echolist.ui.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import net.onefivefour.echolist.ui.theme.ColorTheme
import net.onefivefour.echolist.ui.theme.EchoListColorScheme

val EchoListClassicTheme = ColorTheme(
    name = "EchoList Classic",
    materialColorSchemeLight = lightColorScheme(
        background = Color(0xFFFF8C00),
        surface = Color(0x1A000000),
        surfaceVariant = Color(0x1A000000),
        primary = Color(0xFFC24142),
        onPrimary = Color(0xFFE1E0D5),
        secondary = Color(0xFFFF8C00),
        onSecondary = Color(0xFF333333),
        onBackground = Color(0xFFFF8C00),
        onSurface = Color(0xFF383838),
        onSurfaceVariant = Color(0xFF383838),
        error = Color(0xFFB81A1C)
    ),
    materialColorSchemeDark = darkColorScheme(
        background = Color(0xFFFF8C00),
        surface = Color(0x1AFFFFFF),
        primary = Color(0xFFFF8789),
        onPrimary = Color(0xFF373C45),
        secondary = Color(0xFFFF8C00),
        onSecondary = Color(0xFFEFEFEF),
        onBackground = Color(0xFFFF8C00),
        onSurface = Color(0xFFFF8C00),
        error = Color(0xFFFF0000)
    ),
    echoListColorSchemeLight = EchoListColorScheme(
        background = Color(0xFFE1E0D5),
        backgroundGradient1 = Color(0x27F425A8),
        backgroundGradient2 = Color(0x17F48C25),
        backgroundGradient3 = Color(0x2725C0F4),
        taskColor = Color(0xFFDBEAFE),
        noteColor = Color(0xFFFFEDD5),
        folderColor = Color(0xFFD1FAE5)
    ),
    echoListColorSchemeDark = EchoListColorScheme(
        background = Color(0xFF1A0B18),
        backgroundGradient1 = Color(0x27F425A8),
        backgroundGradient2 = Color(0x17F48C25),
        backgroundGradient3 = Color(0x2725C0F4),
        taskColor = Color(0xFFDBEAFE),
        noteColor = Color(0xFFFFEDD5),
        folderColor = Color(0xFFD1FAE5)
    )
//    echoListColorSchemeLight = EchoListColorScheme(
//        background = Color(0xFF1A0B18),
//        backgroundGradient1 = Color(0x3325C0F4),
//        backgroundGradient2 = Color(0x33F425C0),
//        backgroundGradient3 = Color(0x33F48C25)
//    ),
//    echoListColorSchemeDark = EchoListColorScheme(
//        background = Color(0xFF1A0B18),
//        backgroundGradient1 = Color(0x3325C0F4),
//        backgroundGradient2 = Color(0x33F425C0),
//        backgroundGradient3 = Color(0x33F48C25)
//    )
)