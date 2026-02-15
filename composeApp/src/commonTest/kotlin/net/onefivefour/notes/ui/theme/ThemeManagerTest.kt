package net.onefivefour.notes.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Unit tests for ThemeManager edge cases.
 *
 * **Validates: Requirements 2.1, 2.2**
 */
class ThemeManagerTest : FunSpec({

    val themeA = ColorTheme(
        name = "Theme A",
        lightColorScheme = lightColorScheme(primary = Color.Red),
        darkColorScheme = darkColorScheme(primary = Color.Blue)
    )

    val themeB = ColorTheme(
        name = "Theme B",
        lightColorScheme = lightColorScheme(primary = Color.Green),
        darkColorScheme = darkColorScheme(primary = Color.Yellow)
    )

    val themeNotInList = ColorTheme(
        name = "Unknown",
        lightColorScheme = lightColorScheme(primary = Color.Cyan),
        darkColorScheme = darkColorScheme(primary = Color.Magenta)
    )

    test("initialization sets selectedTheme to initial theme") {
        val manager = ThemeManager(
            availableThemes = listOf(themeA, themeB),
            initialTheme = themeB
        )
        manager.selectedTheme.value shouldBe themeB
    }

    test("selecting a theme not in the available list is ignored") {
        val manager = ThemeManager(availableThemes = listOf(themeA, themeB))
        manager.selectTheme(themeNotInList)
        manager.selectedTheme.value shouldBe themeA
    }
})
