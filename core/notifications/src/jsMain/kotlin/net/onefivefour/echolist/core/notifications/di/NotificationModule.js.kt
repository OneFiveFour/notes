package net.onefivefour.echolist.core.notifications.di

import net.onefivefour.echolist.core.notifications.data.JsNotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.data.JsNotificationPermissionRequester
import net.onefivefour.echolist.core.notifications.data.JsReminderStore
import net.onefivefour.echolist.core.notifications.data.JsReminderPlatform
import net.onefivefour.echolist.core.notifications.data.InProcessReminderBackgroundScheduler
import net.onefivefour.echolist.core.notifications.domain.ReminderStore
import net.onefivefour.echolist.core.notifications.domain.ReminderPlatform
import net.onefivefour.echolist.core.notifications.domain.ReminderBackgroundScheduler
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.notifications.domain.NotificationSyncHandler
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionRequester
import org.koin.dsl.module

actual val notificationModule = module {
    includes(reminderModule)
    single<ReminderStore> { JsReminderStore() }
    single<ReminderPlatform> { JsReminderPlatform() }
    single<ReminderBackgroundScheduler> {
        InProcessReminderBackgroundScheduler(
            restore = { get<NotificationCoordinator>().restore() },
            sync = { get<NotificationSyncHandler>().synchronize() }
        )
    }
    single<NotificationPermissionChecker> { JsNotificationPermissionChecker() }
    single<NotificationPermissionRequester> { JsNotificationPermissionRequester() }
}