package net.onefivefour.echolist.testutil

import net.onefivefour.echolist.domain.NotificationScheduler

/**
 * No-op implementation of [NotificationScheduler] for tests that don't exercise notification logic.
 */
class NoOpNotificationScheduler : NotificationScheduler {
    override suspend fun schedule(taskId: String, title: String, body: String, dueDateIso: String) {
        // No-op
    }

    override suspend fun cancel(taskId: String) {
        // No-op
    }
}
