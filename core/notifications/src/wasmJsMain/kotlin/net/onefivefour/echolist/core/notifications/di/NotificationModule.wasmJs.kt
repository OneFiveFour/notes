package net.onefivefour.echolist.core.notifications.di

import net.onefivefour.echolist.core.notifications.data.WasmJsNotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.data.WasmJsNotificationPermissionRequester
import net.onefivefour.echolist.core.notifications.data.WasmReminderStore
import net.onefivefour.echolist.core.notifications.data.WasmReminderPlatform
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
    single<ReminderStore> { WasmReminderStore() }
    single<ReminderPlatform> { WasmReminderPlatform() }
    single<ReminderBackgroundScheduler> {
        InProcessReminderBackgroundScheduler(
            restore = { get<NotificationCoordinator>().restore() },
            sync = { get<NotificationSyncHandler>().synchronize() }
        )
    }
    single<NotificationPermissionChecker> { WasmJsNotificationPermissionChecker() }
    single<NotificationPermissionRequester> { WasmJsNotificationPermissionRequester() }
}