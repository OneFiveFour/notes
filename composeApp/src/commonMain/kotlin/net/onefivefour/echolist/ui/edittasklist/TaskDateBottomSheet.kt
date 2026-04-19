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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import net.onefivefour.echolist.data.source.cache.currentEpochMillis
import net.onefivefour.echolist.domain.model.MainTask
import net.onefivefour.echolist.ui.common.GradientBackground
import net.onefivefour.echolist.ui.theme.EchoListTheme
import kotlin.time.Instant

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
                onTabSelected = {},
                onDueDateSelected = {}
            )
        }
    }
}
