package net.onefivefour.echolist.core.tasks.domain

import net.onefivefour.echolist.core.notifications.domain.NotificationScheduler

import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.core.tasks.domain.model.MainTask

/**
 * Determines whether a task needs a notification and either schedules or cancels it.
 *
 * Scheduling rules:
 * - If the task has isNotificationEnabled set to false, any existing notification is canceled immediately.
 * - Tasks without a due date and completed tasks are canceled, independently of recurrence.
 * - If the dueDate cannot be parsed as a valid ISO-8601 date, the notification is canceled.
 * - Past due dates are forwarded to the persistent scheduler for one-time catch-up delivery.
 * - Otherwise, a notification is scheduled with the appropriate title and body.
 *
 * @param scheduler the platform notification scheduler implementation
 * @param task the task to evaluate for notification scheduling
 * @param taskListName the name of the containing task list (used in notification title/body)
 * @param taskListId the id of the containing task list (needed for notification actions)
 */
suspend fun scheduleTaskNotification(
    scheduler: NotificationScheduler,
    task: MainTask,
    taskListName: String,
    taskListId: String
) {
    if (!task.isNotificationEnabled) {
        scheduler.cancelWhenIneligible(task.id)
        return
    }

    if (task.dueDate.isBlank() || task.isDone) {
        scheduler.cancelWhenIneligible(task.id)
        return
    }

    val dueDate = runCatching { LocalDate.parse(task.dueDate) }.getOrNull()
    if (dueDate == null) {
        scheduler.cancelWhenIneligible(task.id)
        return
    }

    scheduler.schedule(
        taskId = task.id,
        taskListId = taskListId,
        title = "Task due: $taskListName",
        body = task.description.ifEmpty { taskListName }.take(200),
        dueDateIso = task.dueDate
    )
}