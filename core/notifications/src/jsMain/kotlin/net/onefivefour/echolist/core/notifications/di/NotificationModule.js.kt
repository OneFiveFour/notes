package net.onefivefour.echolist.core.notifications.di

import net.onefivefour.echolist.core.notifications.data.JsNotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.data.JsNotificationPermissionRequester
import net.onefivefour.echolist.core.notifications.data.JsNotificationScheduler
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionRequester
import net.onefivefour.echolist.core.notifications.domain.NotificationScheduler
import org.koin.dsl.module

actual val notificationModule = module {
    single<NotificationScheduler> { JsNotificationScheduler() }
    single<NotificationPermissionChecker> { JsNotificationPermissionChecker() }
    single<NotificationPermissionRequester> { JsNotificationPermissionRequester() }
}