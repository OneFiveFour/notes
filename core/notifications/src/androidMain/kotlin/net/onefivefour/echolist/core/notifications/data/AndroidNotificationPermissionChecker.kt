package net.onefivefour.echolist.core.notifications.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationManager
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionChecker

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
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = manager.getNotificationChannel(AndroidReminderPlatform.CHANNEL)
            if (channel?.importance == NotificationManager.IMPORTANCE_NONE) return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }
}