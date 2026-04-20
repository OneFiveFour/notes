package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import net.onefivefour.echolist.data.source.cache.currentEpochMillis
import net.onefivefour.echolist.domain.model.MainTask
import net.onefivefour.echolist.ui.common.GradientBackground
import net.onefivefour.echolist.ui.theme.EchoListTheme
import kotlin.time.Instant

internal data class TaskDateSheetState(
    val mainTaskId: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TaskDateBottomSheet(
    sheetState: TaskDateSheetState,
    mainTask: UiMainTask,
    onDismissRequest: () -> Unit,
    onDueDateSelected: (String) -> Unit,
    onRecurrenceChanged: (RecurrenceState) -> Unit
) {
    val currentDueDate = mainTask.dueDateState.text.toString()
    val initialSelectedDateMillis = remember(currentDueDate) {
        dueDateToUtcMillis(currentDueDate)
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis,
        initialDisplayedMonthMillis = initialSelectedDateMillis ?: currentEpochMillis()
    )
    var lastHandledSelection by remember(sheetState.mainTaskId, currentDueDate) {
        mutableLongStateOf(initialSelectedDateMillis ?: Long.MIN_VALUE)
    }

    var recurrenceState by remember { mutableStateOf<RecurrenceState>(RecurrenceState.Off) }

    LaunchedEffect(recurrenceState) {
        onRecurrenceChanged(recurrenceState)
    }

    LaunchedEffect(sheetState.mainTaskId, currentDueDate) {
        lastHandledSelection = initialSelectedDateMillis ?: Long.MIN_VALUE
    }

    LaunchedEffect(sheetState.mainTaskId, datePickerState) {
        snapshotFlow { datePickerState.selectedDateMillis }
            .drop(1)
            .filterNotNull()
            .collect { selectedMillis ->
                if (selectedMillis == lastHandledSelection) return@collect

                lastHandledSelection = selectedMillis
                onDueDateSelected(utcMillisToDueDate(selectedMillis))
            }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {

        DatePicker(
            state = datePickerState,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(EchoListTheme.dimensions.m))

        RecurrenceIntervalPicker(
            selectedInterval = recurrenceState.interval,
            onIntervalSelected = { newInterval ->
                recurrenceState = when (newInterval) {
                    RecurrenceInterval.Off -> RecurrenceState.Off
                    RecurrenceInterval.Daily -> RecurrenceState.Daily()
                    RecurrenceInterval.Weekly -> RecurrenceState.Weekly()
                    RecurrenceInterval.Monthly -> {
                        val selectedMillis = datePickerState.selectedDateMillis
                        val defaultDay = if (selectedMillis != null) {
                            utcMillisToDueDate(selectedMillis)
                                .let { LocalDate.parse(it).day }
                        } else {
                            1
                        }
                        RecurrenceState.Monthly(dayOfMonth = defaultDay)
                    }
                    RecurrenceInterval.Yearly -> RecurrenceState.Yearly
                }
            }
        )

        Spacer(modifier = Modifier.height(EchoListTheme.dimensions.m))

        when (val state = recurrenceState) {
            is RecurrenceState.Off -> { /* no detail content */ }
            is RecurrenceState.Daily -> {
                DailyDetailContent(
                    selectedDays = state.selectedDays,
                    onDayToggled = { day, checked ->
                        val updatedDays = if (checked) {
                            state.selectedDays + day
                        } else {
                            state.selectedDays - day
                        }
                        recurrenceState = state.copy(selectedDays = updatedDays)
                    }
                )
            }
            is RecurrenceState.Weekly -> {
                WeeklyDetailContent(
                    everyNWeeks = state.everyNWeeks,
                    onWeekCountChanged = { newCount ->
                        recurrenceState = state.copy(everyNWeeks = newCount)
                    }
                )
            }
            is RecurrenceState.Monthly -> {
                MonthlyDetailContent(
                    everyNMonths = state.everyNMonths,
                    dayOfMonth = state.dayOfMonth,
                    onMonthIntervalChanged = { newInterval ->
                        recurrenceState = state.copy(everyNMonths = newInterval)
                    },
                    onDayOfMonthChanged = { newDay ->
                        recurrenceState = state.copy(dayOfMonth = newDay)
                    }
                )
            }
            is RecurrenceState.Yearly -> { /* no detail content */ }
        }
    }
}

internal fun dueDateToUtcMillis(dueDate: String): Long? = runCatching {
    LocalDate.parse(dueDate)
        .atStartOfDayIn(TimeZone.UTC)
        .toEpochMilliseconds()
}.getOrNull()

internal fun utcMillisToDueDate(utcMillis: Long): String =
    Instant.fromEpochMilliseconds(utcMillis)
        .toLocalDateTime(TimeZone.UTC)
        .date
        .toString()

@Preview
@Composable
private fun TaskDateBottomSheetPreview() {
    val task = remember {
        UiMainTask.fromDomain(
            id = 1L,
            domain = MainTask(
                description = "Plan launch",
                isDone = false,
                dueDate = "2026-04-01",
                recurrence = "",
                subTasks = emptyList()
            )
        )
    }

    EchoListTheme {
        GradientBackground {
            TaskDateBottomSheet(
                sheetState = TaskDateSheetState(mainTaskId = task.id),
                mainTask = task,
                onDismissRequest = {},
                onDueDateSelected = {},
                onRecurrenceChanged = {}
            )
        }
    }
}
