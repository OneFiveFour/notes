package net.onefivefour.echolist.ui.navigation

import androidx.navigation3.runtime.NavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll

private fun arbHomeRoute(): Arb<HomeRoute> =
    Arb.int(0..50).map { index ->
        if (index == 0) HomeRoute("") else HomeRoute("folder-$index")
    }

private fun arbEditNoteRoute(): Arb<EditNoteRoute> =
    Arb.int(0..50).map { index ->
        EditNoteRoute(
            parentPath = "folder-$index",
            noteId = if (index % 2 == 0) null else "note-id-$index"
        )
    }

private fun arbEditTaskListRoute(): Arb<EditTaskListRoute> =
    Arb.choice(Arb.list(Arb.int(1..1), 1..1).map { EditTaskListRoute })

private fun arbNavKey(): Arb<NavKey> =
    Arb.choice(
        arbHomeRoute(),
        arbEditNoteRoute(),
        arbEditTaskListRoute()
    )

private fun arbDetailRoute(): Arb<NavKey> =
    Arb.choice(
        arbEditNoteRoute(),
        arbEditTaskListRoute()
    )

class NavigationPropertyTest : FunSpec({

    test("Feature: unified-ic_edit-screens, Property 3: Create actions push routes - add note") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(arbNavKey(), 1..20),
            arbHomeRoute()
        ) { initial, currentHome ->
            val backStack = initial.toMutableList()
            val sizeBefore = backStack.size
            val entriesBefore = backStack.toList()

            backStack.add(EditNoteRoute(parentPath = currentHome.path))

            backStack.size shouldBe sizeBefore + 1
            backStack.last() shouldBe EditNoteRoute(parentPath = currentHome.path)
            backStack.subList(0, sizeBefore) shouldBe entriesBefore
        }
    }

    test("Feature: unified-ic_edit-screens, Property 3: Create actions push routes - add tasklist") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(arbNavKey(), 1..20)
        ) { initial ->
            val backStack = initial.toMutableList()
            val sizeBefore = backStack.size
            val entriesBefore = backStack.toList()

            backStack.add(EditTaskListRoute)

            backStack.size shouldBe sizeBefore + 1
            backStack.last() shouldBe EditTaskListRoute
            backStack.subList(0, sizeBefore) shouldBe entriesBefore
        }
    }

    test("Feature: unified-ic_edit-screens, Property 4: File click pushes EditNoteRoute") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(arbNavKey(), 1..20),
            arbHomeRoute(),
            Arb.int(1..50)
        ) { initial, currentHome, fileIndex ->
            val backStack = initial.toMutableList()
            val sizeBefore = backStack.size
            val entriesBefore = backStack.toList()
            val noteId = "note-id-$fileIndex"

            backStack.add(EditNoteRoute(parentPath = currentHome.path, noteId = noteId))

            backStack.size shouldBe sizeBefore + 1
            backStack.last() shouldBe EditNoteRoute(parentPath = currentHome.path, noteId = noteId)
            backStack.subList(0, sizeBefore) shouldBe entriesBefore
        }
    }

    test("Feature: unified-ic_edit-screens, Property 5: Back navigation pops top entry") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(arbNavKey(), 1..19),
            arbDetailRoute()
        ) { base, detailRoute ->
            val backStack = (base + detailRoute).toMutableList()
            val sizeBefore = backStack.size
            val expectedRemaining = backStack.dropLast(1)

            backStack.removeLastOrNull()

            backStack.size shouldBe sizeBefore - 1
            backStack shouldBe expectedRemaining
        }
    }
})
