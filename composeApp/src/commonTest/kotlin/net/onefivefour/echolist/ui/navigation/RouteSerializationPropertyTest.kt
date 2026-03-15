package net.onefivefour.echolist.ui.navigation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.serialization.json.Json
import androidx.navigation3.runtime.NavKey

/**
 * Feature: unified-edit-screens, Property 1: Route serialization round-trip
 *
 * Validates serialization round-trip for all route types.
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

    // EditNoteRoute and EditTaskListRoute are now data objects (singletons).
    // Round-trip tests for these are in jvmTest/RouteSerializationPropertyTest.kt (Property 5).

    test("Property 1: EditNoteRoute serialization round-trip") {
        val encoded = json.encodeToString(kotlinx.serialization.serializer<EditNoteRoute>(), EditNoteRoute)
        val decoded = json.decodeFromString(kotlinx.serialization.serializer<EditNoteRoute>(), encoded)
        decoded shouldBe EditNoteRoute
    }

    test("Property 1: Polymorphic NavKey serialization round-trip for EditNoteRoute") {
        val route: NavKey = EditNoteRoute
        val encoded = json.encodeToString(kotlinx.serialization.serializer<NavKey>(), route)
        val decoded = json.decodeFromString(kotlinx.serialization.serializer<NavKey>(), encoded)
        decoded shouldBe route
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
