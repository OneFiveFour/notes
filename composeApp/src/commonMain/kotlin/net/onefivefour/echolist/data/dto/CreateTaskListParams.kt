package net.onefivefour.echolist.data.dto

import net.onefivefour.echolist.domain.model.MainTask

data class CreateTaskListParams(
    val name: String,
    val path: String,
    val tasks: List<MainTask>,
    val isAutoDelete: Boolean = false
)
