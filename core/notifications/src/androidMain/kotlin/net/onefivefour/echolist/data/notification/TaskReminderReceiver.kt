package net.onefivefour.echolist.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.notifications.data.AndroidReminderBackgroundScheduler
import org.koin.core.context.GlobalContext

/** Stable receiver name retained for existing PendingIntents. */
class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val account = intent.getStringExtra(EXTRA_ACCOUNT_ID)
        val task = intent.getStringExtra(EXTRA_TASK_ID)
        val date = intent.getStringExtra(EXTRA_DUE_DATE)
        val result = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                if (account == null || task == null || date == null) {
                    AndroidReminderBackgroundScheduler(context).enqueueRefresh().await()
                } else {
                    GlobalContext.get().get<NotificationCoordinator>().deliver(account, task, date)
                }
            } catch (_: Exception) {
                Log.w("EchoListReminders", "Unable to deliver reminder; background reconciliation will retry")
            } finally {
                result.finish()
            }
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_LIST_ID = "task_list_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_ACCOUNT_ID = "account_id"
        const val EXTRA_DUE_DATE = "due_date"
    }
}