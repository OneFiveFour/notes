package net.onefivefour.echolist.domain.model

data class MainTask(
    val id: String,
    val description: String,
    val isDone: Boolean,
    val dueDate: String,
    val recurrence: String,
    val isNotificationEnabled: Boolean = true,
    val subTasks: List<SubTask>
)
