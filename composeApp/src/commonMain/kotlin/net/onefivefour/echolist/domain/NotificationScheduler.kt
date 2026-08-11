package net.onefivefour.echolist.domain

/**
 * Platform-agnostic contract for scheduling and canceling local notifications
 * tied to recurring tasks.
 */
interface NotificationScheduler {
    /**
     * Schedule a local notification for the given task.
     * If a notification already exists for this [taskId], it is replaced.
     *
     * @param taskId unique identifier for the task (used as notification ID)
     * @param title notification title (max 100 characters)
     * @param body notification body (max 300 characters)
     * @param dueDateIso ISO-8601 date string (yyyy-MM-dd) for when the notification should fire
     */
    suspend fun schedule(
        taskId: String,
        title: String,
        body: String,
        dueDateIso: String
    )

    /**
     * Cancel any pending notification for the given task.
     *
     * @param taskId unique identifier of the task whose notification should be canceled
     */
    suspend fun cancel(taskId: String)
}
