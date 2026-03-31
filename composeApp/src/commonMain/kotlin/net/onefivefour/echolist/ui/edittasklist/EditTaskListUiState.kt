package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshots.SnapshotStateList

data class EditTaskListUiState(
    val titleState: TextFieldState,
    val mainTasks: SnapshotStateList<MainTaskDraft>,
    val mode: EditTaskListMode,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
) {
    val isSaveEnabled: Boolean
        get() = titleState.text.isNotBlank() && !isLoading && !isSaving

    val isCreateMode: Boolean
        get() = mode is EditTaskListMode.Create

    val isEditMode: Boolean
        get() = mode is EditTaskListMode.Edit
}
