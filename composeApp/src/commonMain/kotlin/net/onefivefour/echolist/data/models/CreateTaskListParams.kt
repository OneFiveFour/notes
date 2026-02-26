package net.onefivefour.echolist.data.models

data class CreateTaskListParams(
    val name: String,
    val path: String,
    val tasks: List<MainTask>
)
