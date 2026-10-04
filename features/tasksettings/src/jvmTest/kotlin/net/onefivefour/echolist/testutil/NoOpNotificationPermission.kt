package net.onefivefour.echolist.testutil

import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionRequester

/**
 * No-op implementations of notification permission interfaces for tests that
 * don't exercise permission logic.
 */
class NoOpNotificationPermissionChecker : NotificationPermissionChecker {
    override suspend fun isGranted(): Boolean = true
}

class NoOpNotificationPermissionRequester : NotificationPermissionRequester {
    override suspend fun request(): Boolean = true
}
