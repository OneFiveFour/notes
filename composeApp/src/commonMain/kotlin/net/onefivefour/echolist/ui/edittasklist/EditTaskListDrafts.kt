package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import net.onefivefour.echolist.domain.model.MainTask
import net.onefivefour.echolist.domain.model.SubTask

class MainTaskDraft(
    val id: Long,
    description: String = "",
    done: Boolean = false,
    dueDate: String = "",
    recurrence: String = "",
    subTasks: List<SubTaskDraft> = emptyList()
) {
    val descriptionState = TextFieldState(initialText = description)
    var done by mutableStateOf(done)
    val dueDateState = TextFieldState(initialText = dueDate)
    val recurrenceState = TextFieldState(initialText = recurrence)
    val subTasks = mutableStateListOf<SubTaskDraft>().apply {
        addAll(subTasks)
    }

    fun toDomain(): MainTask? {
        val trimmedDescription = descriptionState.text.toString().trim()
        if (trimmedDescription.isBlank()) return null

        val normalizedRecurrence = recurrenceState.text.toString().singleLine()
        val normalizedDueDate = dueDateState.text.toString().trim()

        return MainTask(
            description = trimmedDescription,
            done = done,
            dueDate = if (normalizedRecurrence.isNotBlank()) "" else normalizedDueDate,
            recurrence = normalizedRecurrence,
            subTasks = subTasks.mapNotNull { it.toDomain() }
        )
    }

    companion object {
        fun fromDomain(id: Long, domain: MainTask): MainTaskDraft = MainTaskDraft(
            id = id,
            description = domain.description,
            done = domain.done,
            dueDate = if (domain.recurrence.isNotBlank()) "" else domain.dueDate,
            recurrence = domain.recurrence.singleLine(),
            subTasks = domain.subTasks.mapIndexed { index, subTask ->
                SubTaskDraft.fromDomain(id = id * 1000L + index.toLong(), domain = subTask)
            }
        )
    }
}

class SubTaskDraft(
    val id: Long,
    description: String = "",
    done: Boolean = false
) {
    val descriptionState = TextFieldState(initialText = description)
    var done by mutableStateOf(done)

    fun toDomain(): SubTask? {
        val trimmedDescription = descriptionState.text.toString().trim()
        if (trimmedDescription.isBlank()) return null

        return SubTask(
            description = trimmedDescription,
            done = done
        )
    }

    companion object {
        fun fromDomain(id: Long, domain: SubTask): SubTaskDraft = SubTaskDraft(
            id = id,
            description = domain.description,
            done = domain.done
        )
    }
}

internal fun String.singleLine(): String = trim().replace("\r", "").replace("\n", "")
