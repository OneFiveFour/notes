package net.onefivefour.echolist.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject
import org.koin.mp.KoinPlatform

object EchoListTheme {
    val materialColors: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalMaterialColors.current
    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current
    val dimensions: Dimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalDimensions.current
    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = LocalShapes.current
}

@Composable
fun EchoListTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeManager: ThemeManager? = null,
    content: @Composable () -> Unit
) {
    val isKoinStarted = runCatching { KoinPlatform.getKoin() }.isSuccess
    val resolvedThemeManager = themeManager ?: if (isKoinStarted) {
        koinInject<ThemeManager>()
    } else {
        ThemeManager(
            availableThemes = listOf(EchoListClassicTheme),
            initialTheme = EchoListClassicTheme
        )
    }
    val colorTheme by resolvedThemeManager.selectedTheme.collectAsState()
    val colorScheme = if (darkTheme) colorTheme.darkColorScheme else colorTheme.lightColorScheme

    val typography = material3Typography()

    CompositionLocalProvider(
        LocalTypography provides typography,
        LocalMaterialColors provides colorScheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = LocalShapes.current,
            content = content
        )
    }
}
