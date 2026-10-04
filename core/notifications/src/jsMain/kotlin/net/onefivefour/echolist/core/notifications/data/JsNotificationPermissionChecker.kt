package net.onefivefour.echolist.core.notifications.data

import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionChecker

/**
 * JS (browser) implementation of [NotificationPermissionChecker].
 *
 * Uses the [Notification.permission] property from the Web Notification API
 * to check whether notification permission is currently granted.
 */
class JsNotificationPermissionChecker : NotificationPermissionChecker {
    override suspend fun isGranted(): Boolean {
        return try {
            Notification.permission == "granted"
        } catch (e: Throwable) {
            false
        }
    }
}