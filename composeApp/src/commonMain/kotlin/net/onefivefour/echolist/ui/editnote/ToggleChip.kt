package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.check_box_checked
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ToggleChip(
    label: String,
    iconRes: DrawableResource,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = EchoListTheme.materialColors

    Surface(
        shape = RoundedCornerShape(50),
        color = if (isSelected) colors.primary else colors.surface,
        modifier = Modifier
            .border(
                width = EchoListTheme.dimensions.borderWidth,
                color = if (isSelected) colors.primary else colors.surfaceVariant,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            modifier = Modifier
            .padding(
                horizontal = EchoListTheme.dimensions.m,
            vertical = EchoListTheme.dimensions.s
        ),
            tint = if (isSelected) colors.onPrimary else colors.onSurface
        )
    }
}

@Preview
@Composable
private fun ToggleChipPreviewSelected() {
    EchoListTheme {
        ToggleChip(
            label = "Bold",
            iconRes = Res.drawable.check_box_checked,
            isSelected = true,
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun ToggleChipPreview() {
    EchoListTheme {
        ToggleChip(
            label = "Bold",
            iconRes = Res.drawable.check_box_checked,
            isSelected = false,
            onClick = {}
        )
    }
}