package net.onefivefour.echolist.core.notifications.di

import net.onefivefour.echolist.core.notifications.data.AndroidNotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.data.AndroidNotificationPermissionRequester
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import net.onefivefour.echolist.core.notifications.database.NotificationDatabase
import net.onefivefour.echolist.core.notifications.data.SqlReminderStore
import net.onefivefour.echolist.core.notifications.data.AndroidReminderPlatform
import net.onefivefour.echolist.core.notifications.data.AndroidReminderBackgroundScheduler
import net.onefivefour.echolist.core.notifications.domain.ReminderStore
import net.onefivefour.echolist.core.notifications.domain.ReminderPlatform
import net.onefivefour.echolist.core.notifications.domain.ReminderBackgroundScheduler
import net.onefivefour.echolist.core.notifications.data.PermissionResultBridge
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionRequester
import org.koin.dsl.module

actual val notificationModule = module {
    includes(reminderModule)
    single<ReminderStore> {
        SqlReminderStore(
            NotificationDatabase(
                AndroidSqliteDriver(
                    NotificationDatabase.Schema.synchronous(),
                    get(),
                    "echolist-notifications.db"
                )
            )
        )
    }
    single<ReminderPlatform> { AndroidReminderPlatform(get()) }
    single<ReminderBackgroundScheduler> { AndroidReminderBackgroundScheduler(get()) }
    single<NotificationPermissionChecker> { AndroidNotificationPermissionChecker(context = get()) }
    single { PermissionResultBridge() }
    single<NotificationPermissionRequester> {
        AndroidNotificationPermissionRequester(
            context = get(),
            bridge = get()
        )
    }
}