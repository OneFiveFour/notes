package net.onefivefour.echolist.data.notification

import kotlin.time.Clock
import kotlinx.browser.window
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn
import net.onefivefour.echolist.domain.NotificationScheduler

/**
 * External declaration for the browser Notification API.
 */
external class Notification(title: String, options: dynamic = definedExternally) {
    companion object {
        val permission: String
        fun requestPermission(): dynamic
    }
}

/**
 * JS (Kotlin/JS) implementation of [NotificationScheduler].
 *
 * Uses the browser [Notification] API for displaying notifications and
 * [window.setTimeout] for delayed firing at midnight local time of the due date.
 */
class JsNotificationScheduler : NotificationScheduler {

    private val scheduledTimeouts = mutableMapOf<String, Int>()

    override suspend fun schedule(
        taskId: String,
        title: String,
        body: String,
        dueDateIso: String
    ) {
        val dueDate = runCatching { LocalDate.parse(dueDateIso) }.getOrNull()
        if (dueDate == null) {
            println("[JsNotificationScheduler] WARNING: Cannot parse due date '$dueDateIso' for task $taskId")
            return
        }

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        if (dueDate < today) {
            println("[JsNotificationScheduler] WARNING: Due date $dueDateIso is before today for task $taskId, skipping")
            return
        }

        val permission = Notification.permission
        when (permission) {
            "denied" -> {
                println("[JsNotificationScheduler] WARNING: Notification permission denied for task $taskId")
                return
            }
            "default" -> {
                println("[JsNotificationScheduler] WARNING: Notification permission not yet granted for task $taskId, skipping")
                return
            }
        }

        // Cancel any existing timeout for the same task (idempotent replacement)
        scheduledTimeouts.remove(taskId)?.let { existingId ->
            window.clearTimeout(existingId)
        }

        // Calculate delay until midnight of the due date in local timezone
        val dueDateInstant = dueDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val now = Clock.System.now()
        val delayMs = (dueDateInstant - now).inWholeMilliseconds

        // If delayMs <= 0, fire immediately
        val effectiveDelay = if (delayMs <= 0) 0 else delayMs.toInt()

        val timeoutId = window.setTimeout({
            Notification(title, js("({body: body})"))
        }, effectiveDelay)

        scheduledTimeouts[taskId] = timeoutId
    }

    override suspend fun cancel(taskId: String) {
        scheduledTimeouts.remove(taskId)?.let { timeoutId ->
            window.clearTimeout(timeoutId)
        }
    }
}
