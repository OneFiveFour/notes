package net.onefivefour.echolist.data.models

import net.onefivefour.echolist.domain.model.MainTask

data class UpdateTaskListParams(
    val id: String,
    val title: String,
    val tasks: List<MainTask>
)
