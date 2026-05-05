package net.onefivefour.echolist.domain.model

data class MainTask(
    val id: String,
    val description: String,
    val isDone: Boolean,
    val dueDate: String,
    val recurrence: String,
    val subTasks: List<SubTask>
)
