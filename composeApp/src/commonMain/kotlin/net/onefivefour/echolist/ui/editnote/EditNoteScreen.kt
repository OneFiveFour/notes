package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.edit
import echolist.composeapp.generated.resources.visibility
import kotlinx.serialization.builtins.ArraySerializer
import net.onefivefour.echolist.ui.common.ElButton
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.painterResource

@Composable
fun EditNoteScreen(
    uiState: EditNoteUiState,
    onPreviewToggle: () -> Unit,
    onToolbarAction: (MarkdownToolbarAction) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = EchoListTheme.dimensions

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = dimensions.xl,
                vertical = dimensions.l
            )
    ) {

        if (uiState.isCreateMode) {
            EditorCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                NoteTextField(
                    modifier = Modifier.fillMaxWidth(),
                    state = uiState.titleState,
                    placeholder = "Note title",
                    textStyle = EchoListTheme.typography.titleMedium
                )
            }
        } else {
            Text(
                text = uiState.titleState.text.toString(),
                style = EchoListTheme.typography.titleLarge,
                color = EchoListTheme.materialColors.primary
            )
        }

        Spacer(modifier = Modifier.height(dimensions.m))

        EditorCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading note...",
                        style = EchoListTheme.typography.bodyMedium,
                        color = EchoListTheme.materialColors.onSurface
                    )
                }

                uiState.isPreview -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (uiState.contentState.text.isEmpty()) {
                        Text(
                            text = "Nothing to preview yet.",
                            style = EchoListTheme.typography.bodyMedium,
                            color = EchoListTheme.materialColors.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        MarkdownPreview(
                            document = uiState.contentState.text.toString(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                else -> NoteTextField(
                    state = uiState.contentState,
                    placeholder = "Write your note in markdown...",
                    modifier = Modifier.fillMaxSize(),
                    textStyle = EchoListTheme.typography.bodyMedium
                )
            }
        }

        uiState.error?.let { errorMessage ->
            Spacer(modifier = Modifier.height(dimensions.s))
            Text(
                text = errorMessage,
                style = EchoListTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(dimensions.m))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (!uiState.isPreview) {
                MarkdownToolbar(
                    modifier = Modifier.weight(1f),
                    onToolbarAction = onToolbarAction
                )
            }

            Spacer(modifier = Modifier.width(dimensions.m))

            Icon(
                painter = painterResource(when (uiState.isPreview) {
                    true -> Res.drawable.edit
                    false -> Res.drawable.visibility
                }),
                contentDescription = when (uiState.isPreview) {
                    true -> "Edit"
                    false -> "Preview"
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onPreviewToggle() }
                    .padding(
                        horizontal = EchoListTheme.dimensions.m,
                        vertical = EchoListTheme.dimensions.m
                    )
            )
        }

        Spacer(modifier = Modifier.height(dimensions.m))

        ElButton(
            onClick = onSaveClick,
            isEnabled = uiState.isSaveEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (uiState.isSaving) "Saving..." else "Save",
                style = EchoListTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun EditorCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        shape = EchoListTheme.shapes.medium,
        color = EchoListTheme.materialColors.surface,
        modifier = modifier.border(
            width = EchoListTheme.dimensions.borderWidth,
            color = EchoListTheme.materialColors.surfaceVariant,
            shape = EchoListTheme.shapes.medium
        )
    ) {
        Box(
            modifier = Modifier
                .padding(EchoListTheme.dimensions.m)
        ) {
            content()
        }
    }
}

@Composable
private fun NoteTextField(
    state: TextFieldState,
    placeholder: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (state.text.isEmpty()) {
            Text(
                text = placeholder,
                style = textStyle,
                color = EchoListTheme.materialColors.onSurface.copy(alpha = 0.45f)
            )
        }

        BasicTextField(
            modifier = Modifier.fillMaxSize(),
            state = state,
            textStyle = textStyle.copy(
                color = EchoListTheme.materialColors.onSurface
            )
        )
    }
}
