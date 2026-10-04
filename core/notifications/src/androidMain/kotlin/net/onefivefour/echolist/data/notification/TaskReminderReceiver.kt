package net.onefivefour.echolist.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * BroadcastReceiver that fires when an AlarmManager alarm triggers.
 * Posts a local notification for the recurring task reminder.
 */
class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val body = intent.getStringExtra(EXTRA_BODY) ?: return
        val taskListId = intent.getStringExtra(EXTRA_TASK_LIST_ID)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        ensureNotificationChannel(notificationManager)

        val publicNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("EchoList")
            .setContentText("You have a task reminder")
            .build()

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotification)
            .setAutoCancel(true)

        // Only offer the "Done" action when we know which task list the task belongs
        // to, since completing it requires updating the whole list.
        if (taskListId != null) {
            notificationBuilder.addAction(
                buildDoneAction(context, taskId, taskListId)
            )
        }

        notificationManager.notify(taskId.hashCode(), notificationBuilder.build())
    }

    private fun buildDoneAction(
        context: Context,
        taskId: String,
        taskListId: String
    ): NotificationCompat.Action {
        val doneIntent = Intent(context, TaskDoneReceiver::class.java).apply {
            action = TaskDoneReceiver.ACTION_MARK_DONE
            putExtra(TaskDoneReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskDoneReceiver.EXTRA_TASK_LIST_ID, taskListId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            ("done_$taskId").hashCode(),
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(
            android.R.drawable.checkbox_on_background,
            "Done",
            donePendingIntent
        ).build()
    }

    private fun ensureNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existing = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for recurring task reminders"
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_LIST_ID = "task_list_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"

        private const val CHANNEL_ID = "echolist_task_reminders"
        private const val CHANNEL_NAME = "Task Reminders"
    }
}
