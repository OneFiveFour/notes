package net.onefivefour.echolist.core.notifications.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderRestoreReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val scheduler = AndroidReminderBackgroundScheduler(context)
                scheduler.enqueuePeriodic().await()
                scheduler.enqueueRefresh().await()
            } catch (_: Exception) {
                Log.w("EchoListReminders", "Unable to enqueue reminder restoration")
            } finally {
                pending.finish()
            }
        }
    }
}