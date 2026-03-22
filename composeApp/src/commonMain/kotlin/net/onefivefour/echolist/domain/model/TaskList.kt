package net.onefivefour.echolist.domain.model

data class TaskList(
    val filePath: String,
    val name: String,
    val tasks: List<MainTask>,
    val updatedAt: Long
)