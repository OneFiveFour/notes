package net.onefivefour.echolist.ui.home

import net.onefivefour.echolist.data.models.ItemType

/**
 * Represents user actions that can trigger state transitions.
 */
internal sealed interface CreateItemPillAction {
    /**
     * User clicked a pill to start creating an item of the given type.
     */
    data class BeginCreateItem(val itemType: ItemType) : CreateItemPillAction

    /**
     * User confirmed the IME action with the given title.
     */
    data class ConfirmCreateItem(val title: String) : CreateItemPillAction

    /**
     * User clicked the close button.
     */
    data object OnClose : CreateItemPillAction
}