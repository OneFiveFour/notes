package net.onefivefour.echolist.core.tasks.domain.model

data class CreateTaskListParams(
    val name: String,
    val parentDir: String,
    val tasks: List<MainTask>,
    val isAutoDelete: Boolean = false
)