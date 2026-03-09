package net.onefivefour.echolist.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.ic_plus
import net.onefivefour.echolist.ui.common.RoundIconButton
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
fun CreateItemPills(
    onCreateNote: () -> Unit,
    onTaskCreate: () -> Unit,
    onFolderCreate: () -> Unit,
    onClosePills: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CreateItemPill(
            color = EchoListTheme.echoListColorScheme.noteColor,
            text = "Note",
            onClick = onCreateNote
        )
        CreateItemPill(
            color = EchoListTheme.echoListColorScheme.taskColor,
            text = "Task",
            onClick = onTaskCreate
        )
        CreateItemPill(
            color = EchoListTheme.echoListColorScheme.folderColor,
            text = "Folder",
            onClick = onFolderCreate
        )
        RoundIconButton(
            modifier = Modifier.rotate(45f),
            iconRes = Res.drawable.ic_plus,
            onClick = onClosePills,
            containerColor = EchoListTheme.materialColors.primary,
            contentColor = EchoListTheme.materialColors.onPrimary
        )
    }
}