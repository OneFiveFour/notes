package net.onefivefour.echolist.core.tasks.domain.model

import net.onefivefour.echolist.core.tasks.domain.model.MainTask

data class UpdateTaskListParams(
    val id: String,
    val title: String,
    val tasks: List<MainTask>,
    val isAutoDelete: Boolean
)
