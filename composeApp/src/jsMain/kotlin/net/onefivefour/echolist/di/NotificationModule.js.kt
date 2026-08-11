package net.onefivefour.echolist.di

import net.onefivefour.echolist.data.notification.JsNotificationPermissionChecker
import net.onefivefour.echolist.data.notification.JsNotificationPermissionRequester
import net.onefivefour.echolist.data.notification.JsNotificationScheduler
import net.onefivefour.echolist.domain.NotificationPermissionChecker
import net.onefivefour.echolist.domain.NotificationPermissionRequester
import net.onefivefour.echolist.domain.NotificationScheduler
import org.koin.dsl.module

val notificationModule = module {
    single<NotificationScheduler> { JsNotificationScheduler() }
    single<NotificationPermissionChecker> { JsNotificationPermissionChecker() }
    single<NotificationPermissionRequester> { JsNotificationPermissionRequester() }
}
