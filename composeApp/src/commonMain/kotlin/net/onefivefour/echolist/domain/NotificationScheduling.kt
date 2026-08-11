package net.onefivefour.echolist.domain

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import net.onefivefour.echolist.domain.model.MainTask

/**
 * Determines whether a task needs a notification and either schedules or cancels it.
 *
 * Scheduling rules:
 * - If the task has isNotificationEnabled set to false, any existing notification is canceled immediately.
 * - If the task has a blank dueDate or blank recurrence, any existing notification is canceled.
 * - If the dueDate cannot be parsed as a valid ISO-8601 date, the notification is canceled.
 * - If the dueDate is strictly before today, scheduling is skipped (no schedule, no cancel).
 * - Otherwise, a notification is scheduled with the appropriate title and body.
 *
 * @param scheduler the platform notification scheduler implementation
 * @param task the task to evaluate for notification scheduling
 * @param taskListName the name of the containing task list (used in notification title/body)
 */
suspend fun scheduleTaskNotification(
    scheduler: NotificationScheduler,
    task: MainTask,
    taskListName: String
) {
    // Guard: per-task notifications disabled → cancel and return immediately
    if (!task.isNotificationEnabled) {
        scheduler.cancel(task.id)
        return
    }

    if (task.dueDate.isBlank() || task.recurrence.isBlank()) {
        scheduler.cancel(task.id)
        return
    }

    val dueDate = runCatching { LocalDate.parse(task.dueDate) }.getOrNull()
    if (dueDate == null) {
        scheduler.cancel(task.id)
        return
    }

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    if (dueDate < today) {
        // Past due date — skip scheduling silently (no cancel either)
        return
    }

    scheduler.schedule(
        taskId = task.id,
        title = "Task due: $taskListName",
        body = task.description.ifEmpty { taskListName }.take(200),
        dueDateIso = task.dueDate
    )
}
