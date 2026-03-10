package net.onefivefour.echolist.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.LookaheadScope
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.ic_plus
import net.onefivefour.echolist.data.models.ItemType
import net.onefivefour.echolist.ui.common.ExpandableBox
import net.onefivefour.echolist.ui.common.ExpandableBoxState
import net.onefivefour.echolist.ui.common.RoundIconButton
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
fun CreateItemPills(
    onCreateNote: (String) -> Unit,
    onTaskCreate: (String) -> Unit,
    onFolderCreate: (String) -> Unit,
    onClosePills: () -> Unit
) {
    val pillTypes = remember { listOf(ItemType.NOTE, ItemType.TASK_LIST, ItemType.FOLDER) }
    var expandedType by remember { mutableStateOf<ItemType?>(null) }

    LookaheadScope {
        Row(
            horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            pillTypes.forEach { itemType ->
                val isExpanded = expandedType == itemType

                AnimatedVisibility(
                    visible = expandedType == null || isExpanded,
                    modifier = if (isExpanded) Modifier.weight(1f) else Modifier,
                    enter = fadeIn(tween(150)) + expandHorizontally(
                        animationSpec = tween(300),
                        expandFrom = Alignment.Start
                    ),
                    exit = fadeOut(tween(150)) + shrinkHorizontally(
                        animationSpec = tween(300),
                        shrinkTowards = Alignment.Start
                    )
                ) {
                    ExpandableBox(
                        state = if (isExpanded) ExpandableBoxState.EXPANDED else ExpandableBoxState.COLLAPSED,
                        onClick = {
                            expandedType = if (isExpanded) null else itemType
                        },
                        lookaheadScope = this@LookaheadScope,
                        modifier = Modifier
                            .background(
                                color = itemType.pillColor(),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(
                                horizontal = EchoListTheme.dimensions.m,
                                vertical = EchoListTheme.dimensions.s
                            )
                    ) {
                        Text(
                            text = itemType.pillLabel(),
                            style = EchoListTheme.typography.labelMedium
                        )
                    }
                }
            }

            RoundIconButton(
                modifier = Modifier.rotate(45f),
                iconRes = Res.drawable.ic_plus,
                onClick = {
                    if (expandedType != null) {
                        expandedType = null
                    } else {
                        onClosePills()
                    }
                },
                containerColor = EchoListTheme.materialColors.primary,
                contentColor = EchoListTheme.materialColors.onPrimary
            )
        }
    }
}
