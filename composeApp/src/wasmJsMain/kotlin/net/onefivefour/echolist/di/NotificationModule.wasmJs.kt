package net.onefivefour.echolist.di

import net.onefivefour.echolist.data.notification.WasmJsNotificationScheduler
import net.onefivefour.echolist.domain.NotificationScheduler
import org.koin.dsl.module

val notificationModule = module {
    single<NotificationScheduler> { WasmJsNotificationScheduler() }
}
