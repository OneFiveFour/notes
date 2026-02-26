package net.onefivefour.echolist.ui.navigation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.serialization.json.Json
import androidx.navigation3.runtime.NavKey

/**
 * Feature: unified-edit-screens, Property 1: Route serialization round-trip
 *
 * For any EditNoteRoute with an arbitrary noteId (including null) and for any
 * EditTaskListRoute with an arbitrary taskListId (including null), serializing
 * to JSON and deserializing back produces an equal object.
 *
 * **Validates: Requirements 1.4, 2.4, 7.1, 7.2, 7.3**
 */
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

    // Feature: unified-edit-screens, Property 1: Route serialization round-trip (EditNoteRoute)
    // **Validates: Requirements 1.4, 7.1**

    test("Property 1: EditNoteRoute serialization round-trip") {
        checkAll(PropTestConfig(iterations = 100), Arb.string(0..200).orNull()) { noteId ->
            val route = EditNoteRoute(noteId)
            val encoded = json.encodeToString(kotlinx.serialization.serializer<EditNoteRoute>(), route)
            val decoded = json.decodeFromString(kotlinx.serialization.serializer<EditNoteRoute>(), encoded)
            decoded shouldBe route
        }
    }

    test("Property 1: Polymorphic NavKey serialization round-trip for EditNoteRoute") {
        checkAll(PropTestConfig(iterations = 100), Arb.string(0..200).orNull()) { noteId ->
            val route: NavKey = EditNoteRoute(noteId)
            val encoded = json.encodeToString(kotlinx.serialization.serializer<NavKey>(), route)
            val decoded = json.decodeFromString(kotlinx.serialization.serializer<NavKey>(), encoded)
            decoded shouldBe route
        }
    }

    // Feature: unified-edit-screens, Property 1: Route serialization round-trip (EditTaskListRoute)
    // **Validates: Requirements 2.4, 7.2, 7.3**

    test("Property 1: EditTaskListRoute serialization round-trip") {
        checkAll(PropTestConfig(iterations = 100), Arb.string(0..200).orNull()) { taskListId ->
            val route = EditTaskListRoute(taskListId)
            val encoded = json.encodeToString(kotlinx.serialization.serializer<EditTaskListRoute>(), route)
            val decoded = json.decodeFromString(kotlinx.serialization.serializer<EditTaskListRoute>(), encoded)
            decoded shouldBe route
        }
    }

    test("Property 1: Polymorphic NavKey serialization round-trip for EditTaskListRoute") {
        checkAll(PropTestConfig(iterations = 100), Arb.string(0..200).orNull()) { taskListId ->
            val route: NavKey = EditTaskListRoute(taskListId)
            val encoded = json.encodeToString(kotlinx.serialization.serializer<NavKey>(), route)
            val decoded = json.decodeFromString(kotlinx.serialization.serializer<NavKey>(), encoded)
            decoded shouldBe route
        }
    }
})
