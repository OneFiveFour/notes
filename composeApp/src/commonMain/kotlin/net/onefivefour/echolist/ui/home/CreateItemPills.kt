package net.onefivefour.echolist.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.ic_plus
import net.onefivefour.echolist.data.models.ItemType
import net.onefivefour.echolist.ui.common.RoundIconButton
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
fun CreateItemPills(
    onCreateNote: (String) -> Unit,
    onTaskCreate: (String) -> Unit,
    onFolderCreate: (String) -> Unit,
    onClosePills: () -> Unit
) {
    val pillsState = remember { mutableStateOf<PillsUiState>(PillsUiState.Idle) }
    val textFieldValue = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedContent(
            targetState = pillsState.value,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                (fadeIn(tween(200)) + expandHorizontally(expandFrom = Alignment.Start))
                    .togetherWith(fadeOut(tween(200)) + shrinkHorizontally(shrinkTowards = Alignment.Start))
            }
        ) { state ->
            when (state) {
                PillsUiState.Idle -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.m),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CreateItemPill(
                            color = EchoListTheme.echoListColorScheme.noteColor,
                            text = "Note",
                            onClick = {
                                pillsState.value = PillsUiState.Input(ItemType.NOTE)
                                textFieldValue.value = ""
                            }
                        )
                        CreateItemPill(
                            color = EchoListTheme.echoListColorScheme.taskColor,
                            text = "Task",
                            onClick = {
                                pillsState.value = PillsUiState.Input(ItemType.TASK_LIST)
                                textFieldValue.value = ""
                            }
                        )
                        CreateItemPill(
                            color = EchoListTheme.echoListColorScheme.folderColor,
                            text = "Folder",
                            onClick = {
                                pillsState.value = PillsUiState.Input(ItemType.FOLDER)
                                textFieldValue.value = ""
                            }
                        )
                    }
                }
                is PillsUiState.Input -> {
                    val itemType = state.itemType
                    
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                    
                    BasicTextField(
                        value = textFieldValue.value,
                        onValueChange = { textFieldValue.value = it },
                        modifier = Modifier
                            .focusRequester(focusRequester),
                        textStyle = EchoListTheme.typography.labelMedium.copy(
                            color = EchoListTheme.materialColors.onSurface
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val callback = resolveImeAction(itemType, textFieldValue.value)
                                if (callback != null) {
                                    when (callback) {
                                        ItemType.NOTE -> onCreateNote(textFieldValue.value)
                                        ItemType.TASK_LIST -> onTaskCreate(textFieldValue.value)
                                        ItemType.FOLDER -> onFolderCreate(textFieldValue.value)
                                        ItemType.UNSPECIFIED -> {}
                                    }
                                }
                                pillsState.value = PillsUiState.Idle
                            }
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = itemType.pillColor(),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .padding(
                                        horizontal = EchoListTheme.dimensions.m,
                                        vertical = EchoListTheme.dimensions.s
                                    ),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                innerTextField()
                            }
                        }
                    )
                }
            }
        }
        
        Spacer(Modifier.width(EchoListTheme.dimensions.m))
        
        RoundIconButton(
            modifier = Modifier.rotate(45f),
            iconRes = Res.drawable.ic_plus,
            onClick = {
                pillsState.value = PillsUiState.Idle
                onClosePills()
            },
            containerColor = EchoListTheme.materialColors.primary,
            contentColor = EchoListTheme.materialColors.onPrimary
        )
    }
}