package net.onefivefour.echolist.data.models

import net.onefivefour.echolist.domain.model.MainTask

data class UpdateTaskListParams(
    val filePath: String,
    val tasks: List<MainTask>
)