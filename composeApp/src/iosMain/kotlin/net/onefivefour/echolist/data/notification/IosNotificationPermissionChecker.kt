package net.onefivefour.echolist.data.notification

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import net.onefivefour.echolist.domain.NotificationPermissionChecker
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNUserNotificationCenter

/**
 * iOS implementation of [NotificationPermissionChecker].
 *
 * Queries UNUserNotificationCenter for the current authorization status.
 */
class IosNotificationPermissionChecker : NotificationPermissionChecker {
    override suspend fun isGranted(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            UNUserNotificationCenter.currentNotificationCenter()
                .getNotificationSettingsWithCompletionHandler { settings ->
                    val granted = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
                    continuation.resume(granted)
                }
        }
    }
}
