package net.onefivefour.echolist.data.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn
import net.onefivefour.echolist.domain.NotificationScheduler

/**
 * Android implementation of [NotificationScheduler] that uses [AlarmManager]
 * to schedule exact alarms and a [TaskReminderReceiver] to post notifications.
 */
class AndroidNotificationScheduler(
    private val context: Context
) : NotificationScheduler {

    override suspend fun schedule(
        taskId: String,
        title: String,
        body: String,
        dueDateIso: String
    ) {
        val dueDate = runCatching { LocalDate.parse(dueDateIso) }.getOrNull()
        if (dueDate == null) {
            Log.w(TAG, "Cannot schedule notification for task $taskId: invalid date '$dueDateIso'")
            return
        }

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        if (dueDate < today) {
            Log.w(TAG, "Skipping notification for task $taskId: due date $dueDateIso is in the past")
            return
        }

        if (!hasNotificationPermission()) {
            Log.w(TAG, "Skipping notification for task $taskId: POST_NOTIFICATIONS permission denied")
            return
        }

        val triggerAtMillis = dueDate
            .atStartOfDayIn(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskReminderReceiver.EXTRA_TITLE, title)
            putExtra(TaskReminderReceiver.EXTRA_BODY, body)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    override suspend fun cancel(taskId: String) {
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(it)
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(taskId.hashCode())
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    companion object {
        private const val TAG = "AndroidNotifScheduler"
    }
}
