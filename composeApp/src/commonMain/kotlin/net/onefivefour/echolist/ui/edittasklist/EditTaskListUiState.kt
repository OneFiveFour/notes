package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshots.SnapshotStateList

internal data class EditTaskListUiState(
    val titleState: TextFieldState,
    val mainTasks: SnapshotStateList<UiMainTask>,
    val mode: EditTaskListMode,
    val isAutoDelete: Boolean = false,
    val isPersisted: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
) {
    val isCreateMode: Boolean
        get() = mode is EditTaskListMode.Create

    val isEditMode: Boolean
        get() = mode is EditTaskListMode.Edit
}
