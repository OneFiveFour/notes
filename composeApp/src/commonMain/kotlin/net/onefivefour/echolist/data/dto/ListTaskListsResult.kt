package net.onefivefour.echolist.data.dto

import net.onefivefour.echolist.domain.model.TaskListEntry

data class ListTaskListsResult(
    val taskLists: List<TaskListEntry>,
    val entries: List<String>
)