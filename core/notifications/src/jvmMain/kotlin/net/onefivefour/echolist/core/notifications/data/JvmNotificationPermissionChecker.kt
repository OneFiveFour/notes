package net.onefivefour.echolist.core.notifications.data

import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionChecker

/**
 * JVM Desktop implementation of [NotificationPermissionChecker].
 * Desktop platforms do not require explicit notification permission.
 */
class JvmNotificationPermissionChecker : NotificationPermissionChecker {
    override suspend fun isGranted(): Boolean = true
}
