package net.onefivefour.echolist.core.notifications.data

import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionRequester

/**
 * JVM Desktop implementation of [NotificationPermissionRequester].
 * Desktop platforms do not require explicit notification permission.
 */
class JvmNotificationPermissionRequester : NotificationPermissionRequester {
    override suspend fun request(): Boolean = true
}