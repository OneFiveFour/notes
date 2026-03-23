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
        checkAll(PropTestConfig(iterations = 25), Arb.string(0..200)) { path ->
            val route = HomeRoute(path)
            val encoded = json.encodeToString(kotlinx.serialization.serializer<HomeRoute>(), route)
            val decoded = json.decodeFromString(kotlinx.serialization.serializer<HomeRoute>(), encoded)
            decoded shouldBe route
        }
    }

    test("Property 1: Polymorphic NavKey serialization round-trip for HomeRoute") {
        checkAll(PropTestConfig(iterations = 25), Arb.string(0..200)) { path ->
            val route: NavKey = HomeRoute(path)
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
        ) { index, withFilePath ->
            val parentPath = if (index == 0) "" else "folder-$index"
            val route = EditNoteRoute(
                parentPath = parentPath,
                filePath = if (withFilePath) {
                    if (parentPath.isEmpty()) "note-$index.md" else "$parentPath/note-$index.md"
                } else null
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
        ) { index, withFilePath ->
            val parentPath = if (index == 0) "" else "folder-$index"
            val route: NavKey = EditNoteRoute(
                parentPath = parentPath,
                filePath = if (withFilePath) {
                    if (parentPath.isEmpty()) "note-$index.md" else "$parentPath/note-$index.md"
                } else null
            )
            val encoded = json.encodeToString(kotlinx.serialization.serializer<NavKey>(), route)
            val decoded = json.decodeFromString(kotlinx.serialization.serializer<NavKey>(), encoded)
            decoded shouldBe route
        }
    }

    test("Property 1: EditTaskListRoute serialization round-trip") {
        val encoded = json.encodeToString(kotlinx.serialization.serializer<EditTaskListRoute>(), EditTaskListRoute)
        val decoded = json.decodeFromString(kotlinx.serialization.serializer<EditTaskListRoute>(), encoded)
        decoded shouldBe EditTaskListRoute
    }

    test("Property 1: Polymorphic NavKey serialization round-trip for EditTaskListRoute") {
        val route: NavKey = EditTaskListRoute
        val encoded = json.encodeToString(kotlinx.serialization.serializer<NavKey>(), route)
        val decoded = json.decodeFromString(kotlinx.serialization.serializer<NavKey>(), encoded)
        decoded shouldBe route
    }
})
