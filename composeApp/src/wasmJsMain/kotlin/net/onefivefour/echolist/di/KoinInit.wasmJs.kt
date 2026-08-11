package net.onefivefour.echolist.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    includes(databaseModule, notificationModule)
}