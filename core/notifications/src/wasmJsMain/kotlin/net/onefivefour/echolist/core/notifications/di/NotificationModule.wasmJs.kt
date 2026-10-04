package net.onefivefour.echolist.core.notifications.di

import net.onefivefour.echolist.core.notifications.data.WasmJsNotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.data.WasmJsNotificationPermissionRequester
import net.onefivefour.echolist.core.notifications.data.WasmJsNotificationScheduler
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionRequester
import net.onefivefour.echolist.core.notifications.domain.NotificationScheduler
import org.koin.dsl.module

actual val notificationModule = module {
    single<NotificationScheduler> { WasmJsNotificationScheduler() }
    single<NotificationPermissionChecker> { WasmJsNotificationPermissionChecker() }
    single<NotificationPermissionRequester> { WasmJsNotificationPermissionRequester() }
}
