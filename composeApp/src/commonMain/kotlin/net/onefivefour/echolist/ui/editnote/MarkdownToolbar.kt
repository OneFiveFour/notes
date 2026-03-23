package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun MarkdownToolbar(
    onToolbarAction: (MarkdownToolbarAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = EchoListTheme.dimensions
    val colors = EchoListTheme.materialColors
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = EchoListTheme.materialColors.surfaceVariant,
                shape = RoundedCornerShape(50)
            ),
        shape = RoundedCornerShape(50)
    ) {
        Row(modifier = Modifier.horizontalScroll(scrollState)) {
            MarkdownToolbarAction.entries.forEach { action ->
                Icon(
                    painter = painterResource(action.iconRes),
                    contentDescription = action.label,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onToolbarAction(action) }
                        .padding(
                            horizontal = EchoListTheme.dimensions.m,
                            vertical = EchoListTheme.dimensions.m
                        ),
                    tint = colors.onSurface
                )
            }
        }
    }
}

@Preview
@Composable
private fun MarkdownToolbarPreview() {
    EchoListTheme {
        MarkdownToolbar(
            onToolbarAction = {}
        )
    }
}