@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package net.onefivefour.echolist.core.notifications.data

import net.onefivefour.echolist.core.notifications.domain.Reminder
import net.onefivefour.echolist.core.notifications.domain.ReminderPlatform
import net.onefivefour.echolist.core.notifications.domain.ReminderState
import net.onefivefour.echolist.core.notifications.domain.ReminderStore

@JsFun("() => window.localStorage.getItem('echolist_reminders_v1')")
private external fun readReminders(): JsString?

@JsFun("(value) => window.localStorage.setItem('echolist_reminders_v1', value)")
private external fun writeReminders(value: JsString)

@JsFun("(title, body, tag) => { new Notification(title, {body, tag}); }")
private external fun displayReminder(title: JsString, body: JsString, tag: JsString)

internal class WasmReminderStore : ReminderStore {
    override suspend fun read(): ReminderState = readReminders()?.toString()?.let {
        reminderJson.decodeFromString<ReminderState>(it)
    } ?: ReminderState()
    override suspend fun write(state: ReminderState) {
        writeReminders(reminderJson.encodeToString(state).toJsString())
    }
}

internal class WasmReminderPlatform : ReminderPlatform {
    override suspend fun schedule(reminder: Reminder) = Unit
    override suspend fun cancelPending(reminder: Reminder) = Unit
    override suspend fun dismiss(reminder: Reminder) = Unit
    override suspend fun show(reminder: Reminder): Boolean = try {
        displayReminder(reminder.title.toJsString(), reminder.body.toJsString(), reminder.key.toJsString())
        true
    } catch (_: Exception) {
        false
    }
}