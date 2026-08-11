package net.onefivefour.echolist.data.notification

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import net.onefivefour.echolist.domain.NotificationPermissionRequester
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter

/**
 * iOS implementation of [NotificationPermissionRequester].
 *
 * Requests notification authorization via UNUserNotificationCenter with alert and sound options.
 */
class IosNotificationPermissionRequester : NotificationPermissionRequester {
    override suspend fun request(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            UNUserNotificationCenter.currentNotificationCenter()
                .requestAuthorizationWithOptions(
                    options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound
                ) { granted, error ->
                    if (error != null) {
                        continuation.resume(false)
                    } else {
                        continuation.resume(granted)
                    }
                }
        }
    }
}
