package net.onefivefour.echolist.ui.maintasksettings

import net.onefivefour.echolist.ui.recurrence.RecurrenceState

internal sealed interface MainTaskSettingsUiState {
    data object Loading : MainTaskSettingsUiState
    data class Ready(
        val selectedDueDate: String,
        val recurrenceState: RecurrenceState,
        val initialDateMillis: Long?,
        val isNotificationEnabled: Boolean = true,
        val isNotificationToggleEnabled: Boolean = true,
        val showRecurrenceValidationErrors: Boolean = false,
        val showDueDateRequiredError: Boolean = false
    ) : MainTaskSettingsUiState
}
