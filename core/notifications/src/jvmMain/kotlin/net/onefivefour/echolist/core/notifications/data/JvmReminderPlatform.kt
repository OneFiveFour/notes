package net.onefivefour.echolist.core.notifications.data

import net.onefivefour.echolist.core.notifications.domain.Reminder
import net.onefivefour.echolist.core.notifications.domain.ReminderPlatform

internal class JvmReminderPlatform : ReminderPlatform {
    private val display = JvmNotificationScheduler()
    override suspend fun schedule(reminder: Reminder) = Unit
    override suspend fun cancelPending(reminder: Reminder) = Unit
    override suspend fun dismiss(reminder: Reminder) = Unit
    override suspend fun show(reminder: Reminder): Boolean {
        display.showNow(reminder.title, reminder.body)
        return true
    }
}