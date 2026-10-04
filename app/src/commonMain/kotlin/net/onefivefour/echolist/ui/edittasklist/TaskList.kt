package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import net.onefivefour.echolist.ui.theme.EchoListTheme

/**
 * Time to wait for the keyboard open animation and subsequent layout
 * repass before calculating the second scroll adjustment.
 */
private const val KEYBOARD_SETTLE_DELAY_MS = 400L

@Composable
internal fun TaskList(
    mainTasks: List<UiMainTask>,
    focusedMainTaskId: String?,
    isAutoDelete: Boolean,
    onRemoveMainTask: (Int) -> Unit,
    onMainTaskCheckedChange: (Int, Boolean) -> Unit,
    onAddSubTask: (String) -> Unit,
    onSubTaskCheckedChange: (Int, Int, Boolean) -> Unit,
    onFieldFocusLost: () -> Unit,
    onMainTaskDescriptionFocusChanged: (String, Boolean) -> Unit,
    onNavigateToSettings: (String, String, String, Boolean) -> Unit,
    focusTarget: FocusTarget?,
    onFocusHandled: () -> Unit,
    onMainTaskKeyboardAction: (String) -> Unit,
    onSubTaskKeyboardAction: (String, String) -> Unit
) {
    val mainTaskToFocus = focusTarget as? FocusTarget.MainTask
    val subTaskToFocus = focusTarget as? FocusTarget.SubTask
    val listState = rememberLazyListState()

    // Phase 1: Scroll the target item into the visible area so that its
    // composable is created by LazyColumn. Off-screen items are not composed,
    // so their focus-requesting LaunchedEffect would never fire otherwise.
    LaunchedEffect(mainTaskToFocus) {
        val targetId = mainTaskToFocus?.mainTaskId ?: return@LaunchedEffect
        val index = mainTasks.indexOfFirst { it.id == targetId }
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    // Phase 2: Once focus is set the card expands (subtask button appears) and
    // the keyboard animates open, shrinking the viewport via imePadding().
    // After everything settles, nudge the scroll so the full card is visible.
    LaunchedEffect(focusedMainTaskId) {
        val id = focusedMainTaskId ?: return@LaunchedEffect
        val index = mainTasks.indexOfFirst { it.id == id }
        if (index < 0) return@LaunchedEffect

        delay(KEYBOARD_SETTLE_DELAY_MS)

        val itemInfo = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == index } ?: return@LaunchedEffect
        val viewportEnd = listState.layoutInfo.viewportEndOffset
        val itemEnd = itemInfo.offset + itemInfo.size
        val overflow = itemEnd - viewportEnd
        if (overflow > 0) {
            listState.animateScrollBy(overflow.toFloat())
        }
    }

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.m),
        contentPadding = PaddingValues(bottom = EchoListTheme.dimensions.s)
    ) {
        itemsIndexed(mainTasks, key = { _, mainTask -> mainTask.id }) { mainTaskIndex, mainTask ->
            MainTaskCard(
                mainTask = mainTask,
                isAutoDelete = isAutoDelete,
                onRemoveMainTask = { onRemoveMainTask(mainTaskIndex) },
                onMainTaskCheckedChange = { isChecked ->
                    onMainTaskCheckedChange(mainTaskIndex, isChecked)
                },
                onSubTaskCheckedChange = { subTaskIndex, isChecked ->
                    onSubTaskCheckedChange(mainTaskIndex, subTaskIndex, isChecked)
                },
                onFieldFocusLost = onFieldFocusLost,
                onMainTaskDescriptionFocusChanged = { isFocused ->
                    onMainTaskDescriptionFocusChanged(mainTask.id, isFocused)
                },
                onNavigateToSettings = {
                    onNavigateToSettings(
                        mainTask.id,
                        mainTask.dueDateState.text.toString(),
                        mainTask.recurrenceState.text.toString(),
                        mainTask.isNotificationEnabled
                    )
                },
                onMainTaskKeyboardAction = onMainTaskKeyboardAction,
                shouldFocusMainTask = mainTaskToFocus?.mainTaskId == mainTask.id,
                onMainTaskFocusHandled = onFocusHandled,
                showAddFirstSubTask = mainTask.subTasks.isEmpty() &&
                    focusedMainTaskId == mainTask.id,
                onAddFirstSubTask = { onAddSubTask(mainTask.id) },
                focusedSubTaskId = subTaskToFocus?.id?.takeIf { subTaskToFocus.mainTaskId == mainTask.id },
                onSubTaskFocusHandled = onFocusHandled,
                onSubTaskKeyboardAction = { subTaskId ->
                    onSubTaskKeyboardAction(mainTask.id, subTaskId)
                }
            )
        }
    }
}
