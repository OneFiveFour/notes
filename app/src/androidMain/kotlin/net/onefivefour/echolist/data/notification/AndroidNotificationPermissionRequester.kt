package net.onefivefour.echolist.data.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import net.onefivefour.echolist.domain.NotificationPermissionRequester

/**
 * Android implementation of [NotificationPermissionRequester] that delegates
 * the actual permission request to the Activity via [PermissionResultBridge].
 *
 * The bridge emits a generic request; the Activity handles the full permission flow
 * including rationale display, system dialog, and settings redirect if needed.
 * Once the flow completes, the Activity delivers the result back through the bridge.
 */
class AndroidNotificationPermissionRequester(
    private val context: Context,
    private val bridge: PermissionResultBridge
) : NotificationPermissionRequester {

    override suspend fun request(): Boolean {
        // Below API 33, no runtime permission needed
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }

        // Already granted — no need to ask
        if (isGranted()) return true

        // Delegate to the Activity via the bridge.
        // The Activity will determine the right UI to show (rationale, system dialog, or settings).
        return bridge.awaitPermission()
    }

    private fun isGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
