package net.onefivefour.echolist.data.models

data class UpdateTaskListParams(
    val filePath: String,
    val tasks: List<MainTask>
)
