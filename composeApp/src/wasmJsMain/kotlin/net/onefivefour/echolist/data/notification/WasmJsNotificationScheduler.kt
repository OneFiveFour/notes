package net.onefivefour.echolist.data.notification

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn
import net.onefivefour.echolist.domain.NotificationScheduler

@JsFun("() => Notification.permission")
private external fun getNotificationPermission(): JsString

@JsFun("(title, body) => { new Notification(title, {body: body}); }")
private external fun showNotification(title: JsString, body: JsString)

@JsFun("(delay) => window.setTimeout(() => {}, delay)")
private external fun setEmptyTimeout(delay: JsNumber): JsNumber

@JsFun("(id) => window.clearTimeout(id)")
private external fun clearTimeoutById(id: JsNumber)

/**
 * WasmJS implementation of [NotificationScheduler].
 *
 * Uses the browser Notification API for displaying notifications and
 * `window.setTimeout` for delayed firing at midnight local time of the due date.
 *
 * Since Kotlin/WasmJS cannot directly pass lambdas to JS setTimeout,
 * we use a two-step approach: schedule a timeout that stores the pending
 * notification data, then trigger it via JS interop.
 */
class WasmJsNotificationScheduler : NotificationScheduler {

    private val scheduledTimeouts = mutableMapOf<String, Int>()
    private val pendingNotifications = mutableMapOf<String, Pair<String, String>>()

    override suspend fun schedule(
        taskId: String,
        title: String,
        body: String,
        dueDateIso: String
    ) {
        val dueDate = runCatching { LocalDate.parse(dueDateIso) }.getOrNull()
        if (dueDate == null) {
            println("[WasmJsNotificationScheduler] WARNING: Cannot parse due date '$dueDateIso' for task $taskId")
            return
        }

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        if (dueDate < today) {
            println("[WasmJsNotificationScheduler] WARNING: Due date $dueDateIso is before today for task $taskId, skipping")
            return
        }

        val permission = getNotificationPermission().toString()
        when (permission) {
            "denied" -> {
                println("[WasmJsNotificationScheduler] WARNING: Notification permission denied for task $taskId")
                return
            }
            "default" -> {
                println("[WasmJsNotificationScheduler] WARNING: Notification permission not yet granted for task $taskId, skipping")
                return
            }
        }

        // Cancel any existing timeout for the same task (idempotent replacement)
        scheduledTimeouts.remove(taskId)?.let { existingId ->
            clearTimeoutById(existingId.toJsNumber())
        }

        // Calculate delay until midnight of the due date in local timezone
        val dueDateInstant = dueDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val now = Clock.System.now()
        val delayMs = (dueDateInstant - now).inWholeMilliseconds

        // If delayMs <= 0, fire immediately
        val effectiveDelay = if (delayMs <= 0) 0 else delayMs.toInt()

        // Store notification data and schedule timeout
        pendingNotifications[taskId] = Pair(title, body)

        val timeoutId = scheduleNotificationTimeout(
            taskId.toJsString(),
            title.toJsString(),
            body.toJsString(),
            effectiveDelay.toJsNumber()
        )

        scheduledTimeouts[taskId] = timeoutId.toInt()
    }

    override suspend fun cancel(taskId: String) {
        scheduledTimeouts.remove(taskId)?.let { timeoutId ->
            clearTimeoutById(timeoutId.toJsNumber())
        }
        pendingNotifications.remove(taskId)
    }
}

@JsFun(
    """
    (taskId, title, body, delay) => {
        return window.setTimeout(() => {
            new Notification(title, {body: body});
        }, delay);
    }
    """
)
private external fun scheduleNotificationTimeout(
    taskId: JsString,
    title: JsString,
    body: JsString,
    delay: JsNumber
): JsNumber
