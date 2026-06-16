package net.onefivefour.echolist.ui.maintasksettings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import net.onefivefour.echolist.ui.recurrence.DailyDetailContent
import net.onefivefour.echolist.ui.recurrence.MonthlyDetailContent
import net.onefivefour.echolist.ui.recurrence.RecurrenceInterval
import net.onefivefour.echolist.ui.recurrence.RecurrenceIntervalPicker
import net.onefivefour.echolist.ui.recurrence.RecurrenceState
import net.onefivefour.echolist.ui.recurrence.WeeklyDetailContent
import net.onefivefour.echolist.ui.theme.EchoListTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainTaskSettingsScreen(
    uiState: MainTaskSettingsUiState,
    onDateSelected: (Long) -> Unit,
    onRecurrenceIntervalSelected: (RecurrenceInterval) -> Unit,
    onRecurrenceDetailChanged: (RecurrenceState) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.initialDateMillis
    )

    LaunchedEffect(datePickerState) {
        snapshotFlow { datePickerState.selectedDateMillis }
            .drop(1)
            .filterNotNull()
            .collect { selectedMillis ->
                onDateSelected(selectedMillis)
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Task Settings",
                        style = EchoListTheme.typography.labelLarge,
                        color = EchoListTheme.materialColors.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = EchoListTheme.materialColors.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onConfirm) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Confirm",
                            tint = EchoListTheme.materialColors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EchoListTheme.materialColors.surface
                )
            )
        },
        containerColor = EchoListTheme.materialColors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(EchoListTheme.dimensions.l),
            verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.l)
        ) {
            SettingsSection(title = "Due date") {
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.fillMaxWidth(),
                    title = null,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = Color.Transparent
                    )
                )
            }

            SettingsSection(title = "Repeat") {
                RecurrenceIntervalPicker(
                    selectedInterval = uiState.recurrenceState.interval,
                    onIntervalSelected = onRecurrenceIntervalSelected
                )

                RecurrenceDetail(
                    recurrenceState = uiState.recurrenceState,
                    onRecurrenceDetailChanged = onRecurrenceDetailChanged
                )
            }
        }
    }
}

@Composable
private fun RecurrenceDetail(
    recurrenceState: RecurrenceState,
    onRecurrenceDetailChanged: (RecurrenceState) -> Unit
) {
    val hasDetail = recurrenceState is RecurrenceState.Daily ||
        recurrenceState is RecurrenceState.Weekly ||
        recurrenceState is RecurrenceState.Monthly

    AnimatedVisibility(visible = hasDetail) {
        Column(
            modifier = Modifier.padding(top = EchoListTheme.dimensions.l)
        ) {
            when (val state = recurrenceState) {
                is RecurrenceState.Off -> Unit
                is RecurrenceState.Yearly -> Unit
                is RecurrenceState.Daily -> {
                    DailyDetailContent(
                        selectedDays = state.selectedDays,
                        onDayToggled = { day, checked ->
                            val updatedDays = if (checked) {
                                state.selectedDays + day
                            } else {
                                state.selectedDays - day
                            }
                            onRecurrenceDetailChanged(state.copy(selectedDays = updatedDays))
                        }
                    )
                }
                is RecurrenceState.Weekly -> {
                    WeeklyDetailContent(
                        everyNWeeks = state.everyNWeeks,
                        onWeekCountChanged = { newCount ->
                            onRecurrenceDetailChanged(state.copy(everyNWeeks = newCount))
                        }
                    )
                }
                is RecurrenceState.Monthly -> {
                    MonthlyDetailContent(
                        everyNMonths = state.everyNMonths,
                        dayOfMonth = state.dayOfMonth,
                        onMonthIntervalChanged = { newInterval ->
                            onRecurrenceDetailChanged(state.copy(everyNMonths = newInterval))
                        },
                        onDayOfMonthChanged = { newDay ->
                            onRecurrenceDetailChanged(state.copy(dayOfMonth = newDay))
                        }
                    )
                }
            }
        }
    }
}
