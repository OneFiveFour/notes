package net.onefivefour.echolist.data.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.onefivefour.echolist.domain.NotificationScheduler
import net.onefivefour.echolist.domain.completeRecurringTaskFromNotification
import net.onefivefour.echolist.domain.repository.TaskListRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * BroadcastReceiver that handles the "Done" action on a task reminder notification.
 *
 * Dismisses the notification immediately, then marks the recurring task as done via
 * [completeRecurringTaskFromNotification], letting the backend compute the next due
 * date and rescheduling the notification accordingly.
 */
class TaskDoneReceiver : BroadcastReceiver(), KoinComponent {

    private val taskListRepository: TaskListRepository by inject()
    private val notificationScheduler: NotificationScheduler by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_MARK_DONE) return
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        val taskListId = intent.getStringExtra(EXTRA_TASK_LIST_ID) ?: return

        // Dismiss the notification right away for a responsive feel.
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(taskId.hashCode())

        // Keep the receiver alive while the suspend work runs.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                completeRecurringTaskFromNotification(
                    taskListRepository = taskListRepository,
                    notificationScheduler = notificationScheduler,
                    taskListId = taskListId,
                    taskId = taskId
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_MARK_DONE = "net.onefivefour.echolist.action.MARK_TASK_DONE"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_LIST_ID = "task_list_id"
    }
}
