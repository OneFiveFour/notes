package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import net.onefivefour.echolist.domain.model.SubTask

internal class UiSubTask(
    val subTaskId: Long,
    description: String = "",
    isDone: Boolean = false
) {
    val descriptionState = TextFieldState(initialText = description)
    var isDone by mutableStateOf(isDone)

    fun toDomain(): SubTask? {
        val trimmedDescription = descriptionState.text.toString().trim()
        if (trimmedDescription.isBlank()) return null

        return SubTask(
            description = trimmedDescription,
            isDone = isDone
        )
    }

    companion object {
        fun fromDomain(id: Long, domain: SubTask): UiSubTask = UiSubTask(
            subTaskId = id,
            description = domain.description,
            isDone = domain.isDone
        )
    }
}