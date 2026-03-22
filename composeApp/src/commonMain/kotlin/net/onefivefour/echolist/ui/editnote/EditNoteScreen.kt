package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import net.onefivefour.echolist.ui.common.ElButton
import net.onefivefour.echolist.ui.theme.EchoListTheme

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
        Text(
            text = if (uiState.isCreateMode) "New note" else "Edit note",
            style = EchoListTheme.typography.titleLarge,
            color = EchoListTheme.materialColors.primary
        )

        Spacer(modifier = Modifier.height(dimensions.l))

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
                text = "Title",
                style = EchoListTheme.typography.labelMedium,
                color = EchoListTheme.materialColors.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(dimensions.xs))

            EditorCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = uiState.titleState.text.toString(),
                    style = EchoListTheme.typography.titleMedium,
                    color = EchoListTheme.materialColors.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensions.m))

        Text(
            text = "Supported markdown: bold, bullet points, checkboxes, hyperlinks, and headings. Everything else is saved unchanged and previewed as plain text.",
            style = EchoListTheme.typography.bodySmall,
            color = EchoListTheme.materialColors.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(dimensions.m))

        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensions.s)
        ) {
            ToggleChip(
                label = "Edit",
                isSelected = !uiState.isPreview,
                onClick = {
                    if (uiState.isPreview) onPreviewToggle()
                }
            )
            ToggleChip(
                label = "Preview",
                isSelected = uiState.isPreview,
                onClick = {
                    if (!uiState.isPreview) onPreviewToggle()
                }
            )
        }

        if (!uiState.isPreview) {
            Spacer(modifier = Modifier.height(dimensions.m))
            MarkdownToolbar(
                onToolbarAction = onToolbarAction
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

        Spacer(modifier = Modifier.height(dimensions.l))

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
private fun MarkdownToolbar(
    onToolbarAction: (MarkdownToolbarAction) -> Unit
) {
    val dimensions = EchoListTheme.dimensions
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(dimensions.s)
    ) {
        MarkdownToolbarAction.entries.forEach { action ->
            ToggleChip(
                label = action.label,
                isSelected = false,
                onClick = { onToolbarAction(action) }
            )
        }
    }
}

@Composable
private fun ToggleChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = EchoListTheme.materialColors

    Surface(
        shape = RoundedCornerShape(50),
        color = if (isSelected) colors.primary else colors.surface,
        modifier = Modifier
            .border(
                width = EchoListTheme.dimensions.borderWidth,
                color = if (isSelected) colors.primary else colors.surfaceVariant,
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = EchoListTheme.typography.labelMedium,
            color = if (isSelected) colors.onPrimary else colors.onSurface,
            modifier = Modifier.padding(
                horizontal = EchoListTheme.dimensions.m,
                vertical = EchoListTheme.dimensions.s
            )
        )
    }
}

@Composable
private fun EditorCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        shape = EchoListTheme.shapes.small,
        color = EchoListTheme.materialColors.surface,
        modifier = modifier.border(
            width = EchoListTheme.dimensions.borderWidth,
            color = EchoListTheme.materialColors.surfaceVariant,
            shape = EchoListTheme.shapes.small
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(EchoListTheme.dimensions.m)
        ) {
            content()
        }
    }
}

@Composable
private fun NoteTextField(
    state: androidx.compose.foundation.text.input.TextFieldState,
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
            state = state,
            modifier = Modifier.fillMaxSize(),
            textStyle = textStyle.copy(
                color = EchoListTheme.materialColors.onSurface
            )
        )
    }
}
