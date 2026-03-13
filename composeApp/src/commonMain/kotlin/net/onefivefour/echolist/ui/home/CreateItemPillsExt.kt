package net.onefivefour.echolist.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import net.onefivefour.echolist.data.models.ItemType
import net.onefivefour.echolist.ui.theme.EchoListTheme

/**
 * State machine function that computes the next state given the current state and action.
 */
internal fun nextState(action: CreateItemPillAction): CreateItemPillUiState {
    return when (action) {
        is CreateItemPillAction.BeginCreateItem -> CreateItemPillUiState.CreateMode(action.itemType)
        is CreateItemPillAction.ConfirmCreateItem -> CreateItemPillUiState.Idle
        CreateItemPillAction.OnClose -> CreateItemPillUiState.Idle
    }
}

/**
 * determines which callback to invoke based on item type and title.
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
    ItemType.NOTE -> "+ Note"
    ItemType.TASK_LIST -> "+ Task"
    ItemType.FOLDER -> "+ Folder"
    ItemType.UNSPECIFIED -> ""
}
