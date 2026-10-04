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

private fun arbBrowserRoute(): Arb<BrowserRoute> =
    Arb.int(0..50).map { index ->
        if (index == 0) BrowserRoute("") else BrowserRoute("folder-$index")
    }

private fun arbEditNoteRoute(): Arb<EditNoteRoute> =
    Arb.int(0..50).map { index ->
        EditNoteRoute(
            parentDir = "folder-$index",
            noteId = if (index % 2 == 0) null else "note-id-$index"
        )
    }

private fun arbEditTaskListRoute(): Arb<EditTaskListRoute> =
    Arb.choice(
        Arb.int(0..50).map { index ->
            EditTaskListRoute(
                parentDir = if (index == 0) "" else "folder-$index",
                taskListId = null
            )
        },
        Arb.int(0..50).map { index ->
            EditTaskListRoute(
                parentDir = if (index == 0) "" else "folder-$index",
                taskListId = "task-list-$index"
            )
        }
    )

private fun arbNavKey(): Arb<NavKey> =
    Arb.choice(
        arbBrowserRoute(),
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
            arbBrowserRoute()
        ) { initial, currentBrowser ->
            val backStack = initial.toMutableList()
            val sizeBefore = backStack.size
            val entriesBefore = backStack.toList()

            backStack.add(EditNoteRoute(parentDir = currentBrowser.parentDir))

            backStack.size shouldBe sizeBefore + 1
            backStack.last() shouldBe EditNoteRoute(parentDir = currentBrowser.parentDir)
            backStack.subList(0, sizeBefore) shouldBe entriesBefore
        }
    }

    test("Feature: unified-ic_edit-screens, Property 3: Create actions push routes - add tasklist") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(arbNavKey(), 1..20),
            arbBrowserRoute()
        ) { initial, currentBrowser ->
            val backStack = initial.toMutableList()
            val sizeBefore = backStack.size
            val entriesBefore = backStack.toList()

            backStack.add(EditTaskListRoute(parentDir = currentBrowser.parentDir))

            backStack.size shouldBe sizeBefore + 1
            (backStack.last() as EditTaskListRoute).parentDir shouldBe currentBrowser.parentDir
            (backStack.last() as EditTaskListRoute).taskListId shouldBe null
            backStack.subList(0, sizeBefore) shouldBe entriesBefore
        }
    }

    test("Feature: unified-ic_edit-screens, Property 4: File click pushes EditTaskListRoute") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(arbNavKey(), 1..20),
            arbBrowserRoute(),
            Arb.int(1..50)
        ) { initial, currentBrowser, fileIndex ->
            val backStack = initial.toMutableList()
            val sizeBefore = backStack.size
            val entriesBefore = backStack.toList()
            val taskListId = "task-list-$fileIndex"

            backStack.add(
                EditTaskListRoute(
                    parentDir = currentBrowser.parentDir,
                    taskListId = taskListId
                )
            )

            backStack.size shouldBe sizeBefore + 1
            (backStack.last() as EditTaskListRoute).parentDir shouldBe currentBrowser.parentDir
            (backStack.last() as EditTaskListRoute).taskListId shouldBe taskListId
            backStack.subList(0, sizeBefore) shouldBe entriesBefore
        }
    }

    test("Feature: unified-ic_edit-screens, Property 5: File click pushes EditNoteRoute") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(arbNavKey(), 1..20),
            arbBrowserRoute(),
            Arb.int(1..50)
        ) { initial, currentBrowser, fileIndex ->
            val backStack = initial.toMutableList()
            val sizeBefore = backStack.size
            val entriesBefore = backStack.toList()
            val noteId = "note-id-$fileIndex"

            backStack.add(EditNoteRoute(parentDir = currentBrowser.parentDir, noteId = noteId))

            backStack.size shouldBe sizeBefore + 1
            backStack.last() shouldBe EditNoteRoute(parentDir = currentBrowser.parentDir, noteId = noteId)
            backStack.subList(0, sizeBefore) shouldBe entriesBefore
        }
    }

    test("Feature: unified-ic_edit-screens, Property 6: Back navigation pops top entry") {
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