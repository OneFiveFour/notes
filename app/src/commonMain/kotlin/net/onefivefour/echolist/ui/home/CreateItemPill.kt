package net.onefivefour.echolist.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import net.onefivefour.echolist.data.models.ItemType
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun CreateItemPill(
    itemType: ItemType,
    onClick: (ItemType) -> Unit
) {
        Text(
            modifier = Modifier
                .clickable { onClick(itemType) }
                .background(
                    color = itemType.pillColor(),
                    shape = RoundedCornerShape(50)
                )
                .padding(
                    horizontal = EchoListTheme.dimensions.m,
                    vertical = EchoListTheme.dimensions.s
                ),
            text = itemType.pillLabel(),
            style = EchoListTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
}