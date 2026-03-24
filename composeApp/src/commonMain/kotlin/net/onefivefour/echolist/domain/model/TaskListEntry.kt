package net.onefivefour.echolist.domain.model

data class TaskListEntry(
    val id: String,
    val filePath: String,
    val name: String,
    val updatedAt: Long
)