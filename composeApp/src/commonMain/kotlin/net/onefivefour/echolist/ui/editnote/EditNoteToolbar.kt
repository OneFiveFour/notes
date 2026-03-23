package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.edit
import echolist.composeapp.generated.resources.visibility
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun EditNoteToolbar(
    uiState: EditNoteUiState,
    onToolbarAction: (MarkdownToolbarAction) -> Unit,
    onPreviewToggle: () -> Unit
) {
    Row(
        modifier = Modifier.Companion.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        if (!uiState.isPreview) {
            MarkdownToolbar(
                modifier = Modifier.Companion.weight(1f),
                onToolbarAction = onToolbarAction
            )
        }

        Spacer(modifier = Modifier.Companion.width(EchoListTheme.dimensions.m))

        Icon(
            painter = painterResource(
                when (uiState.isPreview) {
                    true -> Res.drawable.edit
                    false -> Res.drawable.visibility
                }
            ),
            contentDescription = when (uiState.isPreview) {
                true -> "Edit"
                false -> "Preview"
            },
            modifier = Modifier.Companion
                .clip(RoundedCornerShape(50))
                .clickable { onPreviewToggle() }
                .padding(
                    horizontal = EchoListTheme.dimensions.m,
                    vertical = EchoListTheme.dimensions.m
                )
        )
    }
}