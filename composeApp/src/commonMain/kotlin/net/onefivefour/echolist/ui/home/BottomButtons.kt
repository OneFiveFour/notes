package net.onefivefour.echolist.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.ic_plus
import echolist.composeapp.generated.resources.ic_search
import echolist.composeapp.generated.resources.ic_settings
import net.onefivefour.echolist.ui.common.RoundIconButton
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
fun BottomButtons(
    onOpenPills: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundIconButton(
            iconRes = Res.drawable.ic_search,
            onClick = {}
        )
        RoundIconButton(
            iconRes = Res.drawable.ic_settings,
            onClick = {}
        )
        RoundIconButton(
            iconRes = Res.drawable.ic_plus,
            onClick = onOpenPills,
            containerColor = EchoListTheme.materialColors.primary,
            contentColor = EchoListTheme.materialColors.onPrimary
        )
    }
}