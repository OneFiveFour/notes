package net.onefivefour.echolist.core.notifications.data

import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.notifications.domain.NotificationScheduler

internal class PersistentNotificationScheduler(
    private val coordinator: NotificationCoordinator
) : NotificationScheduler {
    override suspend fun schedule(taskId: String, taskListId: String, title: String, body: String, dueDateIso: String) =
        coordinator.schedule(taskId, taskListId, title, body, dueDateIso)

    override suspend fun cancel(taskId: String) = coordinator.cancel(taskId)

    override suspend fun cancelWhenIneligible(taskId: String) = coordinator.cancelWhenIneligible(taskId)

    override suspend fun setEnabled(taskId: String, enabled: Boolean) = coordinator.setEnabled(taskId, enabled)
}