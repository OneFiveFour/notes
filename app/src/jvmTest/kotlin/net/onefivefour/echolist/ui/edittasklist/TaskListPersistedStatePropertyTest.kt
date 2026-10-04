package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.text.input.TextFieldState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import net.onefivefour.echolist.ui.edittasklist.EditTaskListMode
import net.onefivefour.echolist.ui.edittasklist.EditTaskListUiState

// Feature: note-tasklist-editors, Property tests for editor UI state

@OptIn(io.kotest.common.ExperimentalKotest::class)
class TaskListPersistedStatePropertyTest : FunSpec({

    test("EditTaskListUiState exposes persisted status explicitly") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.boolean(),
            Arb.boolean(),
            Arb.boolean()
        ) { isPersisted, isLoading, isSaving ->
            val uiState = EditTaskListUiState(
                titleState = TextFieldState(),
                uiMainTasks = androidx.compose.runtime.mutableStateListOf(),
                mode = EditTaskListMode.Create(""),
                isPersisted = isPersisted,
                isLoading = isLoading,
                isSaving = isSaving
            )

            uiState.isPersisted shouldBe isPersisted
        }
    }
})
