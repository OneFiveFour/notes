package net.onefivefour.echolist.ui.maintasksettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.ui.recurrence.RecurrenceInterval
import net.onefivefour.echolist.ui.recurrence.RecurrenceState

internal class MainTaskSettingsViewModel(
    private val mainTaskId: Long,
    initialDueDate: String,
    initialRecurrence: String,
    private val resultFlow: MutableSharedFlow<MainTaskSettingsResult>
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MainTaskSettingsUiState(
            selectedDueDate = initialDueDate,
            recurrenceState = rruleToRecurrenceState(initialRecurrence),
            initialDateMillis = dueDateToUtcMillis(initialDueDate)
        )
    )
    val uiState: StateFlow<MainTaskSettingsUiState> = _uiState.asStateFlow()

    fun onDateSelected(dateMillis: Long) {
        val dueDate = utcMillisToDueDate(dateMillis)
        _uiState.update { state ->
            state.copy(
                selectedDueDate = dueDate,
                recurrenceState = RecurrenceState.Off
            )
        }
    }

    fun onRecurrenceIntervalSelected(interval: RecurrenceInterval) {
        val newRecurrenceState = when (interval) {
            RecurrenceInterval.Off -> RecurrenceState.Off
            RecurrenceInterval.Daily -> RecurrenceState.Daily()
            RecurrenceInterval.Weekly -> RecurrenceState.Weekly()
            RecurrenceInterval.Monthly -> RecurrenceState.Monthly()
            RecurrenceInterval.Yearly -> RecurrenceState.Yearly
        }

        _uiState.update { state ->
            state.copy(
                selectedDueDate = if (interval != RecurrenceInterval.Off) "" else state.selectedDueDate,
                recurrenceState = newRecurrenceState
            )
        }
    }

    fun onRecurrenceDetailChanged(state: RecurrenceState) {
        _uiState.update { it.copy(recurrenceState = state) }
    }

    fun onConfirm() {
        viewModelScope.launch {
            val currentState = _uiState.value
            resultFlow.emit(
                MainTaskSettingsResult(
                    mainTaskId = mainTaskId,
                    dueDate = currentState.selectedDueDate,
                    recurrence = currentState.recurrenceState.toRrule()
                )
            )
        }
    }
}
