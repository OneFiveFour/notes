package net.onefivefour.echolist.ui

import androidx.compose.foundation.text.input.TextFieldState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import net.onefivefour.echolist.ui.editnote.EditNoteMode
import net.onefivefour.echolist.ui.editnote.EditNoteUiState
import net.onefivefour.echolist.ui.edittasklist.EditTaskListMode
import net.onefivefour.echolist.ui.edittasklist.EditTaskListUiState

// Feature: note-tasklist-editors, Property tests for editor UI state

@OptIn(io.kotest.common.ExperimentalKotest::class)
class SaveEnabledPropertyTest : FunSpec({

    test("Property 4: EditNoteUiState.isSaveEnabled iff text is non-blank and not loading or saving") {
        // **Validates: Requirements 8.1, 8.2**
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..50),
            Arb.boolean(),
            Arb.boolean()
        ) { text, isLoading, isSaving ->
            val titleState = TextFieldState()
            titleState.edit { replace(0, length, text) }

            val uiState = EditNoteUiState(
                titleState = titleState,
                mode = EditNoteMode.Create(""),
                isLoading = isLoading,
                isSaving = isSaving
            )

            val expected = text.isNotBlank() && !isLoading && !isSaving
            uiState.isSaveEnabled shouldBe expected
        }
    }

    test("EditTaskListUiState exposes persisted status explicitly") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.boolean(),
            Arb.boolean(),
            Arb.boolean()
        ) { isPersisted, isLoading, isSaving ->
            val uiState = EditTaskListUiState(
                titleState = TextFieldState(),
                mainTasks = androidx.compose.runtime.mutableStateListOf(),
                mode = EditTaskListMode.Create(""),
                isPersisted = isPersisted,
                isLoading = isLoading,
                isSaving = isSaving
            )

            uiState.isPersisted shouldBe isPersisted
        }
    }
})
