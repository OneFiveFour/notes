package net.onefivefour.echolist.ui.recurrence

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.ui.theme.EchoListTheme

/**
 * Consistent inline label text used by the recurrence detail rows
 * (e.g. "Repeat every", "weeks", "of the month").
 */
@Composable
internal fun LabelText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        text = text,
        style = EchoListTheme.typography.bodyMedium,
        color = EchoListTheme.materialColors.onSurface
    )
}
