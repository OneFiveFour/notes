package net.onefivefour.echolist.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import net.onefivefour.echolist.data.models.ItemType
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun ItemType.pillColor(): Color = when (this) {
    ItemType.NOTE -> EchoListTheme.echoListColorScheme.noteColor
    ItemType.TASK_LIST -> EchoListTheme.echoListColorScheme.taskColor
    ItemType.FOLDER -> EchoListTheme.echoListColorScheme.folderColor
    ItemType.UNSPECIFIED -> Color.Transparent
}

internal fun ItemType.pillLabel(): String = when (this) {
    ItemType.NOTE -> "+ Note"
    ItemType.TASK_LIST -> "+ Task"
    ItemType.FOLDER -> "+ Folder"
    ItemType.UNSPECIFIED -> ""
}
