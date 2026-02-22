package net.onefivefour.echolist.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

expect fun platformModule(): Module

fun initKoin(
    platformConfiguration: KoinApplication.() -> Unit = {},
    appModule: Module = Module()
): KoinApplication {
    return startKoin {
        platformConfiguration()
        modules(appModule, platformModule())
        modules(appModules)
    }
}
