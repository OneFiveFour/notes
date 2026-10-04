package net.onefivefour.echolist.feature.tasksettings.di

import net.onefivefour.echolist.feature.tasksettings.ui.MainTaskSettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val taskSettingsModule = module {
    viewModel { params ->
        MainTaskSettingsViewModel(
            mainTaskId = params.get(), currentDueDate = params.get(), currentRecurrence = params.get(),
            currentIsNotificationEnabled = params.get(), permissionChecker = get(), permissionRequester = get(), resultBus = params.get()
        )
    }
}
