package net.onefivefour.echolist.di

import net.onefivefour.echolist.data.notification.IosNotificationPermissionChecker
import net.onefivefour.echolist.data.notification.IosNotificationPermissionRequester
import net.onefivefour.echolist.data.notification.IosNotificationScheduler
import net.onefivefour.echolist.domain.NotificationPermissionChecker
import net.onefivefour.echolist.domain.NotificationPermissionRequester
import net.onefivefour.echolist.domain.NotificationScheduler
import org.koin.dsl.module

val notificationModule = module {
    single<NotificationScheduler> { IosNotificationScheduler() }
    single<NotificationPermissionChecker> { IosNotificationPermissionChecker() }
    single<NotificationPermissionRequester> { IosNotificationPermissionRequester() }
}
