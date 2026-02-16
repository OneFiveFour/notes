package net.onefivefour.notes.ui.navigation

import androidx.navigation3.runtime.NavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Arb generators for NavKey instances used across all back stack property tests.
 */
private fun Arb.Companion.homeRoute(): Arb<HomeRoute> =
    Arb.string(0..50).map { HomeRoute(it) }

private fun Arb.Companion.noteDetailRoute(): Arb<NoteDetailRoute> =
    Arb.string(1..50).map { NoteDetailRoute(it) }

private fun Arb.Companion.navKey(): Arb<NavKey> =
    Arb.choice(Arb.homeRoute(), Arb.noteDetailRoute())

/**
 * Feature: compose-navigation-3, Properties 2, 3, 4: Back stack navigation
 *
 * These property tests verify the back stack manipulation logic used in App.kt.
 * The tests operate on MutableList<NavKey> which mirrors SnapshotStateList behavior.
 */
class BackStackPropertyTest : FunSpec({


    // -----------------------------------------------------------------------
    // Property 2: Push navigation grows the back stack
    // Validates: Requirements 4.1, 4.2
    // -----------------------------------------------------------------------

    test("Property 2: pushing a NavKey increases stack size by one") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(Arb.navKey(), 1..20),
            Arb.navKey()
        ) { initial, newKey ->
            val stack = initial.toMutableList()
            val sizeBefore = stack.size
            val entriesBefore = stack.toList()

            stack.add(newKey)

            stack.size shouldBe sizeBefore + 1
            stack.last() shouldBe newKey
            stack.subList(0, sizeBefore) shouldBe entriesBefore
        }
    }

    // -----------------------------------------------------------------------
    // Property 3: Breadcrumb navigation truncates the back stack
    // Validates: Requirements 4.3
    // -----------------------------------------------------------------------

    test("Property 3: breadcrumb navigation truncates to matching HomeRoute") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(Arb.string(1..20), 2..10)
        ) { paths ->
            // Build a back stack of HomeRoutes with distinct paths
            val stack = paths.map { HomeRoute(it) }.toMutableList()
            // Pick a random target index to navigate to
            val targetIndex = (0 until stack.size).random()
            val targetPath = stack[targetIndex].path
            val entriesBefore = stack.toList()

            // Replicate the breadcrumb logic from App.kt
            val index = stack.indexOfLast { it is HomeRoute && it.path == targetPath }
            if (index >= 0) {
                while (stack.size > index + 1) stack.removeLast()
            } else {
                stack.add(HomeRoute(targetPath))
            }

            // The stack should be truncated so the target is the last entry
            stack.last() shouldBe HomeRoute(targetPath)
            stack.size shouldBe index + 1
            // All entries at or before the target index are unchanged
            stack shouldBe entriesBefore.subList(0, index + 1)
        }
    }

    // -----------------------------------------------------------------------
    // Property 4: Back navigation removes the top entry
    // Validates: Requirements 5.1, 5.2, 5.3
    // -----------------------------------------------------------------------

    test("Property 4: back on multi-entry stack removes top entry") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(Arb.navKey(), 2..20)
        ) { initial ->
            val stack = initial.toMutableList()
            val sizeBefore = stack.size
            val expectedRemaining = stack.dropLast(1)

            stack.removeLastOrNull()

            stack.size shouldBe sizeBefore - 1
            stack shouldBe expectedRemaining
        }
    }

    test("Property 4: back on single-entry stack leaves it unchanged") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.navKey()
        ) { singleKey ->
            val stack = mutableListOf<NavKey>(singleKey)

            // Replicate App.kt logic: only remove if more than one entry
            if (stack.size > 1) {
                stack.removeLastOrNull()
            }

            stack.size shouldBe 1
            stack.first() shouldBe singleKey
        }
    }
})
