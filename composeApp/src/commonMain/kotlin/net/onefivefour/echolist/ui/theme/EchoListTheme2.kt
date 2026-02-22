package net.onefivefour.echolist.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val EchoListTheme2 = ColorTheme(
    name = "EchoList 2",
    lightColorScheme = lightColorScheme(
        background = Color(0xFFB3B3B3),
        surface = Color.White,
        primary = Color(0xFF023047),
        onPrimary = Color.White,
        secondary = Color(0xFF1F721D),
        onSecondary = Color.White,
        onBackground = Color(0xFF023047),
        onSurface = Color(0xFF023047)
    ),
    darkColorScheme = darkColorScheme(
        background = Color(0xFF1A1A1A),
        surface = Color(0xFF2C2C2C),
        primary = Color(0xFF8ECAE6),
        onPrimary = Color(0xFF023047),
        secondary = Color(0xFFFFB3B3),
        onSecondary = Color(0xFF780000),
        onBackground = Color(0xFFFFFAF0),
        onSurface = Color(0xFFE0E0E0)
    )
)