package net.onefivefour.echolist.di

import net.onefivefour.echolist.core.notifications.di.notificationModule

import net.onefivefour.echolist.core.database.di.databaseModule

import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    includes(databaseModule, notificationModule)
}
