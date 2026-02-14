package net.onefivefour.notes.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

expect fun platformModule(): Module

fun initKoin(appModule: Module = Module()): KoinApplication {
    return startKoin {
        modules(appModule, platformModule())
        modules(appModules)
    }
}
