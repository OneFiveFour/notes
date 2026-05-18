package net.onefivefour.echolist.ui.navigation

import androidx.navigation3.runtime.NavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.serialization.json.Json

class RouteSerializationPropertyTest : FunSpec({

    val json = Json {
        serializersModule = navKeySerializersModule
    }

    test("Property 1: HomeRoute serialization round-trip") {
        checkAll(PropTestConfig(iterations = 25), Arb.string(0..200)) { parentDir ->
            val route = HomeRoute(parentDir)
            val encoded = json.encodeToString(kotlinx.serialization.serializer<HomeRoute>(), route)
            val decoded = json.decodeFromString(kotlinx.serialization.serializer<HomeRoute>(), encoded)
            decoded shouldBe route
        }
    }

    test("Property 1: Polymorphic NavKey serialization round-trip for HomeRoute") {
        checkAll(PropTestConfig(iterations = 25), Arb.string(0..200)) { parentDir ->
            val route: NavKey = HomeRoute(parentDir)
            val encoded = json.encodeToString(kotlinx.serialization.serializer<NavKey>(), route)
            val decoded = json.decodeFromString(kotlinx.serialization.serializer<NavKey>(), encoded)
            decoded shouldBe route
        }
    }

    test("Property 1: EditNoteRoute serialization round-trip") {
        checkAll(
            PropTestConfig(iterations = 25),
            Arb.int(0..50),
            Arb.boolean()
        ) { index, withNoteId ->
            val parentDir = if (index == 0) "" else "folder-$index"
            val route = EditNoteRoute(
                parentDir = parentDir,
                noteId = if (withNoteId) "note-id-$index" else null
            )
            val encoded = json.encodeToString(kotlinx.serialization.serializer<EditNoteRoute>(), route)
            val decoded = json.decodeFromString(kotlinx.serialization.serializer<EditNoteRoute>(), encoded)
            decoded shouldBe route
        }
    }

    test("Property 1: Polymorphic NavKey serialization round-trip for EditNoteRoute") {
        checkAll(
            PropTestConfig(iterations = 25),
            Arb.int(0..50),
            Arb.boolean()
        ) { index, withNoteId ->
            val parentDir = if (index == 0) "" else "folder-$index"
            val route: NavKey = EditNoteRoute(
                parentDir = parentDir,
                noteId = if (withNoteId) "note-id-$index" else null
            )
            val encoded = json.encodeToString(kotlinx.serialization.serializer<NavKey>(), route)
            val decoded = json.decodeFromString(kotlinx.serialization.serializer<NavKey>(), encoded)
            decoded shouldBe route
        }
    }

    test("Property 1: EditTaskListRoute serialization round-trip") {
        val route = EditTaskListRoute(
            parentDir = "folder-1",
            taskListId = "task-list-1"
        )
        val encoded = json.encodeToString(kotlinx.serialization.serializer<EditTaskListRoute>(), route)
        val decoded = json.decodeFromString(kotlinx.serialization.serializer<EditTaskListRoute>(), encoded)
        decoded shouldBe route
    }

    test("Property 1: Polymorphic NavKey serialization round-trip for EditTaskListRoute") {
        val route: NavKey = EditTaskListRoute(
            parentDir = "folder-1",
            taskListId = "task-list-1"
        )
        val encoded = json.encodeToString(kotlinx.serialization.serializer<NavKey>(), route)
        val decoded = json.decodeFromString(kotlinx.serialization.serializer<NavKey>(), encoded)
        decoded shouldBe route
    }
})
