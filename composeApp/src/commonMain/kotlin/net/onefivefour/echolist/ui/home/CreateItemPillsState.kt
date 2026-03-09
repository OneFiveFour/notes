package net.onefivefour.echolist.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import net.onefivefour.echolist.data.models.ItemType
import net.onefivefour.echolist.ui.theme.EchoListTheme

/**
 * Represents the UI state of the CreateItemPills composable.
 */
internal sealed interface PillsUiState {
    /**
     * Default state where all three pills (Note, Task, Folder) are visible.
     */
    data object Idle : PillsUiState

    /**
     * State where a pill has been selected and expanded into a text field for title entry.
     */
    data class Input(val itemType: ItemType) : PillsUiState
}

/**
 * Represents user actions that can trigger state transitions.
 */
internal sealed interface PillsAction {
    /**
     * User clicked a pill to start creating an item of the given type.
     */
    data class PillClicked(val itemType: ItemType) : PillsAction

    /**
     * User confirmed the IME action with the given title.
     */
    data class ImeConfirm(val title: String) : PillsAction

    /**
     * User clicked the close button.
     */
    data object CloseClicked : PillsAction
}

/**
 * Pure state machine function that computes the next state given the current state and action.
 */
internal fun nextState(current: PillsUiState, action: PillsAction): PillsUiState {
    return when (action) {
        is PillsAction.PillClicked -> PillsUiState.Input(action.itemType)
        is PillsAction.ImeConfirm -> PillsUiState.Idle
        PillsAction.CloseClicked -> PillsUiState.Idle
    }
}

/**
 * Pure function that determines which callback to invoke based on item type and title.
 * Returns the item type if the title is non-empty, null otherwise.
 */
internal fun resolveImeAction(itemType: ItemType, title: String): ItemType? {
    return if (title.isNotBlank()) itemType else null
}

/**
 * Extension function that maps ItemType to its corresponding pill color.
 */
@Composable
internal fun ItemType.pillColor(): Color = when (this) {
    ItemType.NOTE -> EchoListTheme.echoListColorScheme.noteColor
    ItemType.TASK_LIST -> EchoListTheme.echoListColorScheme.taskColor
    ItemType.FOLDER -> EchoListTheme.echoListColorScheme.folderColor
    ItemType.UNSPECIFIED -> Color.Transparent
}

/**
 * Extension function that maps ItemType to its display label.
 */
internal fun ItemType.pillLabel(): String = when (this) {
    ItemType.NOTE -> "Note"
    ItemType.TASK_LIST -> "Task"
    ItemType.FOLDER -> "Folder"
    ItemType.UNSPECIFIED -> ""
}
