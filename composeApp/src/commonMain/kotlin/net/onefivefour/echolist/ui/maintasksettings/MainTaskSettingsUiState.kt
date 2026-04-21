package net.onefivefour.echolist.ui.maintasksettings

import net.onefivefour.echolist.ui.recurrence.RecurrenceState

data class MainTaskSettingsUiState(
    val selectedDueDate: String,
    val recurrenceState: RecurrenceState,
    val initialDateMillis: Long?
)
