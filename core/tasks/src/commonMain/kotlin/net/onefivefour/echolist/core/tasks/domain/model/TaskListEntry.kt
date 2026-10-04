package net.onefivefour.echolist.core.tasks.domain.model

data class TaskListEntry(
    val id: String,
    val parentDir: String,
    val name: String,
    val updatedAt: Long
)