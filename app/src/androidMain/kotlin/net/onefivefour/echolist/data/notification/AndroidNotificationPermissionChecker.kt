package net.onefivefour.echolist.data.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import net.onefivefour.echolist.domain.NotificationPermissionChecker

/**
 * Android implementation of [NotificationPermissionChecker].
 *
 * On API 33+ (Tiramisu), checks the POST_NOTIFICATIONS runtime permission.
 * On lower API levels, notification permission is granted at install time.
 */
class AndroidNotificationPermissionChecker(
    private val context: Context
) : NotificationPermissionChecker {
    override suspend fun isGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }
}
