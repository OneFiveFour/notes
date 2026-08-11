package net.onefivefour.echolist.data.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import net.onefivefour.echolist.domain.NotificationPermissionRequester

/**
 * Android implementation of [NotificationPermissionRequester].
 *
 * On API 33+ (Tiramisu), runtime notification permission is required.
 * Since requesting a permission dialog requires an Activity context with an
 * ActivityResultLauncher, and this class operates at the ViewModel/DI layer,
 * it checks the current permission status. If permission is not yet granted,
 * it returns false so the toggle gets deactivated gracefully.
 *
 * The actual system permission dialog should be triggered via the Activity's
 * registered ActivityResultLauncher when a more interactive flow is implemented.
 * For now, this serves as a safe fallback that prevents scheduling notifications
 * without proper permission.
 */
class AndroidNotificationPermissionRequester(
    private val context: Context
) : NotificationPermissionRequester {
    override suspend fun request(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
