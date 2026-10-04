package net.onefivefour.echolist.feature.login.di

import net.onefivefour.echolist.feature.login.ui.LoginViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val loginModule = module {
    viewModel { LoginViewModel(authRepository = get()) }
}
