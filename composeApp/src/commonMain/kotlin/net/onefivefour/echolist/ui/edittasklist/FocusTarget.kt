package net.onefivefour.echolist.ui.edittasklist

internal sealed interface FocusTarget {
    data class MainTask(val mainTaskId: Long) : FocusTarget
    data class SubTask(val mainTaskId: Long, val id: Long) : FocusTarget
    data object LastMainTask : FocusTarget
    data class LastSubTask(val mainTaskId: Long) : FocusTarget
}