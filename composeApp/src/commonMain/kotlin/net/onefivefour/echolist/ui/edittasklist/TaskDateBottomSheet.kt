package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import net.onefivefour.echolist.data.source.cache.currentEpochMillis
import net.onefivefour.echolist.domain.model.MainTask
import net.onefivefour.echolist.ui.common.GradientBackground
import net.onefivefour.echolist.ui.theme.EchoListTheme

internal enum class TaskDateSheetTab(val label: String) {
    DueDate("Due date"),
    Recurring("Recurring")
}

internal data class TaskDateSheetState(
    val mainTaskId: Long,
    val selectedTab: TaskDateSheetTab = TaskDateSheetTab.DueDate
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TaskDateBottomSheet(
    sheetState: TaskDateSheetState,
    mainTask: UiMainTask,
    onDismissRequest: () -> Unit,
    onTabSelected: (TaskDateSheetTab) -> Unit,
    onDueDateSelected: (String) -> Unit
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
        PrimaryTabRow(selectedTabIndex = sheetState.selectedTab.ordinal) {
            TaskDateSheetTab.entries.forEach { tab ->
                Tab(
                    selected = tab == sheetState.selectedTab,
                    onClick = { onTabSelected(tab) },
                    text = { Text(text = tab.label) }
                )
            }
        }

        when (sheetState.selectedTab) {
            TaskDateSheetTab.DueDate -> DatePicker(
                state = datePickerState,
                modifier = Modifier.fillMaxWidth()
            )

            TaskDateSheetTab.Recurring -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(EchoListTheme.dimensions.xxxl * 2)
                    .padding(horizontal = EchoListTheme.dimensions.m)
            )
        }
    }
}

private const val MillisPerDay = 86_400_000L

internal fun dueDateToUtcMillis(dueDate: String): Long? {
    val parts = dueDate.split('-')
    if (parts.size != 3) return null

    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null

    if (month !in 1..12 || day !in 1..31) return null

    val epochDay = civilToEpochDay(year = year, month = month, day = day)
    return utcMillisToDueDate(epochDay * MillisPerDay)
        .takeIf { it == dueDate }
        ?.let { epochDay * MillisPerDay }
}

internal fun utcMillisToDueDate(utcMillis: Long): String {
    val date = epochDayToCivil(floorDiv(utcMillis, MillisPerDay))
    return buildString(10) {
        append(date.year.toString().padStart(4, '0'))
        append('-')
        append(date.month.toString().padStart(2, '0'))
        append('-')
        append(date.day.toString().padStart(2, '0'))
    }
}

private fun floorDiv(dividend: Long, divisor: Long): Long =
    if (dividend >= 0L) {
        dividend / divisor
    } else {
        ((dividend + 1L) / divisor) - 1L
    }

private fun civilToEpochDay(year: Int, month: Int, day: Int): Long {
    val adjustedYear = year - if (month <= 2) 1 else 0
    val era = floorDiv(adjustedYear.toLong(), 400L)
    val yearOfEra = adjustedYear - era.toInt() * 400
    val monthIndex = month + if (month > 2) -3 else 9
    val dayOfYear = (153 * monthIndex + 2) / 5 + day - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear

    return era * 146097L + dayOfEra - 719468L
}

private fun epochDayToCivil(epochDay: Long): CivilDate {
    val shifted = epochDay + 719468L
    val era = floorDiv(shifted, 146097L)
    val dayOfEra = (shifted - era * 146097L).toInt()
    val yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36524 - dayOfEra / 146096) / 365
    var year = yearOfEra + era.toInt() * 400
    val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val monthParam = (5 * dayOfYear + 2) / 153
    val day = dayOfYear - (153 * monthParam + 2) / 5 + 1
    val month = monthParam + if (monthParam < 10) 3 else -9
    year += if (month <= 2) 1 else 0

    return CivilDate(year = year, month = month, day = day)
}

private data class CivilDate(
    val year: Int,
    val month: Int,
    val day: Int
)

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
                onTabSelected = {},
                onDueDateSelected = {}
            )
        }
    }
}
