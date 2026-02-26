package net.onefivefour.echolist.data.models

data class TaskList(
    val filePath: String,
    val name: String,
    val tasks: List<MainTask>,
    val updatedAt: Long
)
