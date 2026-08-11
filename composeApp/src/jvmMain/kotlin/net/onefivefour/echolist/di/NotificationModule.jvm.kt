package net.onefivefour.echolist.di

import net.onefivefour.echolist.data.notification.JvmNotificationPermissionChecker
import net.onefivefour.echolist.data.notification.JvmNotificationPermissionRequester
import net.onefivefour.echolist.data.notification.JvmNotificationScheduler
import net.onefivefour.echolist.domain.NotificationPermissionChecker
import net.onefivefour.echolist.domain.NotificationPermissionRequester
import net.onefivefour.echolist.domain.NotificationScheduler
import org.koin.dsl.module

val notificationModule = module {
    single<NotificationScheduler> { JvmNotificationScheduler() }
    single<NotificationPermissionChecker> { JvmNotificationPermissionChecker() }
    single<NotificationPermissionRequester> { JvmNotificationPermissionRequester() }
}
