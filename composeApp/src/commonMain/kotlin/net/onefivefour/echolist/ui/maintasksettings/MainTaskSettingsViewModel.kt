package net.onefivefour.echolist.ui.maintasksettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.ui.recurrence.RecurrenceInterval
import net.onefivefour.echolist.ui.recurrence.RecurrenceState
import net.onefivefour.echolist.ui.recurrence.hasValidDetails

internal class MainTaskSettingsViewModel(
    private val mainTaskId: String,
    currentDueDate: String,
    currentRecurrence: String,
    private val resultBus: MainTaskSettingsResultBus
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainTaskSettingsUiState>(
        MainTaskSettingsUiState.Ready(
            selectedDueDate = currentDueDate,
            recurrenceState = rruleToRecurrenceState(currentRecurrence),
            initialDateMillis = dueDateToUtcMillis(currentDueDate)
        )
    )
    val uiState: StateFlow<MainTaskSettingsUiState> = _uiState.asStateFlow()

    fun onDateSelected(dateMillis: Long) {
        val dueDate = utcMillisToDueDate(dateMillis)
        updateReady { state ->
            state.copy(
                selectedDueDate = dueDate,
                initialDateMillis = dateMillis,
                showRecurrenceValidationErrors = false,
                showDueDateRequiredError = false
            )
        }
        confirm()
    }

    fun onRecurrenceIntervalSelected(interval: RecurrenceInterval) {
        val newRecurrenceState = when (interval) {
            RecurrenceInterval.Off -> RecurrenceState.Off
            RecurrenceInterval.Daily -> RecurrenceState.Daily()
            RecurrenceInterval.Weekly -> RecurrenceState.Weekly()
            RecurrenceInterval.Monthly -> RecurrenceState.Monthly()
            RecurrenceInterval.Yearly -> RecurrenceState.Yearly
        }

        updateReady { state ->
            state.copy(
                recurrenceState = newRecurrenceState,
                showRecurrenceValidationErrors = false
            )
        }
        confirm()
    }

    fun onRecurrenceDetailChanged(state: RecurrenceState) {
        updateReady { it.copy(recurrenceState = state) }
        confirm()
    }

    private fun updateReady(transform: (MainTaskSettingsUiState.Ready) -> MainTaskSettingsUiState.Ready) {
        _uiState.update { current ->
            when (current) {
                is MainTaskSettingsUiState.Ready -> transform(current)
                is MainTaskSettingsUiState.Loading -> current
            }
        }
    }

    private fun confirm(): Boolean {
        val currentState = _uiState.value as? MainTaskSettingsUiState.Ready ?: return false

        if (currentState.recurrenceState != RecurrenceState.Off && currentState.selectedDueDate.isBlank()) {
            updateReady { it.copy(showDueDateRequiredError = true) }
            return false
        }

        if (!currentState.recurrenceState.hasValidDetails()) {
            updateReady { it.copy(showRecurrenceValidationErrors = true) }
            return false
        }

        viewModelScope.launch {
            resultBus.emit(
                MainTaskSettingsResult(
                    mainTaskId = mainTaskId,
                    dueDate = currentState.selectedDueDate,
                    recurrence = currentState.recurrenceState.toRrule()
                )
            )
        }
        return true
    }
}
