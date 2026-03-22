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

private fun Arb.Companion.homeRoute(): Arb<HomeRoute> =
    Arb.int(0..50).map { index ->
        if (index == 0) HomeRoute("/") else HomeRoute("/folder-$index")
    }

private fun Arb.Companion.editNoteRoute(): Arb<EditNoteRoute> =
    Arb.int(0..50).map { index ->
        if (index % 2 == 0) {
            EditNoteRoute(parentPath = "/folder-$index")
        } else {
            EditNoteRoute(
                parentPath = "/folder-$index",
                filePath = "/folder-$index/note-$index.md"
            )
        }
    }

private fun Arb.Companion.navKey(): Arb<NavKey> =
    Arb.choice(Arb.homeRoute(), Arb.editNoteRoute())

class BackStackPropertyTest : FunSpec({

    test("Property 2: pushing a NavKey increases stack size by one") {
        checkAll(
            PropTestConfig(iterations = 20),
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

    test("Property 3: breadcrumb navigation truncates to matching HomeRoute") {
        checkAll(
            PropTestConfig(iterations = 20),
            Arb.list(Arb.int(1..10).map { "/folder-$it" }, 2..10)
        ) { paths ->
            val stack = paths.map { HomeRoute(it) }.toMutableList()
            val targetIndex = (0 until stack.size).random()
            val targetPath = stack[targetIndex].path
            val entriesBefore = stack.toList()

            val index = stack.indexOfLast { it is HomeRoute && it.path == targetPath }
            if (index >= 0) {
                while (stack.size > index + 1) stack.removeLast()
            } else {
                stack.add(HomeRoute(targetPath))
            }

            stack.last() shouldBe HomeRoute(targetPath)
            stack.size shouldBe index + 1
            stack shouldBe entriesBefore.subList(0, index + 1)
        }
    }

    test("Property 4: back on multi-entry stack removes top entry") {
        checkAll(
            PropTestConfig(iterations = 20),
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
            PropTestConfig(iterations = 20),
            Arb.navKey()
        ) { singleKey ->
            val stack = mutableListOf<NavKey>(singleKey)

            if (stack.size > 1) {
                stack.removeLastOrNull()
            }

            stack.size shouldBe 1
            stack.first() shouldBe singleKey
        }
    }
})
