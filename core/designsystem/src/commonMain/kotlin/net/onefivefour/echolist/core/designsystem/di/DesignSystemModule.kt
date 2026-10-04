package net.onefivefour.echolist.core.designsystem.di

import net.onefivefour.echolist.core.designsystem.ui.theme.ThemeManager
import net.onefivefour.echolist.core.designsystem.ui.theme.colorscheme.EchoListClassicTheme
import net.onefivefour.echolist.core.designsystem.ui.theme.colorscheme.EchoListTheme2
import org.koin.core.module.Module
import org.koin.dsl.module

val designSystemModule: Module = module {
    single {
        ThemeManager(
            availableThemes = listOf(
                EchoListClassicTheme,
                EchoListTheme2
            ),
            initialTheme = EchoListClassicTheme
        )
    }
}