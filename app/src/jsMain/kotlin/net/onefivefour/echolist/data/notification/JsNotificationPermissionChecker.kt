package net.onefivefour.echolist.data.notification

import net.onefivefour.echolist.domain.NotificationPermissionChecker

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
