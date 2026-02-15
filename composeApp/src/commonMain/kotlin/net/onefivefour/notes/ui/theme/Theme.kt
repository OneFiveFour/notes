package net.onefivefour.notes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

val themeManager = ThemeManager(
    availableThemes = listOf(BeepMeClassicTheme),
    initialTheme = BeepMeClassicTheme
)

@Composable
fun BeepMeTheme(
    dimensions: BeepMeDimensions = BeepMeDimensions(),
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorTheme by themeManager.selectedTheme.collectAsState()
    val colorScheme = if (darkTheme) colorTheme.darkColorScheme else colorTheme.lightColorScheme

    CompositionLocalProvider(LocalBeepMeDimensions provides dimensions) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BeepMeTypography,
            shapes = BeepMeShapes,
            content = content
        )
    }
}
