package net.onefivefour.echolist.feature.tasklist.di

import net.onefivefour.echolist.feature.tasklist.ui.EditTaskListMode
import net.onefivefour.echolist.feature.tasklist.ui.EditTaskListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val taskListModule = module {
    viewModel { params ->
        EditTaskListViewModel(mode = params.get<EditTaskListMode>(), taskListRepository = get(), settingsResults = params.get(), notificationScheduler = get())
    }
}
