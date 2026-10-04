package net.onefivefour.echolist.ui.editnote

import net.onefivefour.echolist.core.designsystem.resources.Res as SharedRes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import echolist.composeapp.generated.resources.Res
import net.onefivefour.echolist.core.designsystem.resources.ic_delete
import net.onefivefour.echolist.core.designsystem.resources.ic_edit
import net.onefivefour.echolist.core.designsystem.resources.visibility_on
import net.onefivefour.echolist.core.designsystem.ui.components.ElButton
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun EditNoteToolbar(
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPreviewToggle: () -> Unit,
    uiState: EditNoteUiState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.m)
    ) {

        Icon(
            painter = painterResource(SharedRes.drawable.ic_delete),
            contentDescription = "Delete Note",
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { onDeleteClick() }
                .padding(
                    horizontal = EchoListTheme.dimensions.m,
                    vertical = EchoListTheme.dimensions.m
                )
        )

        ElButton(
            onClick = onSaveClick,
            isEnabled = uiState.isSaveEnabled,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (uiState.isSaving) "Saving..." else "Save",
                style = EchoListTheme.typography.labelMedium
            )
        }

        Icon(
            painter = painterResource(
                when (uiState.isPreview) {
                    true -> SharedRes.drawable.ic_edit
                    false -> SharedRes.drawable.visibility_on
                }
            ),
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
}