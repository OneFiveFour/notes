package net.onefivefour.echolist.ui.edittasklist

internal sealed interface FocusTarget {
    data class MainTask(val mainTaskId: String) : FocusTarget
    data class SubTask(val mainTaskId: String, val id: String) : FocusTarget
    data object LastMainTask : FocusTarget
    data class LastSubTask(val mainTaskId: String) : FocusTarget
}
