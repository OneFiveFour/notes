package net.onefivefour.echolist.di

import net.onefivefour.echolist.data.notification.WasmJsNotificationPermissionChecker
import net.onefivefour.echolist.data.notification.WasmJsNotificationPermissionRequester
import net.onefivefour.echolist.data.notification.WasmJsNotificationScheduler
import net.onefivefour.echolist.domain.NotificationPermissionChecker
import net.onefivefour.echolist.domain.NotificationPermissionRequester
import net.onefivefour.echolist.domain.NotificationScheduler
import org.koin.dsl.module

val notificationModule = module {
    single<NotificationScheduler> { WasmJsNotificationScheduler() }
    single<NotificationPermissionChecker> { WasmJsNotificationPermissionChecker() }
    single<NotificationPermissionRequester> { WasmJsNotificationPermissionRequester() }
}
