package net.onefivefour.echolist.data.notification

import kotlinx.coroutines.await
import net.onefivefour.echolist.domain.NotificationPermissionRequester
import kotlin.js.Promise

/**
 * JS (browser) implementation of [NotificationPermissionRequester].
 *
 * Uses [Notification.requestPermission] from the Web Notification API
 * to prompt the user for notification permission.
 */
class JsNotificationPermissionRequester : NotificationPermissionRequester {
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    override suspend fun request(): Boolean {
        return try {
            val result = (Notification.requestPermission() as Promise<String>).await()
            result == "granted"
        } catch (e: Throwable) {
            false
        }
    }
}
