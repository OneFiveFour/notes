package net.onefivefour.echolist.di

import net.onefivefour.echolist.core.database.di.databaseModule

import net.onefivefour.echolist.data.source.AndroidSecureStorage
import net.onefivefour.echolist.data.source.SecureStorage
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    includes(databaseModule, notificationModule)
    single<SecureStorage> { AndroidSecureStorage(context = get()) }
}