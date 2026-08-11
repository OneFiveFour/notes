package net.onefivefour.echolist.data.notification

import kotlinx.coroutines.suspendCancellableCoroutine
import net.onefivefour.echolist.domain.NotificationPermissionRequester
import kotlin.coroutines.resume

@JsFun(
    """
    (onGranted, onDenied) => {
        Notification.requestPermission().then(function(result) {
            if (result === 'granted') {
                onGranted();
            } else {
                onDenied();
            }
        }).catch(function() {
            onDenied();
        });
    }
    """
)
private external fun requestPermissionWithCallback(
    onGranted: () -> Unit,
    onDenied: () -> Unit
)

/**
 * WasmJS (browser) implementation of [NotificationPermissionRequester].
 *
 * Uses [Notification.requestPermission] from the Web Notification API
 * via `@JsFun` interop to prompt the user for notification permission.
 * The asynchronous promise is bridged into a Kotlin coroutine using
 * [suspendCancellableCoroutine] with callback-based resolution.
 */
class WasmJsNotificationPermissionRequester : NotificationPermissionRequester {
    override suspend fun request(): Boolean {
        return try {
            suspendCancellableCoroutine { continuation ->
                requestPermissionWithCallback(
                    onGranted = { continuation.resume(true) },
                    onDenied = { continuation.resume(false) }
                )
            }
        } catch (e: Throwable) {
            false
        }
    }
}
