package net.onefivefour.echolist.data.notification

import net.onefivefour.echolist.domain.NotificationPermissionChecker

@JsFun("() => Notification.permission")
private external fun getPermission(): JsString

/**
 * WasmJS (browser) implementation of [NotificationPermissionChecker].
 *
 * Uses the [Notification.permission] property from the Web Notification API
 * via `@JsFun` interop to check whether notification permission is currently granted.
 */
class WasmJsNotificationPermissionChecker : NotificationPermissionChecker {
    override suspend fun isGranted(): Boolean {
        return try {
            getPermission().toString() == "granted"
        } catch (e: Throwable) {
            false
        }
    }
}
