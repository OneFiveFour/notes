package net.onefivefour.echolist.core.notifications.data

import kotlinx.browser.window
import net.onefivefour.echolist.core.notifications.domain.Reminder
import net.onefivefour.echolist.core.notifications.domain.ReminderPlatform
import net.onefivefour.echolist.core.notifications.domain.ReminderState
import net.onefivefour.echolist.core.notifications.domain.ReminderStore

internal class JsReminderStore : ReminderStore {
    override suspend fun read(): ReminderState = window.localStorage.getItem("echolist_reminders_v1")?.let {
        reminderJson.decodeFromString<ReminderState>(it)
    } ?: ReminderState()
    override suspend fun write(state: ReminderState) {
        window.localStorage.setItem("echolist_reminders_v1", reminderJson.encodeToString(state))
    }
}

internal class JsReminderPlatform : ReminderPlatform {
    override suspend fun schedule(reminder: Reminder) = Unit
    override suspend fun cancelPending(reminder: Reminder) = Unit
    override suspend fun dismiss(reminder: Reminder) = Unit
    override suspend fun show(reminder: Reminder): Boolean = try {
        val body = reminder.body
        val tag = reminder.key
        Notification(reminder.title, js("({body: body, tag: tag})"))
        true
    } catch (_: Exception) {
        false
    }
}