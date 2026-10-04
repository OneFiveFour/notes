package net.onefivefour.echolist.ui.edittasklist

sealed interface EditTaskListMode {
    data class Create(val parentDir: String) : EditTaskListMode
    data class Edit(val taskListId: String) : EditTaskListMode
}
