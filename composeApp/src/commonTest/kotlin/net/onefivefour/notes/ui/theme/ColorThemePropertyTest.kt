package net.onefivefour.notes.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll

/**
 * Property-based tests for ColorTheme structural invariant.
 *
 * **Validates: Requirements 1.1**
 */
class ColorThemePropertyTest : FunSpec({

    // -- Generators --

    fun arbColor(): Arb<Color> = arbitrary {
        Color(
            red = Arb.float(0f..1f).bind(),
            green = Arb.float(0f..1f).bind(),
            blue = Arb.float(0f..1f).bind(),
            alpha = Arb.float(0f..1f).bind()
        )
    }

    fun arbColorSchemeLight(): Arb<androidx.compose.material3.ColorScheme> = arbitrary {
        val c = arbColor()
        lightColorScheme(
            primary = c.bind(),
            onPrimary = c.bind(),
            secondary = c.bind(),
            onSecondary = c.bind(),
            background = c.bind(),
            onBackground = c.bind(),
            surface = c.bind(),
            onSurface = c.bind()
        )
    }

    fun arbColorSchemeDark(): Arb<androidx.compose.material3.ColorScheme> = arbitrary {
        val c = arbColor()
        darkColorScheme(
            primary = c.bind(),
            onPrimary = c.bind(),
            secondary = c.bind(),
            onSecondary = c.bind(),
            background = c.bind(),
            onBackground = c.bind(),
            surface = c.bind(),
            onSurface = c.bind()
        )
    }

    val arbColorTheme = arbitrary {
        ColorTheme(
            name = Arb.stringPattern("[A-Za-z][A-Za-z0-9 ]{0,49}").bind(),
            lightColorScheme = arbColorSchemeLight().bind(),
            darkColorScheme = arbColorSchemeDark().bind()
        )
    }

    // -- Property 1: ColorTheme structural invariant --

    test("Property 1: ColorTheme name must be non-blank").config(invocations = 100) {
        checkAll(arbColorTheme) { theme ->
            theme.name.shouldNotBeBlank()
        }
    }

    test("Property 1: ColorTheme light and dark schemes must be distinct instances").config(invocations = 100) {
        checkAll(arbColorTheme) { theme ->
            (theme.lightColorScheme !== theme.darkColorScheme) shouldBe true
        }
    }
})
