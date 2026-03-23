package net.onefivefour.echolist.ui.navigation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import net.onefivefour.echolist.data.repository.joinPath

/**
 * Feature: note-tasklist-editors, Property 5: Route serialization round-trip
 *
 * Validates: Requirements 9.3, 9.4
 */
class EditorRouteSerializationPropertyTest : FunSpec({

    test("Feature: note-tasklist-editors, Property 5: EditNoteRoute serialization round-trip produces equal route") {
        checkAll(
            PropTestConfig(iterations = 50),
            Arb.int(0..50),
            Arb.boolean()
        ) { index, withFilePath ->
            val parentPath = if (index == 0) "/" else "/folder-$index"
            val route = EditNoteRoute(
                parentPath = parentPath,
                filePath = if (withFilePath) joinPath(parentPath, "note-$index.md") else null
            )
            val json = Json.encodeToString(route)
            val decoded = Json.decodeFromString<EditNoteRoute>(json)
            decoded shouldBe route
        }
    }

    test("Feature: note-tasklist-editors, Property 5: EditTaskListRoute serialization round-trip produces equal object") {
        // Feature: note-tasklist-editors, Property 5: Route serialization round-trip
        val json = Json.encodeToString(EditTaskListRoute)
        val decoded = Json.decodeFromString<EditTaskListRoute>(json)
        decoded shouldBe EditTaskListRoute
    }
})
