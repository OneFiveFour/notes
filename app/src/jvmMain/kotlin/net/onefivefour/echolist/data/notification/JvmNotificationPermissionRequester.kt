package net.onefivefour.echolist.data.notification

import net.onefivefour.echolist.domain.NotificationPermissionRequester

/**
 * JVM Desktop implementation of [NotificationPermissionRequester].
 * Desktop platforms do not require explicit notification permission.
 */
class JvmNotificationPermissionRequester : NotificationPermissionRequester {
    override suspend fun request(): Boolean = true
}
