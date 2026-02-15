package net.onefivefour.notes.ui.theme

import androidx.compose.ui.graphics.Color
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Unit tests for EchoList Classic theme color values.
 *
 * **Validates: Requirements 1.2, 1.3**
 */
class EchoListClassicThemeTest : FunSpec({

    val light = EchoListClassicTheme.lightColorScheme
    val dark = EchoListClassicTheme.darkColorScheme

    // -- Light variant exact hex values (Requirement 1.2) --

    test("light background is #FFFAF0") {
        light.background shouldBe Color(0xFFFFFAF0)
    }

    test("light surface is White") {
        light.surface shouldBe Color.White
    }

    test("light primary is #023047") {
        light.primary shouldBe Color(0xFF023047)
    }

    test("light secondary is #780000") {
        light.secondary shouldBe Color(0xFF780000)
    }

    // -- Dark variant adjusted colors (Requirement 1.3) --

    test("dark background is #1A1A1A") {
        dark.background shouldBe Color(0xFF1A1A1A)
    }

    test("dark surface is #2C2C2C") {
        dark.surface shouldBe Color(0xFF2C2C2C)
    }

    test("dark primary is #8ECAE6") {
        dark.primary shouldBe Color(0xFF8ECAE6)
    }

    test("dark secondary is #FFB3B3") {
        dark.secondary shouldBe Color(0xFFFFB3B3)
    }
})
