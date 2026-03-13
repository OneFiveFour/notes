package net.onefivefour.echolist.ui.home

import net.onefivefour.echolist.data.models.ItemType

/**
 * Represents the UI state of the CreateItemPills composable.
 */
internal sealed interface CreateItemPillUiState {
    /**
     * Default state where all three pills (Note, Task, Folder) are visible.
     */
    data object Idle : CreateItemPillUiState

    /**
     * State where a pill has been selected and expanded into a text field for title entry.
     */
    data class CreateMode(val itemType: ItemType) : CreateItemPillUiState
}