package net.onefivefour.echolist.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.ic_search
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun RoundIconButton(
    iconRes: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = EchoListTheme.materialColors.surface,
    contentColor: Color = EchoListTheme.materialColors.onSurface,
    contentDescription: String? = null
) {

    Surface(
        modifier = modifier
            .size(EchoListTheme.dimensions.xxxl)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        border = BorderStroke(
            width = 1.dp,
            color = EchoListTheme.materialColors.surfaceVariant
        ),
        color = containerColor
    ) {
        Icon(
            modifier = Modifier.padding(EchoListTheme.dimensions.m),
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = contentColor
        )
    }
}

@Preview
@Composable
private fun RoundIconButtonPreview() {
    EchoListTheme {
        RoundIconButton(
            iconRes = Res.drawable.ic_search,
            onClick = {}
        )
    }
}