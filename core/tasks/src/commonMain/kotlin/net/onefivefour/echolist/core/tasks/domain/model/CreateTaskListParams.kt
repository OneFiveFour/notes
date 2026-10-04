package net.onefivefour.echolist.core.tasks.domain.model

import net.onefivefour.echolist.core.tasks.domain.model.MainTask

data class CreateTaskListParams(
    val name: String,
    val parentDir: String,
    val tasks: List<MainTask>,
    val isAutoDelete: Boolean = false
)
