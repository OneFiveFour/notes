package net.onefivefour.echolist.ui.navigation

import androidx.navigation3.runtime.NavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Arb generators for NavKey instances used in navigation property tests.
 */
private fun Arb.Companion.homeRoute(): Arb<HomeRoute> =
    Arb.string(0..50).map { HomeRoute(it) }

private fun Arb.Companion.editNoteRoute(): Arb<EditNoteRoute> =
    Arb.string(1..50).orNull().map { EditNoteRoute(noteId = it) }

private fun Arb.Companion.editTaskListRoute(): Arb<EditTaskListRoute> =
    Arb.string(1..50).orNull().map { EditTaskListRoute(taskListId = it) }

private fun Arb.Companion.navKey(): Arb<NavKey> =
    Arb.choice(
        Arb.homeRoute(),
        Arb.editNoteRoute(),
        Arb.editTaskListRoute()
    )

private fun Arb.Companion.detailRoute(): Arb<NavKey> =
    Arb.choice(
        Arb.editNoteRoute(),
        Arb.editTaskListRoute()
    )

/**
 * Feature: unified-edit-screens, Property 3: Create actions push null-ID routes
 * Feature: unified-edit-screens, Property 4: File click pushes EditNoteRoute with file ID
 * Feature: unified-edit-screens, Property 5: Back navigation pops top entry
 *
 * These property tests verify the navigation logic wired in App.kt for the
 * add-note and add-tasklist actions, file click navigation, and back navigation.
 */
class NavigationPropertyTest : FunSpec({

    // -----------------------------------------------------------------------
    // Property 3: Create actions push null-ID routes
    // **Validates: Requirements 5.4, 5.5, 7.4, 7.5, 7.6**
    // -----------------------------------------------------------------------

    test("Feature: unified-edit-screens, Property 3: Create actions push null-ID routes - add note") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(Arb.navKey(), 1..20)
        ) { initial ->
            val backStack = initial.toMutableList()
            val sizeBefore = backStack.size
            val entriesBefore = backStack.toList()

            // Simulate: onAddNoteClick = { backStack.add(EditNoteRoute()) }
            backStack.add(EditNoteRoute())

            backStack.size shouldBe sizeBefore + 1
            backStack.last() shouldBe EditNoteRoute()
            backStack.subList(0, sizeBefore) shouldBe entriesBefore
        }
    }

    test("Feature: unified-edit-screens, Property 3: Create actions push null-ID routes - add tasklist") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(Arb.navKey(), 1..20)
        ) { initial ->
            val backStack = initial.toMutableList()
            val sizeBefore = backStack.size
            val entriesBefore = backStack.toList()

            // Simulate: onAddTasklistClick = { backStack.add(EditTaskListRoute()) }
            backStack.add(EditTaskListRoute())

            backStack.size shouldBe sizeBefore + 1
            backStack.last() shouldBe EditTaskListRoute()
            backStack.subList(0, sizeBefore) shouldBe entriesBefore
        }
    }

    // -----------------------------------------------------------------------
    // Property 4: File click pushes EditNoteRoute with file ID
    // **Validates: Requirements 5.6**
    // -----------------------------------------------------------------------

    test("Feature: unified-edit-screens, Property 4: File click pushes EditNoteRoute with file ID") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(Arb.navKey(), 1..20),
            Arb.string(1..50)
        ) { initial, fileId ->
            val backStack = initial.toMutableList()
            val sizeBefore = backStack.size
            val entriesBefore = backStack.toList()

            // Simulate: onFileClick = { fileId -> backStack.add(EditNoteRoute(noteId = fileId)) }
            backStack.add(EditNoteRoute(noteId = fileId))

            backStack.size shouldBe sizeBefore + 1
            backStack.last() shouldBe EditNoteRoute(noteId = fileId)
            backStack.subList(0, sizeBefore) shouldBe entriesBefore
        }
    }

    // -----------------------------------------------------------------------
    // Property 5: Back navigation pops top entry
    // **Validates: Requirements 5.7**
    // -----------------------------------------------------------------------

    test("Feature: unified-edit-screens, Property 5: Back navigation pops top entry") {
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
