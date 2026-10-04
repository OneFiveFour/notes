package net.onefivefour.echolist.core.tasks.domain

import net.onefivefour.echolist.core.tasks.domain.model.TaskSettingsChanges

fun interface TaskSettingsResultSink {
    suspend fun emit(changes: TaskSettingsChanges)
}