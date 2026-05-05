package net.onefivefour.echolist.ui.edittasklist

internal data class KeyboardActionResolution(
    val focusTarget: FocusTarget? = null,
    val mutation: KeyboardMutation? = null,
    val shouldClearFocus: Boolean = false
)

internal sealed interface KeyboardMutation {
    data object AddMainTask : KeyboardMutation
    data class RemoveMainTask(val mainTaskId: String) : KeyboardMutation
    data class AddSubTask(val mainTaskId: String) : KeyboardMutation
    data class RemoveSubTask(val mainTaskId: String, val subTaskId: String) : KeyboardMutation
}
