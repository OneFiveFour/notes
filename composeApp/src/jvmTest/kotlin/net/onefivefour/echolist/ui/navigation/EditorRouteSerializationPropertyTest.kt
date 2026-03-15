package net.onefivefour.echolist.ui.navigation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * Feature: note-tasklist-editors, Property 5: Route serialization round-trip
 *
 * Validates: Requirements 9.3, 9.4
 */
class EditorRouteSerializationPropertyTest : FunSpec({

    test("Feature: note-tasklist-editors, Property 5: EditNoteRoute serialization round-trip produces equal object") {
        // Feature: note-tasklist-editors, Property 5: Route serialization round-trip
        val json = Json.encodeToString(EditNoteRoute)
        val decoded = Json.decodeFromString<EditNoteRoute>(json)
        decoded shouldBe EditNoteRoute
    }

    test("Feature: note-tasklist-editors, Property 5: EditTaskListRoute serialization round-trip produces equal object") {
        // Feature: note-tasklist-editors, Property 5: Route serialization round-trip
        val json = Json.encodeToString(EditTaskListRoute)
        val decoded = Json.decodeFromString<EditTaskListRoute>(json)
        decoded shouldBe EditTaskListRoute
    }
})
