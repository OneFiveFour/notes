package net.onefivefour.echolist.core.notifications.di

import net.onefivefour.echolist.core.notifications.data.PersistentNotificationScheduler
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.notifications.domain.NotificationScheduler
import org.koin.dsl.module

internal val reminderModule = module {
    single { NotificationCoordinator(get(), get(), get(), get(), get()) }
    single<NotificationScheduler> { PersistentNotificationScheduler(get()) }
}