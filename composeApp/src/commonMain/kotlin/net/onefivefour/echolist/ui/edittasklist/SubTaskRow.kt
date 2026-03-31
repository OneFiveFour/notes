package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.ic_delete
import net.onefivefour.echolist.ui.common.ElTextField
import net.onefivefour.echolist.ui.common.GradientBackground
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun SubTaskRow(
    subTask: UiSubTask,
    shouldRequestFocus: Boolean,
    onFocusHandled: () -> Unit,
    onKeyboardAction: () -> Unit,
    onRemoveSubTask: () -> Unit
) {
    val focusRequester = remember(subTask.subTaskId) { FocusRequester() }

    LaunchedEffect(shouldRequestFocus) {
        if (shouldRequestFocus) {
            focusRequester.requestFocus()
            onFocusHandled()
        }
    }

    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(horizontal = EchoListTheme.dimensions.m),
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides EchoListTheme.dimensions.xxl) {
            Checkbox(
                checked = subTask.isDone,
                onCheckedChange = { isChecked -> subTask.isDone = isChecked }
            )
        }

        ElTextField(
            state = subTask.descriptionState,
            modifier = Modifier.Companion.weight(1f),
            singleLine = true,
            imeAction = ImeAction.Companion.Next,
            onKeyboardAction = onKeyboardAction,
            focusRequester = focusRequester
        )

        Icon(
            painter = painterResource(Res.drawable.ic_delete),
            contentDescription = "Delete subtask",
            modifier = Modifier.Companion.clickable { onRemoveSubTask() }
        )
    }
}


@Preview
@Composable
private fun SubTaskRowPreview() {
    val subTask = remember {
        UiSubTask(subTaskId = 1, description = "Review copy", isDone = false)
    }
    EchoListTheme {
        GradientBackground {
            SubTaskRow(
                subTask = subTask,
                shouldRequestFocus = false,
                onFocusHandled = {},
                onKeyboardAction = {},
                onRemoveSubTask = {}
            )
        }
    }
}

@Preview
@Composable
private fun SubTaskRowDonePreview() {
    val subTask = remember {
        UiSubTask(subTaskId = 2, description = "Draft checklist", isDone = true)
    }
    EchoListTheme {
        GradientBackground {
            SubTaskRow(
                subTask = subTask,
                shouldRequestFocus = false,
                onFocusHandled = {},
                onKeyboardAction = {},
                onRemoveSubTask = {}
            )
        }
    }
}