package net.onefivefour.echolist.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.create
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CreateItemPill(
    color: Color,
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .clickable(onClick = onClick)
            .padding(
                horizontal = EchoListTheme.dimensions.m,
                vertical = EchoListTheme.dimensions.s
            )
    ) {
        Text(
            text = stringResource(Res.string.create, text),
            style = EchoListTheme.typography.labelMedium
        )
    }
}


@Composable
@Preview
private fun CreateItemPillPreview() {
    EchoListTheme {
        CreateItemPill(
            color = EchoListTheme.echoListColorScheme.noteColor,
            text = "Note",
            onClick = {}
        )
    }
}