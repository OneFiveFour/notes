package net.onefivefour.echolist.feature.tasklist.ui

import net.onefivefour.echolist.core.tasks.domain.model.TaskSettingsChanges

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Concrete DI type for settings results so Koin never confuses this flow with
 * other generic MutableSharedFlow registrations after JVM type erasure.
 */
internal class MainTaskSettingsResultBus(
    private val resultFlow: MutableSharedFlow<TaskSettingsChanges> = MutableSharedFlow()
) : net.onefivefour.echolist.core.tasks.domain.TaskSettingsResultSink {

    val results: SharedFlow<TaskSettingsChanges> = resultFlow.asSharedFlow()

    override suspend fun emit(result: TaskSettingsChanges) {
        resultFlow.emit(result)
    }
}