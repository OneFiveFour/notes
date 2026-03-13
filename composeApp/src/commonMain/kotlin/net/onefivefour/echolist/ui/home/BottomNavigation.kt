package net.onefivefour.echolist.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
internal fun BottomNavigation(
    onNoteCreate: () -> Unit = {},
    onTaskCreate: () -> Unit = {},
    onFolderCreate: () -> Unit = {}
) {

    var isFabExpanded by remember { mutableStateOf(false) }

    Crossfade(targetState = isFabExpanded) { showItemPills ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (showItemPills) {
                true -> {
                    CreateItemPills(
                        onCreateNote = {
                            onNoteCreate()
                            isFabExpanded = false
                        },
                        onCreateTaskList = {
                            onTaskCreate()
                            isFabExpanded = false
                        },
                        onCreateFolder = {
                            onFolderCreate()
                            isFabExpanded = false
                        },
                        onClosePills = {
                            isFabExpanded = false
                        }
                    )
                }
                else -> {
                    BottomButtons(
                        onOpenPills = {
                            isFabExpanded = true
                        }
                    )
                }
            }
        }
    }
}