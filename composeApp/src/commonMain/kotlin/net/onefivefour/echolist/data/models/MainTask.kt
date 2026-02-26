package net.onefivefour.echolist.data.models

data class MainTask(
    val description: String,
    val done: Boolean,
    val dueDate: String,
    val recurrence: String,
    val subTasks: List<SubTask>
)
