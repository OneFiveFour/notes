package net.onefivefour.echolist.data.notification

import net.onefivefour.echolist.domain.NotificationPermissionChecker

/**
 * JVM Desktop implementation of [NotificationPermissionChecker].
 * Desktop platforms do not require explicit notification permission.
 */
class JvmNotificationPermissionChecker : NotificationPermissionChecker {
    override suspend fun isGranted(): Boolean = true
}
