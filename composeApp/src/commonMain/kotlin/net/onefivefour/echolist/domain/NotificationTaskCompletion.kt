package net.onefivefour.echolist.domain

import net.onefivefour.echolist.data.models.UpdateTaskListParams
import net.onefivefour.echolist.domain.repository.TaskListRepository

/**
 * Marks a recurring task as done in response to a notification "Done" action.
 *
 * There is no "mark single task done" endpoint, so this fetches the owning task
 * list, flips the matching task's [MainTask.isDone] flag, and syncs the whole
 * list. The backend advances the recurring task to its next due date and returns
 * the updated list. On success the notification is rescheduled for the newly
 * computed due date (or cancelled if the task is no longer recurring / due).
 *
 * The currently shown notification is dismissed up front via [NotificationScheduler.cancel]
 * so the interaction feels immediate regardless of network latency.
 *
 * @param taskListRepository repository used to read and update the task list
 * @param notificationScheduler scheduler used to dismiss and reschedule the notification
 * @param taskListId id of the task list that owns the task
 * @param taskId id of the recurring task to mark done
 * @return [Result.success] when the task was marked done and synced, otherwise [Result.failure]
 */
suspend fun completeRecurringTaskFromNotification(
    taskListRepository: TaskListRepository,
    notificationScheduler: NotificationScheduler,
    taskListId: String,
    taskId: String
): Result<Unit> {
    // Dismiss the currently shown notification immediately.
    notificationScheduler.cancel(taskId)

    val taskList = taskListRepository.getTaskList(taskListId).getOrElse {
        return Result.failure(it)
    }

    if (taskList.tasks.none { it.id == taskId }) {
        return Result.failure(
            IllegalStateException("Task $taskId not found in task list $taskListId")
        )
    }

    val updatedTasks = taskList.tasks.map { task ->
        if (task.id == taskId) task.copy(isDone = true) else task
    }

    val updated = taskListRepository.updateTaskList(
        UpdateTaskListParams(
            id = taskList.id,
            title = taskList.name,
            tasks = updatedTasks,
            isAutoDelete = taskList.isAutoDelete
        )
    ).getOrElse {
        return Result.failure(it)
    }

    // Reschedule the notification for the backend-computed next due date.
    updated.tasks.firstOrNull { it.id == taskId }?.let { syncedTask ->
        scheduleTaskNotification(
            scheduler = notificationScheduler,
            task = syncedTask,
            taskListName = updated.name,
            taskListId = updated.id
        )
    }

    return Result.success(Unit)
}
