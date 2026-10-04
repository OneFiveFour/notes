package net.onefivefour.echolist.feature.tasklist.ui

internal sealed interface EditTaskListMode {
    data class Create(val parentDir: String) : EditTaskListMode
    data class Edit(val taskListId: String) : EditTaskListMode
}