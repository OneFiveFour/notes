package net.onefivefour.echolist.ui.maintasksettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.domain.repository.TaskListRepository
import net.onefivefour.echolist.ui.recurrence.RecurrenceInterval
import net.onefivefour.echolist.ui.recurrence.RecurrenceState
import net.onefivefour.echolist.ui.recurrence.hasValidDetails

internal class MainTaskSettingsViewModel(
    private val mainTaskId: String,
    taskListRepository: TaskListRepository,
    private val resultBus: MainTaskSettingsResultBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MainTaskSettingsUiState(
            selectedDueDate = "",
            recurrenceState = RecurrenceState.Off,
            initialDateMillis = null
        )
    )
    val uiState: StateFlow<MainTaskSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            taskListRepository.getMainTask(mainTaskId).fold(
                onSuccess = { task ->
                    _uiState.update {
                        MainTaskSettingsUiState(
                            selectedDueDate = task.dueDate,
                            recurrenceState = rruleToRecurrenceState(task.recurrence),
                            initialDateMillis = dueDateToUtcMillis(task.dueDate)
                        )
                    }
                },
                onFailure = { /* leave defaults */ }
            )
        }
    }

    fun onDateSelected(dateMillis: Long) {
        val dueDate = utcMillisToDueDate(dateMillis)
        _uiState.update { state ->
            state.copy(
                selectedDueDate = dueDate,
                recurrenceState = RecurrenceState.Off,
                showRecurrenceValidationErrors = false
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
                recurrenceState = newRecurrenceState,
                showRecurrenceValidationErrors = false
            )
        }
    }

    fun onRecurrenceDetailChanged(state: RecurrenceState) {
        _uiState.update { it.copy(recurrenceState = state) }
    }

    fun onConfirm(): Boolean {
        val currentState = _uiState.value
        if (!currentState.recurrenceState.hasValidDetails()) {
            _uiState.update { it.copy(showRecurrenceValidationErrors = true) }
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
