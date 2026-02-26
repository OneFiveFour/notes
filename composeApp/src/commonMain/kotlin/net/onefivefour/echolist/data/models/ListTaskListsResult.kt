package net.onefivefour.echolist.data.models

data class ListTaskListsResult(
    val taskLists: List<TaskListEntry>,
    val entries: List<String>
)
