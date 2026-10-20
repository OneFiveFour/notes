package net.onefivefour.echolist.core.notifications.data

import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import net.onefivefour.echolist.core.notifications.domain.ReminderBackgroundScheduler

/** Desktop/web keep checking while the application is running; Android uses persistent WorkManager jobs. */
internal class InProcessReminderBackgroundScheduler(
    private val restore: suspend () -> Unit,
    private val sync: suspend () -> Result<Unit>
) : ReminderBackgroundScheduler {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val requests = Channel<Unit>(Channel.CONFLATED)
    private var job: Job? = null

    override fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            var syncedDay = ""
            while (true) {
                try {
                    restore()
                    val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
                    val requested = requests.tryReceive().isSuccess
                    if (requested || syncedDay != today) {
                        if (sync().isSuccess) syncedDay = today
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    // Retry after the next wake-up without killing the process-level scheduler.
                }
                withTimeoutOrNull(60_000) {
                    requests.receive()
                    requests.trySend(Unit)
                }
            }
        }
    }

    override fun requestSync() {
        requests.trySend(Unit)
    }
    override fun stop() {
        job?.cancel()
        job = null
    }
}