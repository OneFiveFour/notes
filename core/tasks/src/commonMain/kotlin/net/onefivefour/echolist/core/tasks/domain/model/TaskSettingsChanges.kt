package net.onefivefour.echolist.core.tasks.domain.model

data class TaskSettingsChanges(
    val mainTaskId: String,
    val dueDate: String,
    val recurrence: String,
    val isNotificationEnabled: Boolean = true
)
