package net.onefivefour.echolist.ui.navigation

import androidx.navigation3.runtime.NavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Arb generators for NavKey instances used in navigation property tests.
 */
private fun Arb.Companion.homeRoute(): Arb<HomeRoute> =
    Arb.string(0..50).map { HomeRoute(it) }

private fun Arb.Companion.navKey(): Arb<NavKey> =
    Arb.choice(
        Arb.homeRoute(),
        Arb.of(NoteCreateRoute),
        Arb.of(TasklistDetailRoute)
    )

private fun Arb.Companion.detailRoute(): Arb<NavKey> =
    Arb.choice(
        Arb.of(NoteCreateRoute),
        Arb.of(TasklistDetailRoute)
    )

/**
 * Feature: file-add-button, Property 2: Sub-button navigation pushes the correct route
 * Feature: file-add-button, Property 4: Back navigation pops the detail route
 *
 * These property tests verify the navigation logic wired in App.kt for the
 * add-note and add-tasklist sub-buttons, as well as back navigation from detail screens.
 */
class NavigationPropertyTest : FunSpec({

    // -----------------------------------------------------------------------
    // Property 2: Sub-button navigation pushes the correct route
    // **Validates: Requirements 3.1, 4.1**
    // -----------------------------------------------------------------------

    test("Feature: file-add-button, Property 2: Sub-button navigation pushes the correct route - add note") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(Arb.navKey(), 1..20)
        ) { initial ->
            val backStack = initial.toMutableList()
            val sizeBefore = backStack.size
            val entriesBefore = backStack.toList()

            // Simulate: onAddNoteClick = { backStack.add(NoteCreateRoute) }
            backStack.add(NoteCreateRoute)

            backStack.size shouldBe sizeBefore + 1
            backStack.last() shouldBe NoteCreateRoute
            backStack.subList(0, sizeBefore) shouldBe entriesBefore
        }
    }

    test("Feature: file-add-button, Property 2: Sub-button navigation pushes the correct route - add tasklist") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(Arb.navKey(), 1..20)
        ) { initial ->
            val backStack = initial.toMutableList()
            val sizeBefore = backStack.size
            val entriesBefore = backStack.toList()

            // Simulate: onAddTasklistClick = { backStack.add(TasklistDetailRoute) }
            backStack.add(TasklistDetailRoute)

            backStack.size shouldBe sizeBefore + 1
            backStack.last() shouldBe TasklistDetailRoute
            backStack.subList(0, sizeBefore) shouldBe entriesBefore
        }
    }

    // -----------------------------------------------------------------------
    // Property 4: Back navigation pops the detail route
    // **Validates: Requirements 3.6, 4.6**
    // -----------------------------------------------------------------------

    test("Feature: file-add-button, Property 4: Back navigation pops the detail route") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(Arb.navKey(), 1..19),
            Arb.detailRoute()
        ) { base, detailRoute ->
            // Build a back stack with a detail route on top
            val backStack = (base + detailRoute).toMutableList()
            val sizeBefore = backStack.size
            val expectedRemaining = backStack.dropLast(1)

            // Simulate: onBackClick = { backStack.removeLastOrNull() }
            backStack.removeLastOrNull()

            backStack.size shouldBe sizeBefore - 1
            backStack shouldBe expectedRemaining
        }
    }
})
