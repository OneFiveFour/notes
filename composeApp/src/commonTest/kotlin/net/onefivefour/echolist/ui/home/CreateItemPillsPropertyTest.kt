package net.onefivefour.echolist.ui.home

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import net.onefivefour.echolist.data.models.ItemType

class CreateItemPillsPropertyTest : FunSpec({

    // Arbitrary for valid item types (excluding UNSPECIFIED)
    val validItemTypes = Arb.enum<ItemType>().filter { it != ItemType.UNSPECIFIED }

    // Arbitrary for non-empty strings
    val nonEmptyStrings = Arb.string(minSize = 1).filter { it.isNotBlank() }

    test("Feature: inline-item-creation, Property 1: Pill selection transitions to correct Input state") {
        checkAll(validItemTypes) { itemType ->
            val result = nextState(PillsUiState.Idle, PillsAction.PillClicked(itemType))
            result shouldBe PillsUiState.Input(itemType)
        }
    }

    test("Feature: inline-item-creation, Property 3: IME confirm with non-empty title invokes correct callback and resets") {
        checkAll(validItemTypes, nonEmptyStrings) { itemType, title ->
            // resolveImeAction should return the item type for non-empty titles
            val resolvedType = resolveImeAction(itemType, title)
            resolvedType shouldBe itemType

            // nextState should transition back to Idle
            val result = nextState(PillsUiState.Input(itemType), PillsAction.ImeConfirm(title))
            result shouldBe PillsUiState.Idle
        }
    }

    test("Feature: inline-item-creation, Property 4: IME confirm with empty title resets without callback") {
        checkAll(validItemTypes) { itemType ->
            // Test with empty string
            val resolvedEmpty = resolveImeAction(itemType, "")
            resolvedEmpty shouldBe null

            // Test with whitespace-only string
            val resolvedWhitespace = resolveImeAction(itemType, "   ")
            resolvedWhitespace shouldBe null

            // nextState should still transition back to Idle
            val resultEmpty = nextState(PillsUiState.Input(itemType), PillsAction.ImeConfirm(""))
            resultEmpty shouldBe PillsUiState.Idle

            val resultWhitespace = nextState(PillsUiState.Input(itemType), PillsAction.ImeConfirm("   "))
            resultWhitespace shouldBe PillsUiState.Idle
        }
    }

    test("Feature: inline-item-creation, Property 5: Close button in Input state resets and invokes onClosePills") {
        checkAll(validItemTypes) { itemType ->
            val result = nextState(PillsUiState.Input(itemType), PillsAction.CloseClicked)
            result shouldBe PillsUiState.Idle
        }
    }
})
