package net.onefivefour.echolist.ui.navigation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import androidx.navigation3.runtime.NavKey

/**
 * Feature: compose-navigation-3, Property 1: Route serialization round-trip
 *
 * For any valid NavKey instance (HomeRoute or NoteDetailRoute), serializing to JSON
 * using the polymorphic SerializersModule and deserializing back produces an equal object.
 *
 * **Validates: Requirements 2.3, 2.4**
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

    test("Property 1: NoteDetailRoute serialization round-trip") {
        checkAll(PropTestConfig(iterations = 25), Arb.string(0..200)) { noteId ->
            val route = NoteDetailRoute(noteId)
            val encoded = json.encodeToString(kotlinx.serialization.serializer<NoteDetailRoute>(), route)
            val decoded = json.decodeFromString(kotlinx.serialization.serializer<NoteDetailRoute>(), encoded)
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

    test("Property 1: Polymorphic NavKey serialization round-trip for NoteDetailRoute") {
        checkAll(PropTestConfig(iterations = 25), Arb.string(0..200)) { noteId ->
            val route: NavKey = NoteDetailRoute(noteId)
            val encoded = json.encodeToString(kotlinx.serialization.serializer<NavKey>(), route)
            val decoded = json.decodeFromString(kotlinx.serialization.serializer<NavKey>(), encoded)
            decoded shouldBe route
        }
    }

    // Feature: file-add-button, Property 5: Route serialization round-trip
    // **Validates: Requirements 3.1, 4.1**

    test("Property 5: NoteCreateRoute serialization round-trip") {
        val route = NoteCreateRoute
        val encoded = json.encodeToString(kotlinx.serialization.serializer<NoteCreateRoute>(), route)
        val decoded = json.decodeFromString(kotlinx.serialization.serializer<NoteCreateRoute>(), encoded)
        decoded shouldBe route
    }

    test("Property 5: TasklistDetailRoute serialization round-trip") {
        val route = TasklistDetailRoute
        val encoded = json.encodeToString(kotlinx.serialization.serializer<TasklistDetailRoute>(), route)
        val decoded = json.decodeFromString(kotlinx.serialization.serializer<TasklistDetailRoute>(), encoded)
        decoded shouldBe route
    }

    test("Property 5: Polymorphic NavKey serialization round-trip for NoteCreateRoute") {
        val route: NavKey = NoteCreateRoute
        val encoded = json.encodeToString(kotlinx.serialization.serializer<NavKey>(), route)
        val decoded = json.decodeFromString(kotlinx.serialization.serializer<NavKey>(), encoded)
        decoded shouldBe route
    }

    test("Property 5: Polymorphic NavKey serialization round-trip for TasklistDetailRoute") {
        val route: NavKey = TasklistDetailRoute
        val encoded = json.encodeToString(kotlinx.serialization.serializer<NavKey>(), route)
        val decoded = json.decodeFromString(kotlinx.serialization.serializer<NavKey>(), encoded)
        decoded shouldBe route
    }
})
