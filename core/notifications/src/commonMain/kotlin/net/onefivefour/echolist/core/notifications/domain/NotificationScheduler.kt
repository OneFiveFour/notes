package net.onefivefour.echolist.core.notifications.domain

/**
 * Platform-agnostic contract for scheduling and canceling local notifications
 * tied to tasks with a due date, including non-recurring tasks.
 */
interface NotificationScheduler {
    /** Device-local preference; independent of recurrence and the backend task payload. */
    suspend fun setEnabled(taskId: String, enabled: Boolean) {}

    /** A policy observation may be stale; persistent implementations check their authoritative state first. */
    suspend fun cancelWhenIneligible(taskId: String) = cancel(taskId)

    /**
     * Schedule a local notification for the given task.
     * Persistent implementations reconcile the authoritative task metadata before scheduling;
     * task-list mutations must update the coordinator so delayed UI callbacks cannot roll them back.
     *
     * @param taskId unique identifier for the task (used as notification ID)
     * @param taskListId identifier of the owning task list, needed so notification
     *   actions (e.g. "Done") can mark the task complete
     * @param title notification title (max 100 characters)
     * @param body notification body (max 300 characters)
     * @param dueDateIso ISO-8601 date string (yyyy-MM-dd) for when the notification should fire
     */
    suspend fun schedule(
        taskId: String,
        taskListId: String,
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