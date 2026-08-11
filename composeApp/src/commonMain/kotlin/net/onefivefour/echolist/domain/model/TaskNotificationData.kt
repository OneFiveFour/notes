package net.onefivefour.echolist.domain.model

/**
 * Data required to schedule a notification for a recurring task.
 *
 * @param taskId unique identifier for the task
 * @param taskDescription the task's description text
 * @param taskListName the name of the task list containing this task
 * @param dueDateIso ISO-8601 date string (yyyy-MM-dd) for when the task is due
 */
data class TaskNotificationData(
    val taskId: String,
    val taskDescription: String,
    val taskListName: String,
    val dueDateIso: String
)
