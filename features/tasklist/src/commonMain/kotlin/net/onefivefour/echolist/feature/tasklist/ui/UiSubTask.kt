package net.onefivefour.echolist.feature.tasklist.ui

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import net.onefivefour.echolist.core.tasks.domain.model.SubTask

internal class UiSubTask(
    val id: String,
    description: String = "",
    isDone: Boolean = false
) {
    val descriptionState = TextFieldState(initialText = description)
    var isDone by mutableStateOf(isDone)

    fun toDomain(): SubTask? {
        val trimmedDescription = descriptionState.text.toString().trim()
        if (trimmedDescription.isBlank()) return null

        return SubTask(
            id = id,
            description = trimmedDescription,
            isDone = isDone
        )
    }

    companion object {
        fun fromDomain(domain: SubTask): UiSubTask = UiSubTask(
            id = domain.id,
            description = domain.description,
            isDone = domain.isDone
        )
    }
}
