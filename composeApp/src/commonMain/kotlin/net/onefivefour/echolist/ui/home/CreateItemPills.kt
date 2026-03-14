package net.onefivefour.echolist.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.ic_plus
import net.onefivefour.echolist.data.models.ItemType
import net.onefivefour.echolist.ui.common.RoundIconButton
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
fun CreateItemPills(
    createItemCallbacks: CreateItemCallbacks,
    onClosePills: () -> Unit
) {
    val pillTypes = remember {
        listOf(
            ItemType.NOTE,
            ItemType.TASK_LIST,
            ItemType.FOLDER
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        pillTypes.forEach { itemType ->
            CreateItemPill(
                itemType = itemType,
                onClick = { itemType ->
                    when (itemType) {
                        ItemType.UNSPECIFIED -> {}
                        ItemType.FOLDER -> createItemCallbacks.onCreateFolder()
                        ItemType.NOTE -> createItemCallbacks.onCreateNote()
                        ItemType.TASK_LIST -> createItemCallbacks.onCreateTaskList()
                    }
                }
            )
        }

        RoundIconButton(
            modifier = Modifier.rotate(45f),
            iconRes = Res.drawable.ic_plus,
            onClick = onClosePills,
            containerColor = EchoListTheme.materialColors.primary,
            contentColor = EchoListTheme.materialColors.onPrimary
        )
    }
}

@Preview
@Composable
private fun CreateItemPillsPreview() {
    EchoListTheme {
        CreateItemPills(
            createItemCallbacks = CreateItemCallbacks(),
            onClosePills = {}
        )
    }
}
